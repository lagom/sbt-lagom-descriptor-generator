package com.lightbend.lagom.generator

import org.scalatest.{ FlatSpec, Matchers }

/**
 *
 */
class LagomJavaGeneratorSpec extends FlatSpec with Matchers {

  behavior of "Lagom Java Generator"

  it should "generate a package statement" in {
    val service = Service("com.example", "")
    LagomJavaGenerator.packageDeclaration(service) should ===("package com.example;")
  }

  it should "include javadsl imports" in {
    LagomJavaGenerator.lagomImports.split("\n").toSeq should ===(
      Seq(
        "import static com.lightbend.lagom.javadsl.api.Service.*;",
        "import com.lightbend.lagom.javadsl.api.*;",
        "import com.lightbend.lagom.javadsl.api.transport.*;"
      )
    )
  }

  it should "include custom imports when there are some" in {
    val service = Service("com.example", "", Seq("Foo", "Bar"))
    LagomJavaGenerator.customImports(service).split("\n").toSeq should ===(
      Seq(
        "import com.example.Foo;",
        "import com.example.Bar;"
      )
    )
  }

  it should "not include custom imports when there are none" in {
    val service = Service("com.example", "aa")
    LagomJavaGenerator.customImports(service) should ===("")
  }

  // TODO: naming the interfaces will get trickier as we go...
  it should "CamelCase the name and append 'Api' for the interfaceName" in {
    val service = Service("", "soMetHing")
    LagomJavaGenerator.interfaceName(service) should ===("SomethingApi")
  }

  it should "generate a method Handler with 4-spaces of indentation" in {
    val call = Call(Method.GET, "", "")
    val handler = LagomJavaGenerator.methodHandlers(Seq(call))
    handler should include("    ServiceCall<")
  }

  it should "generate a method Handler with request and response types defaulting to akka.{NotUsed,Done}" in {
    val call = Call(Method.GET, "", "")
    val handler = LagomJavaGenerator.methodHandlers(Seq(call))
    handler should include("ServiceCall<akka.NotUsed, akka.Done>")
  }

  it should "generate a method Handler with correct request and response types" in {
    val call = Call(Method.GET, "", "", Some("String"), Some("Integer"))
    val handler = LagomJavaGenerator.methodHandlers(Seq(call))
    handler should include("ServiceCall<String, Integer>")
  }

  // TODO: should the generator be the normaliser of the method name?
  it should "generate a method Handler with the provided method name" in {
    val call = Call(Method.GET, "", "asdf")
    val handler = LagomJavaGenerator.methodHandlers(Seq(call))
    handler should include(" asdf(")
  }

  it should "generate a method Handler without arguments" in {
    val call = Call(Method.GET, "", "asdf")
    val handler = LagomJavaGenerator.methodHandlers(Seq(call))
    handler should include("();")
  }

  it should "generate a method Handler without many arguments" in {
    val call = Call(Method.GET, "", "asdf", arguments = Seq(CallArgument("id", "long"), CallArgument("name", "String")))
    val handler = LagomJavaGenerator.methodHandlers(Seq(call))
    handler should include("(long id, String name);")
  }

  // TODO: continue unit tests with call descriptors
  // TODO: continue unit tests with call description (assert calls section is added or not)

}
