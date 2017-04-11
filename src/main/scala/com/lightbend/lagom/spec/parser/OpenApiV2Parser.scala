package com.lightbend.lagom.spec.parser

import java.io.InputStream

import com.lightbend.lagom.spec.ResourceUtils
import com.lightbend.lagom.spec.generator.{ Call, CallArgument, Method, Service }
import io.swagger.models.parameters._
import io.swagger.models._
import io.swagger.models.properties._
import io.swagger.parser.SwaggerParser

trait SpecParser[T] {
  val parse: (InputStream) => T

  val convert: (T) => Service

  /**
   * Convenience method to [[parse]] and then [[convert]] in a single step.
   */
  val read: (InputStream) => Service = { input =>
    (parse andThen convert)(input)
  }
}

// TODO: rename to SwaggerV2Parser. Json or YAML should be processedindistinctly with this parser.
class OpenApiV2Parser(packageName: String) extends SpecParser[Swagger] {

  override val parse: (InputStream) => Swagger = { inputStream =>
    val swaggerAsString = ResourceUtils.loadContents(inputStream)
    new SwaggerParser().parse(swaggerAsString)
  }

  override val convert: (Swagger) => Service = { swagger =>
    val addPetCall = Call(Method.POST, "/v2/pet", "addPet", requestType = Some("Pet"))
    val detelePet = Call(Method.DELETE, "/v2/pet/:petId", "deletePet", arguments = Seq(CallArgument("petId", "long")))
    val findPetsByStatus = Call(Method.GET, "/v2/pet/findByStatus?status", "findPetsByStatus", responseType = Some("org.pcollections.PSequence<Pet>"), arguments = Seq(CallArgument("status", "org.pcollections.PSequence<String>")))
    val findPetsByTags = Call(Method.GET, "/v2/pet/findByTags?tags", "findPetsByTags", responseType = Some("org.pcollections.PSequence<Pet>"), arguments = Seq(CallArgument("tags", "org.pcollections.PSequence<String>")))
    val getPetById = Call(Method.GET, "/v2/pet/:petId", "getPetById", responseType = Some("Pet"), arguments = Seq(CallArgument("petId", "long")))
    val updatePet = Call(Method.PUT, "/v2/pet", "updatePet", requestType = Some("Pet"))
    val updatePetWithFor = Call(Method.POST, "/v2/pet/:petId", "updatePetWithForm", arguments = Seq(CallArgument("petId", "long")))

    val mock = Service("unused", "unused",
      Seq("Pet", "ModelApiResponse"),
      Seq(addPetCall, detelePet, findPetsByStatus, findPetsByTags, getPetById, updatePet, updatePetWithFor))

    mock.copy(
      `package` = packageName,
      name = serviceName(swagger),
      calls = convertCalls(swagger)
    )
  }

  import scala.collection.JavaConverters._

  def serviceName(swagger: Swagger): String = {
    val tags: Iterable[String] = for {
      path <- swagger.getPaths.asScala.values
      operation <- path.getOperations.asScala
      tag <- operation.getTags.asScala
    } yield {
      tag
    }
    // use the most common tag as service name.
    tags.groupBy(identity).mapValues(_.size).maxBy(_._2)._1
  }

  def convertCalls(swagger: Swagger): Seq[Call] = {
    val basePath = swagger.getBasePath
    (for {
      (uriPath, swaggerPath) <- swagger.getPaths.asScala.toSeq
      (method, operation) <- swaggerPath.getOperationMap.asScala.toSeq
    } yield {
      asCall(swaggerPath, operation, method, basePath, uriPath)
    }).sortBy(_.name)
  }

  private def asCall(swaggerPath: Path, operation: Operation, swaggerMethod: HttpMethod, basePath: String, uriPath: String): Call = {
    val callName = operation.getOperationId

    val path = convertPath(swaggerPath, operation, basePath, uriPath)
    val method = swaggerMethod match {
      case HttpMethod.GET     => Method.GET
      case HttpMethod.PUT     => Method.PUT
      case HttpMethod.POST    => Method.POST
      case HttpMethod.DELETE  => Method.DELETE
      case HttpMethod.OPTIONS => Method.OPTIONS
      case HttpMethod.HEAD    => Method.HEAD
      case HttpMethod.PATCH   => Method.PATCH
    }

    // TODO: support all kinds of models
    val requestType: Option[String] =
      bodyParams(operation)
        .map {
          case bodyParam: BodyParameter =>
            bodyParam.getSchema match {
              case model: RefModel      => model.getSimpleRef
              case model: ArrayModel    => ???
              case model: ModelImpl     => ???
              case model: ComposedModel => ???
            }
          case refParam: RefParameter => ???
        }
        .headOption

    val responseType: Option[String] =
      operation.getResponses.asScala.toSeq
        .filter { case (status, _) => status.toInt >= 200 && status.toInt < 300 }
        .map { case (_, response) => response }
        .headOption
        .map { resp =>
          propertyToType(resp.getSchema)
        }

    val arguments = (pathParams(operation) ++ queryParams(operation)).map { param =>
      CallArgument(param.getName, paramTypeDiscovery(param.asInstanceOf[AbstractSerializableParameter[_]]))
    }

    // TODO: normalize operationId name to a valid java/scala method name.
    Call(method, path, callName, requestType, responseType, arguments)
  }

  /**
   * Given a Parameter will generate a String with the Type info.
   */
  private def paramTypeDiscovery(param: AbstractSerializableParameter[_]): String = {
    val (paramType, paramFormat) = (param.getType, param.getFormat) // p.getCollectionFormat

    if (paramType == "array") {
      s"org.pcollections.PSequence<${propertyToType(param.getItems)}>"
    } else {
      dataTypeConversion(paramType, paramFormat)
    }
  }

  // See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#data-types
  private def dataTypeConversion(`type`: String, format: String): String = {
    // TODO: support other formats (email, uuid,... ???)
    (`type`, format) match {
      case ("integer", "int32")    => "int"
      case ("integer", "int64")    => "long"
      case ("number", "float")     => "float"
      case ("number", "double")    => "double"
      case ("string", _)           => "String"
      case ("string", "byte")      => ??? // base64 encoded characters
      case ("string", "binary")    => ??? // any sequence of octets
      case ("boolean", _)          => "boolean"
      case ("string", "date")      => ??? // As defined by full-date - RFC3339
      case ("string", "date-time") => ??? // As defined by date-time - RFC3339
      case ("string", "password")  => ??? // Used to hint UIs the input needs to be obscured
    }
  }

  def propertyToType: PartialFunction[Property, String] = {
    // TODO: don't use hardcoded collection type.
    // TODO: support other formats (email, uuid,... ???)
    case arr: ArrayProperty  => s"org.pcollections.PSequence<${propertyToType(arr.getItems)}>"
    case ref: RefProperty    => ref.getSimpleRef

    case email: EmailProperty=> ??? // it's a subclass of StringPropertye, must match before StringProperty
    case str: StringProperty => dataTypeConversion(str.getType, str.getFormat)
    case binary: BinaryProperty=> ???
    case byteArr: ByteArrayProperty=> ???

    case date: DateProperty=> ???
    case dateTime: DateTimeProperty => ???
    case uuid: UUIDProperty => ???
    case obj: ObjectProperty => ???

    case boolean: BooleanProperty => ???
    case integer: IntegerProperty=> ???
    case long: LongProperty=> ???
    case float: FloatProperty=> ???
    case double:DoubleProperty=> ???

    case file:FileProperty=> ???
    case pwd:PasswordProperty=> ???
  }

  /**
   * @param swaggerPath [[io.swagger.models.Path]] object representing a Swagger's Path (operations,
   *                    description, parameters...). The naming is horrible :-( .
   * @param operation   [[io.swagger.models.Operation]] object representing a Swagger's Operation.
   * @param basePath    a String with the prefix of the URI path applicable globally to all uriPath's
   * @param uriPath     a String with a relative path of a URI.
   * @return
   */
  private def convertPath(swaggerPath: Path, operation: Operation, basePath: String, uriPath: String): String = {

    val completePath = (basePath + uriPath).replaceAll("\\{([^}]*)\\}", ":$1")

    val params = queryParams(operation)
    val queryString = if (params.isEmpty) "" else params.map(_.getName).sorted.mkString("?", "&", "")
    s"$completePath$queryString"
  }

  val bodyParams = params("body") _
  val pathParams = params("path") _
  val queryParams = params("query") _

  /**
   * Given a Swagger Operation and a Swagger Parameter Type ('query', 'body',...) returns a collection of Parameters
   * that fulfill the criteria.
   */
  private def params(`type`: String)(operation: Operation) = {
    Option(operation.getParameters)
      .getOrElse(java.util.Arrays.asList[Parameter]())
      .asScala
      .filter(_.getIn == `type`)
  }

}
