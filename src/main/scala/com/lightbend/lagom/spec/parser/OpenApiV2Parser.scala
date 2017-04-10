package com.lightbend.lagom.spec.parser

import java.io.InputStream

import com.lightbend.lagom.spec.ResourceUtils
import com.lightbend.lagom.spec.generator.{ Call, CallArgument, Method, Service }
import io.swagger.parser.SwaggerParser

trait SpecParser {
  def parse(inputStream: InputStream): Service
}

// TODO: rename to SwaggerV2Parser. Json or YAML should be processedindistinctly with this parser.
object OpenApiV2Parser extends SpecParser {

  def parse(inputStream: InputStream): Service = {
    val addPetCall = Call(Method.POST, "/v2/pet", "addPet", requestType = Some("Pet"))
    val detelePet = Call(Method.DELETE, "/v2/pet/:petId", "deletePet", arguments = Seq(CallArgument("petId", "long")))
    val findPetsByStatus = Call(Method.GET, "/v2/pet/findByStatus?status", "findPetsByStatus", responseType = Some("org.pcollections.PSequence<Pet>"), arguments = Seq(CallArgument("status", "org.pcollections.PSequence<String>")))
    val findPetsByTags = Call(Method.GET, "/v2/pet/findByTags?tags", "findPetsByTags", responseType = Some("org.pcollections.PSequence<Pet>"), arguments = Seq(CallArgument("tags", "org.pcollections.PSequence<String>")))
    val getPetById = Call(Method.GET, "/v2/pet/:petId", "getPetById", responseType = Some("Pet"), arguments = Seq(CallArgument("petId", "long")))
    val updatePet = Call(Method.PUT, "/v2/pet", "updatePet", requestType = Some("Pet"))
    val updatePetWithFor = Call(Method.POST, "/v2/pet/:petId", "updatePetWithForm", arguments = Seq(CallArgument("petId", "long")))

    val mock = Service("com.example.pet.api", "pet",
      Seq("Pet", "ModelApiResponse"),
      Seq(addPetCall, detelePet, findPetsByStatus, findPetsByTags, getPetById, updatePet, updatePetWithFor))

    val swaggerAsString = ResourceUtils.loadContents(inputStream)
    val swagger = new SwaggerParser().parse(swaggerAsString)
    import scala.collection.JavaConversions._
    val tags = (for {
      path <- swagger.getPaths.toMap.values
      operation <- path.getOperations.toIndexedSeq
      tag <- operation.getTags
    } yield {
      tag
    }).toSeq
    val serviceName = tags.groupBy(identity).mapValues(_.size).maxBy(_._2)._1
    mock.copy(name = serviceName)

  }

}
