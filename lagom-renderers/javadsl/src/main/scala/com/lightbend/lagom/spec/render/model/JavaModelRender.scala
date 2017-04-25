package com.lightbend.lagom.spec.render.model

import com.lightbend.lagom.spec.LagomGeneratorTypes
import com.lightbend.lagom.spec.model.{ CustomEnum, CustomModel }
import com.lightbend.lagom.spec.render.ModelRender

/**
 *
 */
object JavaModelRender extends ModelRender {

  def render: LagomGeneratorTypes.ModelRender = { (service, customModel) =>
    customModel match {
      case _: CustomEnum  => JavaEnumRender.render(service, customModel.asInstanceOf[CustomEnum])
      case _: CustomModel => JavaPojoRender.render(service, customModel.asInstanceOf[CustomModel])
    }

  }
}
