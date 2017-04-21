package com.lightbend.lagom.spec

import org.scalatest.{ FlatSpec, Matchers }

class JavaCustomModelGeneratorSpec extends FlatSpec with Matchers {

  private val folder = "/java-model-generator-spec"
  import ResourceUtils._

  behavior of "Descriptor Generator"

  it should "generate a Lagom Java Descriptor given an OpenAPI v2 file" in {

    val packageName = "com.example.pet.api"
    val inputStream = resource(s"$folder/swagger.json")

    val models = LagomGenerators.swaggerV2ToLagomJava(inputStream, packageName).models
    val generated: String = models("Pet").fileContents
    val expected: String = loadContents(s"$folder/Pet.java.txt")
    expected should be(generated)

  }

}
