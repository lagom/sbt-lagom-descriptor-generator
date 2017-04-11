package com.lightbend.lagom.spec

import java.io.InputStream

import com.lightbend.lagom.spec.model.Service

object LagomGeneratorTypes {
  type Output = String
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

