package com.lightbend.lagom.spec.render.descriptor

import com.lightbend.lagom.spec.LagomGeneratorTypes
import com.lightbend.lagom.spec.model.{ Call, CallArgument, Service }

trait LagomDescriptorRender {
  def packageDeclaration(service: Service): String

  val lagomImports: String

  def customImports(service: Service): String

  def interfaceName(service: Service): String = service.interfaceName

  def argument(arg: CallArgument): String

  def methodHandlers(calls: Seq[Call]): String

  def callDescription(call: Call): String

  def descriptor(service: Service): String

  def serviceDefinition(service: Service): String

  final val render: LagomGeneratorTypes.DescriptorRender = { service: Service =>
    s"""${packageDeclaration(service)}
       |
       |$lagomImports
       |${customImports(service)}
       |
       |${serviceDefinition(service)}
       |""".stripMargin.trim
  }

  protected def importWriter(fqcns: Set[String]): String

}

