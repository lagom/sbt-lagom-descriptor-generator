package com.lightbend.lagom.spec.parser

import java.io.InputStream

import com.lightbend.lagom.spec.ResourceUtils
import com.lightbend.lagom.spec.generator.{ Call, CallArgument, Method, Service }
import io.swagger.models.parameters.Parameter
import io.swagger.models.{ Operation, Path, Swagger }
import io.swagger.parser.SwaggerParser

import scala.collection.{ immutable, mutable }

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
    swagger.getPaths.asScala.toIndexedSeq.flatMap {
      case (uriPath, swaggerPath) => {
        // This is awful. There's no way to iterate over the List[Operations] maintaining the Method
        Seq(
          asCall(swaggerPath, swaggerPath.getGet, Method.GET, basePath, uriPath),
          asCall(swaggerPath, swaggerPath.getPut, Method.PUT, basePath, uriPath),
          asCall(swaggerPath, swaggerPath.getPost, Method.POST, basePath, uriPath),
          asCall(swaggerPath, swaggerPath.getDelete, Method.DELETE, basePath, uriPath),
          asCall(swaggerPath, swaggerPath.getHead, Method.HEAD, basePath, uriPath),
          asCall(swaggerPath, swaggerPath.getOptions, Method.OPTIONS, basePath, uriPath),
          asCall(swaggerPath, swaggerPath.getPatch, Method.PATCH, basePath, uriPath)
        ).flatten
      }
    }.sortBy(_.name)
  }

  /**
   * @param swaggerPath [[io.swagger.models.Path]] object representing a Swagger's Path (operations,
   *                    description, parameters...). The naming is horrible :-( .
   * @param operation [[io.swagger.models.Operation]] object representing a Swagger's Operation.
   * @param basePath a String with the prefix of the URI path applicable globally to all uriPath's
   * @param uriPath a String with a relative path of a URI.
   * @return
   */
  private def convertPath(swaggerPath: Path, operation: Operation, basePath: String, uriPath: String): String = {

    val completePath = (basePath + uriPath).replaceAll("\\{([^}]*)\\}", ":$1")

    // TODO: test this overriding logic
    // reads all params of type "in: 'query'" and builds a map where the key is tha param name. Then repeats the
    // process using the params of type "in: 'query'" of the Operation. The second map will override values from the
    // first map so we have inheritance and overriding.
    val pathParams: Seq[Parameter] =
      (Option(swaggerPath.getParameters).getOrElse(java.util.Arrays.asList[Parameter]()).asScala.filter(_.getIn == "query").groupBy(_.getName) ++
        Option(operation.getParameters).getOrElse(java.util.Arrays.asList[Parameter]()).asScala.filter(_.getIn == "query").groupBy(_.getName)).values.flatten.toSeq

    val queryString = if (pathParams.isEmpty) "" else pathParams.map(_.getName).sorted.mkString("?", "&", "")
    s"$completePath$queryString"
  }

  private def asCall(swaggerPath: Path, nullable: Operation, method: Method.Method, basePath: String, uriPath: String): Option[Call] = {
    Option(nullable).map { operation =>
      // TODO: normalize operationId.
      Call(method, convertPath(swaggerPath, operation, basePath, uriPath), operation.getOperationId)
    }
  }

}
