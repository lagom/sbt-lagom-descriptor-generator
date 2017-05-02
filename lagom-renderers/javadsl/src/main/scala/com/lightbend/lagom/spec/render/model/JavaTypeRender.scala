package com.lightbend.lagom.spec.render.model

import com.lightbend.lagom.spec.model._

/**
 *
 */
object JavaTypeRender {
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
    case LMap(keyType, valueType) => s"org.pcollections.HashTreePMap<${renderType(keyType, preferPrimitives = false)}, ${renderType(valueType, preferPrimitives = false)}>"
    case LOptional(of)            => s"java.util.Optional<${renderType(of, preferPrimitives = false)}>"
    case LUserDefined(name)       => s"$name"
    case LEnum(name, values)      => s"$name"
  }
}
