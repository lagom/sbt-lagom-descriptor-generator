package com.lightbend.lagom.spec.render

import com.lightbend.lagom.spec.model.{ Call, CallArgument, Service }

object ScalaLagomDescriptorRender extends LagomDescriptorRender {
  override def packageDeclaration(service: Service): String = ???

  override val lagomImports: String = ???

  override def customImports(service: Service): String = ???

  override def argument(arg: CallArgument): String = ???

  override def methodHandlers(calls: Seq[Call]): String = ???

  override def callDescription(call: Call): String = ???

  override def descriptor(service: Service): String = ???

  override def serviceDefinition(service: Service): String = ???

  override protected def importWriter(fqcns: Seq[String]): String = ???
}
