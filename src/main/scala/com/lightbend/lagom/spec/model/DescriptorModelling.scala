package com.lightbend.lagom.spec.model

case class CallArgument(
  name: String, // TODO: use some abstraction to represent names
  `type`: Type
) // TODO: use some abstraction to represent types

case class Call(
  method: Method.Method,
  path: String,
  name: String, // TODO: use some abstraction to represent names
  requestType: Option[Type] = None, // TODO: use some abstraction to represent types
  responseType: Option[Type] = None, // TODO: use some abstraction to represent types
  arguments: Seq[CallArgument] = Seq.empty[CallArgument],
  autoAcl: Option[Boolean] = None
)

object Method {
  sealed abstract class Method(val name: String)
  case object GET extends Method("GET")
  case object PUT extends Method("PUT")
  case object POST extends Method("POST")
  case object DELETE extends Method("DELETE")
  case object OPTIONS extends Method("OPTIONS")
  case object PATCH extends Method("PATCH")
  case object HEAD extends Method("HEAD")
}

case class ModelField(
  fieldType: Type,
  fieldName: String
)

sealed trait CustomType {
  val className: String
}
case class CustomEnum(className: String, values: Seq[String]) extends CustomType
case class CustomModel(className: String, fields: Seq[ModelField] = Seq.empty[ModelField]) extends CustomType

case class Service(
    `package`: String,
    name: String, // TODO: use some abstraction to represent names
    customModels: Set[CustomType] = Set.empty[CustomType],
    calls: Seq[Call] = Seq.empty[Call],
    autoAcl: Boolean = true
) {
  val interfaceName = s"${name.head.toUpper}${name.tail.map(_.toLower)}Api"
  val traitName = interfaceName
}
