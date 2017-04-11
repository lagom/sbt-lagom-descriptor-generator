package com.lightbend.lagom.spec

import io.swagger.models.Swagger
import org.scalatest.{ FlatSpec, Matchers }

class LagomGeneratorSpec extends FlatSpec with Matchers {

  private val folder = "/lagom-generator-spec"
  import ResourceUtils._

  behavior of "Descriptor Generator"

  it should "generate a Lagom Java Descriptor given an OpenAPI v2 file" in {

    val packageName = "com.example.pet.api"
    val inputStream = resource(s"$folder/swagger.json")

    val generated: String = LagomGenerators.swaggerV2ToLagomJava(inputStream, packageName)
    val expected: String = loadContents(s"$folder/sample-java-descriptor.txt")
    expected should be(generated)

  }

}
