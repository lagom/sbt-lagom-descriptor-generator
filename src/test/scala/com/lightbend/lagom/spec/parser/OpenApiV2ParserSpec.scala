package com.lightbend.lagom.spec.parser

import com.lightbend.lagom.spec.ResourceUtils
import com.lightbend.lagom.spec.ResourceUtils.resource
import org.scalatest.{ FlatSpec, Matchers }
import ResourceUtils._

class OpenApiV2ParserSpec extends FlatSpec with Matchers {
  private val folder = "/openapi-v2-parser"

  behavior of "OpenApiV2Parser"

  it should "read the service name from the path tag" in {
    val service = OpenApiV2Parser.parse(resource(s"$folder/swagger.json"))
    service.name should be("pet")
  }
}
