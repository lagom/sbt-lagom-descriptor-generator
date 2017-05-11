/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.spec.render

import com.lightbend.lagom.spec.LagomGeneratorTypes

trait ModelRender {
  def render: LagomGeneratorTypes.ModelRender
}
