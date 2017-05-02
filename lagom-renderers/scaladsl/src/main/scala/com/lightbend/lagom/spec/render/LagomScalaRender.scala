package com.lightbend.lagom.spec.render

import java.io.File

import com.lightbend.lagom.spec.LagomGeneratorTypes
import com.lightbend.lagom.spec.LagomGeneratorTypes.{ GeneratedCode, Output }
import com.lightbend.lagom.spec.model.Service
import com.lightbend.lagom.spec.render.descriptor.ScalaLagomDescriptorRender
import com.lightbend.lagom.spec.render.model.ScalaModelRender

/**
 *
 */
object LagomScalaRender {

  val render: LagomGeneratorTypes.Render = { service =>
    val descriptorFileName = getPath(service, s"${service.interfaceName}")
    val descriptor: String = ScalaLagomDescriptorRender.render(service)

    // TODO: in scala it's not idiomatic to put one case class per file. Improve this.
    val customModels = service.customModels.map { model =>
      val content = ScalaModelRender.render(service, model)
      val code = GeneratedCode(getPath(service, model.className), content)
      (model.className, code)
    }.toMap

    Output(GeneratedCode(descriptorFileName, descriptor), customModels)
  }

  private def getPath(service: Service, className: String): String = {
    s"${service.`package`.replaceAll("\\.", File.separator)}${File.separator}${className}.scala"
  }
}
