package com.lightbend.lagom.spec.render

import com.lightbend.lagom.spec.model._
import com.lightbend.lagom.spec.render.descriptor.JavaLagomDescriptorRender
import org.scalatest.{ FlatSpec, Matchers }

/**
 *
 */
class JavaLagomDescriptorRenderSpec extends FlatSpec with Matchers {

  behavior of "Lagom Java Generator"

  it should "generate a package statement" in {
    val service = Service("com.example", "name")
    JavaLagomDescriptorRender.packageDeclaration(service) should ===("package com.example;")
  }

  it should "include javadsl imports" in {
    JavaLagomDescriptorRender.lagomImports.split("\n").toSeq should ===(
      Seq(
        "import static com.lightbend.lagom.javadsl.api.Service.*;",
        "import com.lightbend.lagom.javadsl.api.*;",
        "import com.lightbend.lagom.javadsl.api.transport.*;"
      )
    )
  }

  it should "include custom imports when there are some" in {
    val models: Set[CustomType] = Set("Foo", "Bar").map { str => CustomModel(str) }
    val service = Service("com.example", "name", customModels = models)
    JavaLagomDescriptorRender.customImports(service).split("\n").toSeq should ===(
      Seq(
        "import com.example.Foo;",
        "import com.example.Bar;"
      )
    )
  }

  it should "not include custom imports when there are none" in {
    val service = Service("com.example", "aa")
    JavaLagomDescriptorRender.customImports(service) should ===("")
  }

  // TODO: naming the interfaces will get trickier as we go...
  it should "CamelCase the name and append 'Api' for the interfaceName" in {
    val service = Service("", "soMetHing")
    JavaLagomDescriptorRender.interfaceName(service) should ===("SomethingApi")
  }

  it should "generate a method Handler with 4-spaces of indentation" in {
    val call = Call(Method.GET, "", "")
    val handler = JavaLagomDescriptorRender.methodHandlers(Seq(call))
    handler should include("    ServiceCall<")
  }

  it should "generate a method Handler with request and response types defaulting to akka.{NotUsed,Done}" in {
    val call = Call(Method.GET, "", "")
    val handler = JavaLagomDescriptorRender.methodHandlers(Seq(call))
    handler should include("ServiceCall<akka.NotUsed, akka.Done>")
  }

  it should "generate a method Handler with correct request and response types" in {
    val call = Call(Method.GET, "", "", Some(LString), Some(LInt))
    val handler = JavaLagomDescriptorRender.methodHandlers(Seq(call))
    handler should include("ServiceCall<String, Integer>")
  }

  // TODO: should the generator be the normaliser of the method name?
  it should "generate a method Handler with the provided method name" in {
    val call = Call(Method.GET, "", "asdf")
    val handler = JavaLagomDescriptorRender.methodHandlers(Seq(call))
    handler should include(" asdf(")
  }

  it should "generate a method Handler without arguments" in {
    val call = Call(Method.GET, "", "asdf")
    val handler = JavaLagomDescriptorRender.methodHandlers(Seq(call))
    handler should include("();")
  }

  it should "generate a method Handler without many arguments" in {
    val call = Call(Method.GET, "", "asdf", arguments = Seq(CallArgument("id", LLong), CallArgument("name", LString)))
    val handler = JavaLagomDescriptorRender.methodHandlers(Seq(call))
    handler should include("(long id, String name);")
  }

  it should s"generate a method call with the proper HTTP method" in {
    val call = Call(Method.GET, "", "")
    val handler = JavaLagomDescriptorRender.callDescription(call)
    handler should include("Method.GET")
  }

  it should s"generate a method call for the provided path" in {
    val path = "/some/path"
    val call = Call(Method.GET, path, "")
    val handler = JavaLagomDescriptorRender.callDescription(call)
    handler should include(path)
  }

  it should s"use the generated method handler" in {
    val handleName = "handleName"
    val call = Call(Method.GET, "/some/path", handleName)
    val handler = JavaLagomDescriptorRender.callDescription(call)
    handler should include(handleName)
  }

  it should s"include calls if there are any" in {
    val call = Call(Method.GET, "/path", "methodHandler")
    val service = Service("com.example", "name", calls = Seq(call))

    val handler = JavaLagomDescriptorRender.descriptor(service)
    handler should include("withCalls")
  }

  it should s"not include calls if there are none" in {
    val service = Service("com.example", "name")

    val handler = JavaLagomDescriptorRender.descriptor(service)
    handler should not include "withCalls"
  }

}
