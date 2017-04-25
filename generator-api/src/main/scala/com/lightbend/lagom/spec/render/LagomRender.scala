package com.lightbend.lagom.spec.render

import com.lightbend.lagom.spec.LagomGeneratorTypes

trait LagomRender {
  val render: LagomGeneratorTypes.Render
}