package com.lightbend.lagom.spec

import java.io.InputStream

import com.lightbend.lagom.spec.parser.OpenApiV2Parser
import com.lightbend.lagom.spec.render.JavaLagomDescriptorRender
import io.swagger.models.Swagger

object LagomGenerators {

  def swaggerV2ToLagomJava(inputStream: InputStream, packageName: String): LagomGeneratorTypes.Output = {
    val openApiV2 = new OpenApiV2Parser(packageName)
    new LagomGenerator[Swagger] {}.generate(
      inputStream,
      openApiV2.parse,
      openApiV2.convert,
      identity,
      JavaLagomDescriptorRender.render
    )
  }

}
