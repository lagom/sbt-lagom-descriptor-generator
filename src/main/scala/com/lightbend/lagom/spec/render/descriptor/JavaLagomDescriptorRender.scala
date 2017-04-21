package com.lightbend.lagom.spec.render.descriptor

import com.lightbend.lagom.spec.model.{ Call, CallArgument, Service }

/**
 *
 */
object JavaLagomDescriptorRender extends LagomDescriptorRender {

  override def packageDeclaration(service: Service): String = s"package ${service.`package`};"

  override val lagomImports: String = importWriter(Seq(
    "static com.lightbend.lagom.javadsl.api.Service.*",
    "com.lightbend.lagom.javadsl.api.*",
    "com.lightbend.lagom.javadsl.api.transport.*"
  ))

  override def customImports(service: Service): String = importWriter(service.customModels.map(customModel => s"${service.`package`}.${customModel.className}"))

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
    val withCalls = {
      if (service.calls.nonEmpty)
        service.calls
          .map(callDescription)
          .mkString(".withCalls(\n", ",\n", "\n        )")
      else
        ""
    }

    val withAutoAcl = if (service.autoAcl) ".withAutoAcl(true)" else ""

    s"""named("${service.name}")$withCalls$withAutoAcl;"""
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
