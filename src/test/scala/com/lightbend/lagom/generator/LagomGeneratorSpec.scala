package com.lightbend.lagom.generator

import java.io.InputStream

import org.scalatest.{ FlatSpec, Matchers }

import scala.io.Source

class LagomGeneratorSpec extends FlatSpec with Matchers {

  private val folder = "/generator-spec"

  behavior of "Descriptor Generator"

  it should "generate a Lagom Java Descriptor given an OpenAPI v2 file" in {
    val generated: String = LagomGenerator.generateFor(resource(s"$folder/swagger.json"))
    val expected: String = loadContents(s"$folder/sample-java-descriptor.txt")
    expected should be(generated)

  }

  // -----------------------------------------------------------------------------------------------------------
  private def loadContents(name: String): String = {
    val source = Source.fromInputStream(resource(name))
    try {
      source.getLines().mkString("\n")
    } finally {
      source.close()
    }
  }

  private def resource(resourceName: String): InputStream = {
    val stream = getClass.getResourceAsStream(resourceName)
    if (stream == null) throw new IllegalArgumentException(s"Can't locate resource $resourceName. ")
    else stream
  }

}
