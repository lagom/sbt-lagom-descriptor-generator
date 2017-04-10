package com.lightbend.lagom.spec.generator

import java.io.InputStream

import com.lightbend.lagom.spec.parser.OpenApiV2Parser

sealed trait LagomGenerator {
  def packageDeclaration(service: Service): String

  val lagomImports: String

  def customImports(service: Service): String

  def interfaceName(service: Service): String = s"${service.name.head.toUpper}${service.name.tail.map(_.toLower)}Api"

  def argument(arg: CallArgument): String

  def methodHandlers(calls: Seq[Call]): String

  def callDescription(call: Call): String

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

object LagomJavaGenerator extends LagomGenerator {

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

  override def callDescription(call: Call): String =
    s"""                restCall(Method.${call.method.name}, "${call.path}", this::${call.name})"""

  override def descriptor(service: Service) = {
    val withCalls =
      if (service.calls.nonEmpty)
        service.calls
          .map(callDescription)
          .mkString(".withCalls(\n", ",\n", "\n        )")
      else
        ""

    s"""named("${service.name}")$withCalls;"""
  }

  private def tabs(count: Int)(input: String): String = " " * count + input

  override def serviceDefinition(service: Service) =
    s"""
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

    val service = OpenApiV2Parser.parse(inputStream)

    LagomJavaGenerator.generate(service)

  }

}

