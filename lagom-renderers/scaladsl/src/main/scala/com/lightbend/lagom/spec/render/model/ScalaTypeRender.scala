/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.spec.render.model

import com.lightbend.lagom.spec.model._

/**
 *
 */
object ScalaTypeRender {
  def renderType(t: Type): String = t match {
    case LInt                     => "Int"
    case LLong                    => "Long"
    case LFloat                   => "Float"
    case LDouble                  => "Double"
    case LChar                    => "Character"
    case LBoolean                 => "Boolean"
    case LString                  => "String"
    case LDate                    => "java.time.LocalDate"
    case LDateTime                => "java.time.Instant"
    case LSequence(of)            => s"scala.collection.immutable.Seq[${renderType(of)}]"
    case LSet(of)                 => s"scala.collection.immutable.Set[${renderType(of)}]"
    case LMap(keyType, valueType) => s"scala.collection.immutable.Map[${renderType(keyType)}, ${renderType(valueType)}]"
    case LOptional(of)            => s"Option[${renderType(of)}]"
    case LUserDefined(name)       => s"$name"
    case LEnum(name, _)           => s"$name.$name"
  }
}
