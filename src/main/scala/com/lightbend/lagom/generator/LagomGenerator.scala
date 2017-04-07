package com.lightbend.lagom.generator

import java.io.InputStream

sealed trait LagomGenerator {
  def packageDeclaration(service: Service): String
  val lagomImports: String
  def customImports(service: Service): String

  def interfaceName(service: Service): String = s"${service.name.head.toUpper}${service.name.tail.map(_.toLower)}Api"

  def argument(arg: CallArgument): String
  def methodHandlers(calls: Seq[Call]): String
  def callDescriptions(calls: Seq[Call]): String
  def descriptor(service: Service): String
  def serviceDefinition(service: Service): String

  def generate(service: Service): String =
    s"""${packageDeclaration(service)}
       |
       |$lagomImports
       |${customImports(service)}
       |
       |${serviceDefinition(service)}
       |""".stripMargin.trim

  protected def importWriter(fqcns: Seq[String]): String

}

final object LagomJavaGenerator extends LagomGenerator {

  override def packageDeclaration(service: Service): String = s"package ${service.`package`};"

  override val lagomImports: String = importWriter(Seq(
    "static com.lightbend.lagom.javadsl.api.Service.*",
    "com.lightbend.lagom.javadsl.api.*",
    "com.lightbend.lagom.javadsl.api.transport.*"
  ))
  override def customImports(service: Service): String = importWriter(service.customModels.map(name => s"${service.`package`}.$name"))
  override def argument(arg: CallArgument): String = s"${arg.`type`} ${arg.name}"
  override def methodHandlers(calls: Seq[Call]): String = calls.map { call =>
    val req = call.requestType.getOrElse("akka.NotUsed")
    val resp = call.responseType.getOrElse("akka.Done")
    val args = call.arguments.map(argument).mkString(", ")
    s"ServiceCall<$req, $resp> ${call.name}($args);"
  }
    .map { x => s"    $x" }
    .mkString("\n")

  override def callDescriptions(calls: Seq[Call]): String = calls.map { call =>
    s"""                restCall(Method.${call.method.name}, "${call.path}", this::${call.name})"""
  }.mkString(",\n")

  override def descriptor(service: Service) = s"""named("${service.name}").withCalls(
     |${callDescriptions(service.calls)}
     |        );
      """.stripMargin.trim

  override def serviceDefinition(service: Service) = s"""
     |public interface ${interfaceName(service)} extends Service {
     |
     |${methodHandlers(service.calls)}
     |
     |    default Descriptor descriptor() {
     |        return ${descriptor(service)}
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

    LagomJavaGenerator.generate(service)

  }

}

