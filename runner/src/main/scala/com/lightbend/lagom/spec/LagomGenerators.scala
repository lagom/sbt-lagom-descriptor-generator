/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.spec

import java.io.InputStream

import com.lightbend.lagom.spec.parser.OpenApiV2Parser
import com.lightbend.lagom.spec.render.{ LagomJavaRender, LagomScalaRender }
import io.swagger.models.Swagger

object LagomGenerators {

  def openApiV2ToLagomJava(inputStream: InputStream, packageName: String, serviceName: String): LagomGeneratorTypes.Output = {
    val openApiV2 = new OpenApiV2Parser(packageName, serviceName)
    new LagomGenerator[Swagger] {}.generate(
      inputStream,
      openApiV2.parse,
      openApiV2.convert,
      identity,
      LagomJavaRender.render
    )
  }
  def openApiV2ToLagomScala(inputStream: InputStream, packageName: String, serviceName: String): LagomGeneratorTypes.Output = {
    val openApiV2 = new OpenApiV2Parser(packageName, serviceName)
    new LagomGenerator[Swagger] {}.generate(
      inputStream,
      openApiV2.parse,
      openApiV2.convert,
      identity,
      LagomScalaRender.render
    )
  }

}
