/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.spec.render

import java.io.File
import java.util.regex.Matcher

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

  /**
   * @return a relative path to the file.
   */
  private def getPath(service: Service, className: String): File = {
    val packageSplits = service.`package`.split("\\.")
    val directory: File = packageSplits.tail.foldLeft(new File(packageSplits.head))((acc, x) => new File(acc, x))
    new File(directory, s"${className}.scala")
  }

}
