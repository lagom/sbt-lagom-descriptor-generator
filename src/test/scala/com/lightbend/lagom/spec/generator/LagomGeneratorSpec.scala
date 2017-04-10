package com.lightbend.lagom.spec.generator

import com.lightbend.lagom.spec.ResourceUtils
import org.scalatest.{ FlatSpec, Matchers }

class LagomGeneratorSpec extends FlatSpec with Matchers {

  private val folder = "/lagom-generator-spec"
  import ResourceUtils._

  behavior of "Descriptor Generator"

  it should "generate a Lagom Java Descriptor given an OpenAPI v2 file" in {
    val generated: String = LagomGenerator.generateFor(resource(s"$folder/swagger.json"))
    val expected: String = loadContents(s"$folder/sample-java-descriptor.txt")
    expected should be(generated)

  }

}
