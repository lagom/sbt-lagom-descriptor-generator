package com.lightbend.lagom.spec.parser

import java.io.InputStream

import com.lightbend.lagom.spec.model._
import com.lightbend.lagom.spec.{ LagomGenerator, ResourceUtils }
import io.swagger.models._
import io.swagger.models.parameters._
import io.swagger.models.properties._
import io.swagger.parser.SwaggerParser

import scala.collection.JavaConverters._

trait SpecParser[T] {
  val parse: LagomGenerator[T]#Parse

  val convert: LagomGenerator[T]#Convert

  /**
   * Convenience method to [[parse]] and then [[convert]] in a single step.
   */
  val read: (InputStream) => Service = { input =>
    (parse andThen convert)(input)
  }
}

object OpenApiV2ModelParser {

  def parseModel(swagger: Swagger): Set[CustomType] = {

    swagger.getDefinitions.asScala.flatMap {
      case (name, sModel) =>
        val fields: Seq[ModelField] = sModel.getProperties.asScala.map {
          case (fieldName, sProperty) =>
            val actualType = propertyToType(fieldName, sProperty)
            // A field must be optional if it's not a Collection and is not listed in the model as required
            val t2 = (sModel, actualType) match {
              case (_, LSequence(_)) | (_, LSet(_))                                                    => actualType
              case (mi: ModelImpl, _) if mi.getRequired != null && !mi.getRequired.contains(fieldName) => LOptional(actualType)
              case _                                                                                   => actualType
            }
            ModelField(t2, fieldName)
        }.toList
        CustomModel(name, fields) +: produceEnums(fields)
    }.toSet
  }

  private def produceEnums(fields: Seq[ModelField]): Seq[CustomEnum] =
    fields.flatMap {
      case ModelField(LOptional(t), _) => Seq(t)
      case ModelField(LSequence(t), _) => Seq(t)
      case ModelField(LSet(t), _)      => Seq(t)
      case ModelField(LMap(t, q), _)   => Seq(t, q)
      case ModelField(t, _)            => Seq(t)
    }.collect {
      case LEnum(name, values) => CustomEnum(name, values)
    }

  def modelToType(swaggerModel: Model): Type = {
    swaggerModel match {
      case model: RefModel      => LUserDefined(model.getSimpleRef)
      case model: ArrayModel    => ??? // inline models not supported
      case model: ModelImpl     => ??? // inline models not supported
      case model: ComposedModel => ??? // inline models not supported
    }
  }

  def propertyToType(preferredName: String, swaggerProperty: Property): Type =
    swaggerProperty match {
      // TODO: don't use hardcoded collection type.
      // TODO: support other formats (email, uuid,... ???)
      case arr: ArrayProperty => LSequence(propertyToType(preferredName, arr.getItems))
      case ref: RefProperty   => LUserDefined(ref.getSimpleRef)

      case str: StringProperty if str.getEnum != null => {
        // TODO: use naming features to remove '-', add camelcasing, etc...
        val camel = s"${preferredName.head.toUpper}${preferredName.tail}"
        val enumName = s"${camel}Enum"
        LEnum(enumName, str.getEnum.asScala)
      }
      case email: EmailProperty       => ???
      case str: StringProperty        => dataTypeConversion(preferredName, str.getType, str.getFormat)
      case binary: BinaryProperty     => ???
      case byteArr: ByteArrayProperty => ???
      case uuid: UUIDProperty         => ???
      case obj: ObjectProperty        => ???
      case pwd: PasswordProperty      => ???

      case file: FileProperty         => ???

      case date: DateProperty         => LDate
      case dateTime: DateTimeProperty => LDateTime

      case boolean: BooleanProperty   => LBoolean
      case integer: IntegerProperty   => LInt
      case long: LongProperty         => LLong
      case float: FloatProperty       => LFloat
      case double: DoubleProperty     => LDouble
    }

  // See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#data-types
  def dataTypeConversion(preferredName: String, `type`: String, format: String): Type =
    // TODO: support other formats (email, uuid,... ???)
    (`type`, format) match {
      case ("integer", "int32")    => LInt
      case ("integer", "int64")    => LLong
      case ("number", "float")     => LFloat
      case ("number", "double")    => LDouble
      case ("boolean", _)          => LBoolean
      case ("string", "byte")      => ??? // base64 encoded characters
      case ("string", "binary")    => ??? // any sequence of octets
      case ("string", "date")      => LDate // As defined by full-date - RFC3339
      case ("string", "date-time") => LDateTime // As defined by date-time - RFC3339
      case ("string", "password")  => ??? // Used to hint UIs the input needs to be obscured
      case ("string", _)           => LString
    }

}

class OpenApiV2Parser(packageName: String) extends SpecParser[Swagger] {

  override val parse: (InputStream) => Swagger = { inputStream =>
    val swaggerAsString = ResourceUtils.loadContents(inputStream)
    new SwaggerParser().parse(swaggerAsString)
  }

  override val convert: (Swagger) => Service = { swagger =>
    Service(
      `package` = packageName,
      name = serviceName(swagger),
      calls = convertCalls(swagger),
      customModels = OpenApiV2ModelParser.parseModel(swagger),
      // building code from a spec usually means it's public endpoints.
      // TODO: needs review
      autoAcl = true
    )
  }

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
    val requestType: Option[Type] =
      bodyParams(operation)
        .map {
          case bodyParam: BodyParameter  => OpenApiV2ModelParser.modelToType(bodyParam.getSchema)
          case refParam: RefParameter    => ???
          case refParam: HeaderParameter => ???
          case refParam: FormParameter   => ???
          case refParam: PathParameter   => ???
          case refParam: CookieParameter => ???
          case refParam: QueryParameter  => ???
        }
        .headOption

    val responseType: Option[Type] =
      operation.getResponses.asScala.toSeq
        .filter { case (status, _) => status.toInt >= 200 && status.toInt < 300 }
        .map { case (_, response) => response }
        .headOption
        .map { resp =>
          OpenApiV2ModelParser.propertyToType("Response", resp.getSchema)
        }

    val arguments = (pathParams(operation) ++ queryParams(operation)).map { param =>
      CallArgument(param.getName, paramTypeDiscovery(param.getName, param.asInstanceOf[AbstractSerializableParameter[_]]))
    }

    // TODO: normalize operationId name to a valid java/scala method name.
    Call(method, path, callName, requestType, responseType, arguments)
  }

  /**
   * Given a Parameter will generate a String with the Type info.
   */
  private def paramTypeDiscovery(preferredName: String, param: AbstractSerializableParameter[_]): Type = {
    val (paramType, paramFormat) = (param.getType, param.getFormat) // p.getCollectionFormat

    if (paramType == "array") {
      LSequence(OpenApiV2ModelParser.propertyToType(preferredName, param.getItems))
    } else {
      OpenApiV2ModelParser.dataTypeConversion(preferredName, paramType, paramFormat)
    }
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
