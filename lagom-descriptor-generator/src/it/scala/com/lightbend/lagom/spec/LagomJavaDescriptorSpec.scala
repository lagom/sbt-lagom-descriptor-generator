package com.lightbend.lagom.spec

import org.scalatest.{ FlatSpec, Matchers }

class LagomJavaDescriptorSpec extends FlatSpec with Matchers {

  private val folder = "/lagom-java-descriptor-spec"
  import ResourceUtils._

  behavior of "Descriptor Generator"

  it should "generate a Lagom Java Descriptor given an OpenAPI v2 file" in {

    val packageName = "com.example.pet.api"
    val inputStream = resource(s"$folder/swagger.json")

    val generatedCode: String = LagomGenerators.openApiV2ToLagomJava(inputStream, packageName, "pet").descriptor.fileContents
    val expected: String = loadContents(s"$folder/sample-java-descriptor.txt")
    expected should be(generatedCode)

  }

}
