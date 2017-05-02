package com.lightbend.lagom.spec

import org.scalatest.{ FlatSpec, Matchers }

class LagomScalaDescriptorSpec extends FlatSpec with Matchers {

  private val folder = "/lagom-scala-descriptor-spec"
  import ResourceUtils._

  behavior of "Descriptor Generator"

  it should "generate a Lagom Scala Descriptor given an OpenAPI v2 file" in {

    val packageName = "com.example.pet.api"
    val inputStream = resource(s"$folder/swagger.json")

    val generatedCode: String = LagomGenerators.openApiV2ToLagomScala(inputStream, packageName, "pet").descriptor.fileContents
    val expected: String = loadContents(s"$folder/sample-scala-descriptor.txt")
    expected should be(generatedCode)

  }

}
