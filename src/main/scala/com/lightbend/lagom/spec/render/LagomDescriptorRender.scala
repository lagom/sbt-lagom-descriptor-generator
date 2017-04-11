package com.lightbend.lagom.spec.render

import com.lightbend.lagom.spec.LagomGeneratorTypes
import com.lightbend.lagom.spec.model.{ Call, CallArgument, Service }

trait LagomDescriptorRender {
  def packageDeclaration(service: Service): String

  val lagomImports: String

  def customImports(service: Service): String

  def interfaceName(service: Service): String = s"${service.name.head.toUpper}${service.name.tail.map(_.toLower)}Api"

  def argument(arg: CallArgument): String

  def methodHandlers(calls: Seq[Call]): String

  def callDescription(call: Call): String

  def descriptor(service: Service): String

  def serviceDefinition(service: Service): String

  final val render: LagomGeneratorTypes.Render = { service: Service =>
    s"""${packageDeclaration(service)}
       |
       |$lagomImports
       |${customImports(service)}
       |
       |${serviceDefinition(service)}
       |""".stripMargin.trim
  }

  protected def importWriter(fqcns: Seq[String]): String

}

