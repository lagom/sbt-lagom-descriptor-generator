package com.lightbend.lagom.spec.render.model

import com.lightbend.lagom.spec.model.{ CustomModel, Service }

/**
 *
 */
object JavaPojoRender {
  val jacksonImports = "import com.fasterxml.jackson.annotation.*;"

  def packageDeclaration(service: Service): String = s"package ${service.`package`};"

  import JavaTypeRenderer.renderType

  private def fields(model: CustomModel): String = {
    model.fields.map { field =>
      s"""@JsonProperty("${field.fieldName}")
         |private final ${renderType(field.fieldType)} ${field.fieldName};"""
        .stripMargin
    }.map {
      _.replaceAll("\n", "\n    ")
    }.mkString("    ", "\n\n    ", "\n")
  }

  private def capitalized(name: String) = s"${name.head.toUpper}${name.tail}"

  private def withers(className: String, model: CustomModel): String = {
    model.fields.map { field =>
      s"""public $className with${capitalized(field.fieldName)}(${renderType(field.fieldType)} ${field.fieldName}) {
         |    return new $className(${model.fields.map(_.fieldName).mkString(", ")});
         |}"""
        .stripMargin
    }.map {
      _.replaceAll("\n", "\n    ")
    }.mkString("    ", "\n\n    ", "\n")
  }

  private def getters(model: CustomModel): String = {
    // TODO: add annotation params wrt required, default value, ...
    // TODO: boolean feilds should use isXXX() method names
    model.fields.map { field =>
      s"""public ${renderType(field.fieldType)} get${capitalized(field.fieldName)}() {
         |    return ${field.fieldName};
         |}"""
        .stripMargin
    }.map {
      _.replaceAll("\n", "\n    ")
    }.mkString("    ", "\n\n    ", "\n")
  }

  private def fieldEquals(varName: String, model: CustomModel): String = {
    model
      .fields
      .map { field => s"java.util.Objects.equals(this.${field.fieldName}, $varName.${field.fieldName})" }
      .mkString("", " &&\n            ", ";")
  }

  private def theEquals(model: CustomModel): String = {
    val varName = model.className.toLowerCase()
    s"""    @Override
       |public boolean equals(Object o) {
       |    if (this == o) {
       |        return true;
       |    }
       |    if (o == null || getClass() != o.getClass()) {
       |        return false;
       |    }
       |    ${model.className} ${model.className.toLowerCase} = (${model.className}) o;
       |    return ${fieldEquals(varName, model)}
       |}"""
      .stripMargin
      .replaceAll("\n", "\n    ")
  }

  private def theHashCode(model: CustomModel): String = {
    s"""
       |    @Override
       |    public int hashCode() {
       |        return java.util.Objects.hash(${model.fields.map(_.fieldName).mkString(", ")});
       |    }"""
      .stripMargin

  }

  private def theToString(model: CustomModel): String = {
    val eachField = model
      .fields
      .map { field => s"sj.add(toString(this.${field.fieldName}));" }
      .mkString("\n        ")
    s"""
       |    @Override
       |    public String toString() {
       |        java.util.StringJoiner sj = new java.util.StringJoiner(",", "(", ")");
       |        $eachField
       |        return sj.toString();
       |    }
       |
       |    private String toString(Object o) {
       |        if (o == null) {
       |            return "null";
       |        } else {
       |            return o.toString();
       |        }
       |    }""".stripMargin
  }

  private def constructor(model: CustomModel): String = {
    s"""    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
       |public ${model.className}(${model.fields.map { f => s"${renderType(f.fieldType)} ${f.fieldName}" }.mkString(", ")}) {
       |${model.fields.map { f => s"    this.${f.fieldName} = ${f.fieldName};" }.mkString("\n")}
       |}""".stripMargin
      .replaceAll("\n", "\n    ")
  }

  private def modelDefinition(service: Service, model: CustomModel): String =
    s"""
       |@javax.annotation.Generated(value = "class com.lightbend.lagom.spec.LagomGenerators")
       |public final class ${model.className} {
       |${fields(model)}
       |${constructor(model)}
       |
       |    //TODO: add plus() for sequences and nullable withers for optionals
       |
       |${withers(model.className, model)}
       |${getters(model)}
       |${theEquals(model)}
       |${theHashCode(model)}
       |${theToString(model)}
       |
       |}
    """.stripMargin.trim

  def render(service: Service, customModel: CustomModel): String = {
    s"""${packageDeclaration(service)}
       |
       |$jacksonImports
       |
       |${modelDefinition(service, customModel)}
       |""".stripMargin.trim
  }
}
