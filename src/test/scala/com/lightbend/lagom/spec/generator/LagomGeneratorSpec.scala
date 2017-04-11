package com.lightbend.lagom.spec.generator

import java.io.InputStream

import com.lightbend.lagom.spec.ResourceUtils
import com.lightbend.lagom.spec.parser.OpenApiV2Parser
import io.swagger.models.Swagger
import org.scalatest.{ FlatSpec, Matchers }

class LagomGeneratorSpec extends FlatSpec with Matchers {

  private val folder = "/lagom-generator-spec"
  import ResourceUtils._

  behavior of "Descriptor Generator"

  it should "generate a Lagom Java Descriptor given an OpenAPI v2 file" in {

    val packageName = "com.example.pet.api"

    val openApiV2 = new OpenApiV2Parser(packageName)

    val parse: InputStream => Swagger = openApiV2.parse
    val convert: Swagger => Service = openApiV2.convert
    val filter: Service => Service = identity
    val generate: Service => String = JavaLagomDescriptorRender.render
    // val store: String => Unit

    val runTest: (InputStream) => String = parse andThen convert andThen filter andThen generate

    val inputStream = resource(s"$folder/swagger.json")

    val generated: String = runTest(inputStream)
    val expected: String = loadContents(s"$folder/sample-java-descriptor.txt")
    expected should be(generated)

  }

}
