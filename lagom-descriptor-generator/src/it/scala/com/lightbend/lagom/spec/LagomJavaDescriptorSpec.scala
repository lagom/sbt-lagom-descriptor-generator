package com.lightbend.lagom.spec

import com.lightbend.lagom.spec.LagomGeneratorTypes.GeneratedCode
import org.apache.commons.lang3.StringUtils
import org.scalatest.{FlatSpec, Matchers}

class LagomJavaDescriptorSpec extends FlatSpec with Matchers {

  private val folder = "/lagom-java-descriptor-spec"
  import ResourceUtils._

  behavior of "Descriptor Generator"

  it should "generate a Lagom Java Descriptor given an OpenAPI v2 file" in {

    val packageName = "com.example.pet.api"
    val inputStream = resource(s"$folder/swagger.json")

    val descriptor = LagomGenerators.openApiV2ToLagomJava(inputStream, packageName, "pet").descriptor
    val generatedCode: String = StringUtils.normalizeSpace(descriptor.fileContents)
    val expected: String = StringUtils.normalizeSpace(loadContents(s"$folder/sample-java-descriptor.txt"))
    expected should be(generatedCode)

  }

}
