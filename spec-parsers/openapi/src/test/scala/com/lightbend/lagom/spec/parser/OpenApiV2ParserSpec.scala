package com.lightbend.lagom.spec.parser

import com.lightbend.lagom.spec.ResourceUtils.resource
import com.lightbend.lagom.spec.model._
import org.scalatest.{ FlatSpec, Matchers }

class OpenApiV2ParserSpec extends FlatSpec with Matchers {
  private val folder = "/openapi-v2-parser"
  private val packageName = "com.example.spec.pet"
  private val openApiV2Parser = new OpenApiV2Parser(packageName, "pet")
  private val service = openApiV2Parser.read(resource(s"$folder/swagger.json"))

  behavior of "OpenApiV2Parser"

  it should "read the service name from the path tag" in {
    service.name should be("pet")
  }

  it should "use the package provided as input" in {
    service.`package` should be(packageName)
  }

  it should "read as many calls as specified" in {
    service.calls.size should be(7)
  }

  it should "parse Swagger's paths into Lagom paths" in {
    service.calls.map { _.path }.filter(_.contains(":")).toSet should be(Set("/v2/pet/:petId"))
  }

  it should "parse Swagger paths with query parameters into Lagom paths" in {
    service.calls.map { _.path }.filter(_.contains("findBy")).toSet should be(Set("/v2/pet/findByStatus?status", "/v2/pet/findByTags?tags"))
  }

  it should "parse type of request" in {
    service.calls.find(_.name == "addPet").flatMap { _.requestType } should be(Some(LUserDefined("Pet")))
  }

  it should "default type of request to None" in {
    service.calls.find(_.name == "getByPetId").flatMap { _.requestType } should be(None)
  }

  it should "parse type of response" in {
    service.calls.find(_.name == "getPetById").flatMap { _.responseType } should be(Some(LUserDefined("Pet")))
  }

  it should "default type of response to None" in {
    service.calls.find(_.name == "updatePet").flatMap { _.responseType } should be(None)
  }

  it should "parse array response types" in {
    service.calls.find(_.name == "findPetsByTags").flatMap { _.responseType } should be(Some(LSequence(LUserDefined("Pet"))))
  }

  it should "parse call arguments name" in {
    val getPetById = service.calls.find(_.name == "getPetById").get
    getPetById.arguments.head should be(CallArgument("petId", LLong))
  }

}
