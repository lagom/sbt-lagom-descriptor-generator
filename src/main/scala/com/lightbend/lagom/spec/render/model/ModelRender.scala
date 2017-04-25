package com.lightbend.lagom.spec.render.model

import com.lightbend.lagom.spec.model.{ Service, _ }
import com.lightbend.lagom.spec.{ LagomGeneratorTypes, LagomGenerators }

trait ModelRender {
  def render: LagomGeneratorTypes.ModelRender
}

object JavaTypeRenderer {
  def renderType(t: Type, preferPrimitives: Boolean = true): String = t match {
    case LInt                     => if (preferPrimitives) "int" else "Integer"
    case LLong                    => if (preferPrimitives) "long" else "Long"
    case LFloat                   => if (preferPrimitives) "float" else "Float"
    case LDouble                  => if (preferPrimitives) "double" else "Double"
    case LChar                    => if (preferPrimitives) "char" else "Character"
    case LBoolean                 => if (preferPrimitives) "boolean" else "Boolean"
    case LString                  => "String"
    case LDate                    => "java.time.LocalDate"
    case LDateTime                => "java.time.Instant"
    case LSequence(of)            => s"org.pcollections.PSequence<${renderType(of, preferPrimitives = false)}>"
    case LSet(of)                 => s"org.pcollections.HashTreePSet<${renderType(of, preferPrimitives = false)}>"
    case LMap(keyType, valueType) => s"org.pcollections.HashTreePSet<${renderType(keyType, preferPrimitives = false)}, ${renderType(valueType, preferPrimitives = false)}>"
    case LOptional(of)            => s"java.util.Optional<${renderType(of, preferPrimitives = false)}>"
    case LUserDefined(name)       => s"$name"
    case LEnum(name, values)      => s"$name"
  }
}

object JavaModelRender extends ModelRender {

  def render: LagomGeneratorTypes.ModelRender = { (service, customModel) =>
    customModel match {
      case _: CustomEnum  => JavaEnumRender.render(service, customModel.asInstanceOf[CustomEnum])
      case _: CustomModel => JavaPojoRender.render(service, customModel.asInstanceOf[CustomModel])
    }

  }
}

object JavaEnumRender {
  val jacksonImports = "import com.fasterxml.jackson.annotation.*;"

  def packageDeclaration(service: Service): String = s"package ${service.`package`};"

  private def enumValues(customEnum: CustomEnum) = {
    customEnum.values.map(v => s"""    ${v.toUpperCase.replaceAll("-", "_")}("$v")""").mkString(",\n")
  }
  private def enumDefinition(customEnum: CustomEnum) = {
    s"""public enum ${customEnum.className} {
      |${enumValues(customEnum)};
      |
      |    private final String value;
      |
      |    ${customEnum.className}(String value) {
      |      this.value = value;
      |    }
      |
      |    @com.fasterxml.jackson.annotation.JsonValue
      |    public String getValue() {
      |      return value;
      |    }
      |
      |    @Override
      |    public String toString() {
      |      return String.valueOf(value);
      |    }
      |
      |    @com.fasterxml.jackson.annotation.JsonCreator
      |    public static ${customEnum.className} fromValue(String text) {
      |      for (${customEnum.className} b : ${customEnum.className}.values()) {
      |        if (String.valueOf(b.value).equals(text)) {
      |          return b;
      |        }
      |      }
      |      return null;
      |    }
      |  }
      |
    """.stripMargin

  }

  def render(service: Service, customEnum: CustomEnum): String = {
    s"""${packageDeclaration(service)}
       |
       |$jacksonImports
       |
       |${enumDefinition(customEnum)}
       |""".stripMargin.trim
  }
}

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
         |    return new Builder(this).with${capitalized(field.fieldName)}(${field.fieldName}).build();
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
      s"""@ApiModelProperty()
         |public ${renderType(field.fieldType)} get${capitalized(field.fieldName)}() {
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
       |    ${model.className} pet = (${model.className}) o;
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
       |@javax.annotation.Generated(value = "class ${LagomGenerators.getClass.getName}")
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
