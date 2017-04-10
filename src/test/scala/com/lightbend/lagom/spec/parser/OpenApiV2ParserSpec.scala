package com.lightbend.lagom.spec.parser

import com.lightbend.lagom.spec.ResourceUtils.resource
import org.scalatest.{ FlatSpec, Matchers }

class OpenApiV2ParserSpec extends FlatSpec with Matchers {
  private val folder = "/openapi-v2-parser"

  behavior of "OpenApiV2Parser"

  it should "read the service name from the path tag" in {
    val openApiV2Parser = new OpenApiV2Parser("com.example")
    val service = openApiV2Parser.read(resource(s"$folder/swagger.json"))
    service.name should be("pet")
  }

  it should "use the package provided as input" in {
    val packageName = "com.example.spec.pet"
    val openApiV2Parser = new OpenApiV2Parser(packageName)
    val service = openApiV2Parser.read(resource(s"$folder/swagger.json"))
    service.`package` should be(packageName)
  }

  it should "read as many calls as specified" in {
    val openApiV2Parser = new OpenApiV2Parser("com.example.spec.pet")
    val service = openApiV2Parser.read(resource(s"$folder/swagger.json"))
    service.calls.size should be(7)
  }

  it should "parse Swagger's paths into Lagom paths" in {
    val openApiV2Parser = new OpenApiV2Parser("com.example.spec.pet")
    val service = openApiV2Parser.read(resource(s"$folder/swagger.json"))
    service.calls.map { _.path }.filter(_.contains(":")).toSet should be(Set("/v2/pet/:petId"))
  }

  it should "parse Swagger paths with query parameters into Lagom paths" in {
    val openApiV2Parser = new OpenApiV2Parser("com.example.spec.pet")
    val service = openApiV2Parser.read(resource(s"$folder/swagger.json"))
    service.calls.map { _.path }.filter(_.contains("findBy")).toSet should be(Set("/v2/pet/findByStatus?status", "/v2/pet/findByTags?tags"))
  }

}
