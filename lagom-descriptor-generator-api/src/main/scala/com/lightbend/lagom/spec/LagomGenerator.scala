/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.spec

import java.io.{ File, InputStream }

import com.lightbend.lagom.spec.model.{ CustomType, Service }

object LagomGeneratorTypes {
  case class GeneratedCode(relativeFile: File, fileContents: String)
  case class Output(descriptor: GeneratedCode, models: Map[String, GeneratedCode])
  type ModelRender = (Service, CustomType) => String
  type DescriptorRender = Service => String
  type Render = Service => Output
  type Filter = Service => Service
}

trait LagomGenerator[Spec] {

  import LagomGeneratorTypes._

  type Parse = (InputStream) => Spec
  type Convert = Spec => Service

  def generate(
    inputStream: InputStream,
    parse: Parse,
    convert: Convert,
    filter: Filter,
    render: Render
  ): Output = {

    (parse andThen convert andThen filter andThen render)(inputStream)

  }

}

