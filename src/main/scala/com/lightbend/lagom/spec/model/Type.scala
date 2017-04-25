package com.lightbend.lagom.spec.model

sealed trait Type

case object LInt extends Type
case object LLong extends Type
case object LFloat extends Type
case object LDouble extends Type
case object LChar extends Type
case object LBoolean extends Type
case object LString extends Type

case object LDate extends Type
case object LDateTime extends Type

case class LSequence(of: Type) extends Type
case class LSet(of: Type) extends Type
case class LMap(keyType: Type, valueType: Type) extends Type

case class LOptional(of: Type) extends Type
case class LUserDefined(name: String) extends Type
case class LEnum(name: String, values: Seq[String]) extends Type
