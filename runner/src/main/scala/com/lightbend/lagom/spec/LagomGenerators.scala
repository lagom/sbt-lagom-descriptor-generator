package com.lightbend.lagom.spec

import java.io.InputStream

import com.lightbend.lagom.spec.parser.OpenApiV2Parser
import com.lightbend.lagom.spec.render.JavaLagomRender
import io.swagger.models.Swagger

object LagomGenerators {

  def openApiV2ToLagomJava(inputStream: InputStream, packageName: String, serviceName: String): LagomGeneratorTypes.Output = {
    val openApiV2 = new OpenApiV2Parser(packageName, serviceName)
    new LagomGenerator[Swagger] {}.generate(
      inputStream,
      openApiV2.parse,
      openApiV2.convert,
      identity,
      JavaLagomRender.render
    )
  }

}
