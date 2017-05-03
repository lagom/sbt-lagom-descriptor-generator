/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.spec.render.descriptor

import com.lightbend.lagom.spec.model._
import com.lightbend.lagom.spec.render.LagomDescriptorRender
import com.lightbend.lagom.spec.render.model.ScalaTypeRender

object ScalaLagomDescriptorRender extends LagomDescriptorRender {
  override def packageDeclaration(service: Service): String = s"package ${service.`package`}"

  override protected def importWriter(fqcns: Set[String]): String =
    fqcns.map(x => s"import $x").mkString("\n")

  override val lagomImports: String = importWriter(Set(
    "com.lightbend.lagom.scaladsl.api.Service._",
    "com.lightbend.lagom.scaladsl.api._",
    "com.lightbend.lagom.scaladsl.api.transport._"
  ))

  override def customImports(service: Service): String = ""

  import ScalaTypeRender.renderType

  override def argument(arg: CallArgument): String = s"${arg.name}: ${renderType(arg.`type`)}"

  override def methodHandlers(calls: Seq[Call]): String = calls.map { call =>
    val req = call.requestType.map(rt => renderType(rt)).getOrElse("akka.NotUsed")
    val resp = call.responseType.map(rt => renderType(rt)).getOrElse("akka.Done")
    val args = call.arguments.map(argument).mkString(", ")
    s"def ${call.name}($args): ServiceCall[$req, $resp]"
  }
    .map { x => s"    $x" }
    .mkString("\n")

  override def callDescription(call: Call): String =
    s"""                restCall(Method.${call.method.name}, "${call.path}", ${call.name} _)"""

  override def descriptor(service: Service): String = {
    val withCalls = {
      if (service.calls.nonEmpty)
        service.calls
          .map(callDescription)
          .mkString(".withCalls(\n", ",\n", "\n        )")
      else
        ""
    }

    val withAutoAcl = if (service.autoAcl) ".withAutoAcl(true)" else ""

    s"""named("${service.name}")$withCalls$withAutoAcl"""
  }

  private def enumPathParamSerializers(calls: Seq[Call]): String =
    calls
      .flatMap(_.arguments)
      .map {
        // TODO: support pathParamSerializers for Seq[T], Set[T] and Map[T,R]
        case CallArgument(name, LSequence(t)) => CallArgument(name, t)
        case x                                => x
      }
      .filter {
        _.`type` match {
          case x: LEnum => true
          case _        => false
        }
      }
      .toSet[CallArgument]
      .map { ca =>
        val theType = ca.`type`.asInstanceOf[LEnum]
        s"""    implicit val pathParamSerializer: PathParamSerializer[${theType.name}] = PathParamSerializer.required("${ca.name}")(${theType.name}.withName)(_.toString)"""
      }.mkString("\n")

  override def serviceDefinition(service: Service): String =
    s"""
       |trait ${interfaceName(service)} extends Service {
       |
       |${methodHandlers(service.calls)}
       |
       |    final override def descriptor: Descriptor = {
       |        ${descriptor(service)}
       |    }
       |
       |${enumPathParamSerializers(service.calls)}
       |
       |}""".stripMargin.trim

}
