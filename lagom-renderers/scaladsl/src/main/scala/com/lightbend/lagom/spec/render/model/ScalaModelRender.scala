package com.lightbend.lagom.spec.render.model

import com.lightbend.lagom.spec.LagomGeneratorTypes
import com.lightbend.lagom.spec.model.{ CustomEnum, CustomModel, ModelField, Service }

object ScalaModelRender {
  private def renderPackage(service: Service) = s"package ${service.`package`}"
  private val renderCommonImports = "import play.api.libs.json._"

  private def renderEnum(service: Service, model: CustomEnum): String =
    s"""${renderPackage(service)}
       |
       |$renderCommonImports
       |
       |object ${model.className} extends Enumeration {
       |    val ${model.values.mkString(", ")} = Value
       |    type ${model.className} = Value
       |
       |    implicit val format: Format[Value] = Format(Reads.enumNameReads(this), Writes.enumNameWrites[${model.className}.type])
       |
       |}
    """.stripMargin

  private def renderField(field: ModelField): String = s"""`${field.fieldName}`: ${ScalaTypeRender.renderType(field.fieldType)}"""

  private def renderCaseClass(service: Service, model: CustomModel): String =
    s"""${renderPackage(service)}
       |
       |$renderCommonImports
       |
       |case class ${model.className}(${model.fields.map(renderField).mkString(", ")})
       |
       |object ${model.className} {
       |    implicit val format: Format[${model.className}] = Json.format
       |}
    """.stripMargin

  def render: LagomGeneratorTypes.ModelRender = { (service, customModel) =>
    customModel match {
      case _: CustomEnum  => renderEnum(service, customModel.asInstanceOf[CustomEnum])
      case _: CustomModel => renderCaseClass(service, customModel.asInstanceOf[CustomModel])
    }

  }

}