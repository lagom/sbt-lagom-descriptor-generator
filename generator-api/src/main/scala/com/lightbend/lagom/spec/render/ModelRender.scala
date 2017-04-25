package com.lightbend.lagom.spec.render

import com.lightbend.lagom.spec.LagomGeneratorTypes

trait ModelRender {
  def render: LagomGeneratorTypes.ModelRender
}
