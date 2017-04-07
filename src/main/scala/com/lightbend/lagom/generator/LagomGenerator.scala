package com.lightbend.lagom.generator

import java.io.InputStream

sealed trait LagomGenerator {
  val service: Service
  val packageDeclaration: String
  val lagomImports: String
  val customImports: String

  val interfaceName = s"${service.name.head.toUpper}${service.name.tail.map(_.toLower)}Api"

  def argument(arg: CallArgument): String
  val methodHandlers: String
  val callDescriptions: String
  val descriptor: String
  val serviceDefinition: String

  def generate: String =
    s"""$packageDeclaration
       |
       |$lagomImports
       |$customImports
       |
       |$serviceDefinition
       |""".stripMargin.trim

  protected def importWriter(fqcns: Seq[String]): String

}

final class LagomJavaLagomGenerator(val service: Service) extends LagomGenerator {

  override val packageDeclaration: String = s"package ${service.`package`};"

  override val lagomImports: String = importWriter(Seq(
    "static com.lightbend.lagom.javadsl.api.Service.*",
    "com.lightbend.lagom.javadsl.api.*",
    "com.lightbend.lagom.javadsl.api.transport.*"
  ))
  override val customImports: String = importWriter(service.customModels.map(name => s"${service.`package`}.$name"))

  override def argument(arg: CallArgument): String = s"${arg.`type`} ${arg.name}"
  val methodHandlers: String = service.calls.map { call =>
    val req = call.requestType.getOrElse("akka.NotUsed")
    val resp = call.responseType.getOrElse("akka.Done")
    val args = call.arguments.map(argument).mkString(", ")
    s"ServiceCall<$req, $resp> ${call.name}($args);"
  }
    .map { x => s"    $x" }
    .mkString("\n")

  override val callDescriptions = service.calls.map { call =>
    s"""                restCall(Method.${call.method.name}, "${call.path}", this::${call.name})"""
  }.mkString(",\n")

  override val descriptor = s"""named("${service.name}").withCalls(
     |$callDescriptions
     |        );
      """.stripMargin.trim

  override val serviceDefinition = s"""
     |public interface $interfaceName extends Service {
     |
     |$methodHandlers
     |
     |    default Descriptor descriptor() {
     |        return $descriptor
     |    }
     |}""".stripMargin.trim

  override def importWriter(fqcns: Seq[String]): String = {
    fqcns.map(fqcn => s"import $fqcn;").mkString("\n")
  }

}

object LagomGenerator {

  def generateFor(inputStream: InputStream): String = {

    val addPetCall = Call(Method.POST, "/v2/pet", "addPet", requestType = Some("Pet"))
    val detelePet = Call(Method.DELETE, "/v2/pet/:petId", "deletePet", arguments = Seq(CallArgument("petId", "long")))
    val findPetsByStatus = Call(Method.GET, "/v2/pet/findByStatus?status", "findPetsByStatus", responseType = Some("org.pcollections.PSequence<Pet>"), arguments = Seq(CallArgument("status", "org.pcollections.PSequence<String>")))
    val findPetsByTags = Call(Method.GET, "/v2/pet/findByTags?tags", "findPetsByTags", responseType = Some("org.pcollections.PSequence<Pet>"), arguments = Seq(CallArgument("tags", "org.pcollections.PSequence<String>")))
    val getPetById = Call(Method.GET, "/v2/pet/:petId", "getPetById", responseType = Some("Pet"), arguments = Seq(CallArgument("petId", "long")))
    val updatePet = Call(Method.PUT, "/v2/pet", "updatePet", requestType = Some("Pet"))
    val updatePetWithFor = Call(Method.POST, "/v2/pet/:petId", "updatePetWithForm", arguments = Seq(CallArgument("petId", "long")))

    val service = Service("com.example.pet.api", "pet",
      Seq("Pet", "ModelApiResponse"),
      Seq(addPetCall, detelePet, findPetsByStatus, findPetsByTags, getPetById, updatePet, updatePetWithFor))

    val generator = new LagomJavaLagomGenerator(service)

    generator.generate
  }

}

