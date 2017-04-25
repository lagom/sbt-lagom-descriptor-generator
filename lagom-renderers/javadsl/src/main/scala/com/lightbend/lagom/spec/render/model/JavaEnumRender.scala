package com.lightbend.lagom.spec.render.model

import com.lightbend.lagom.spec.model.{ CustomEnum, Service }

/**
 *
 */
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
