package com.lightbend.lagom.spec.model

case class CallArgument(
  name: String, // TODO: use some abstraction to represent names
  `type`: String
) // TODO: use some abstraction to represent types

case class Call(
  method: Method.Method,
  path: String,
  name: String, // TODO: use some abstraction to represent names
  requestType: Option[String] = None, // TODO: use some abstraction to represent types
  responseType: Option[String] = None, // TODO: use some abstraction to represent types
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

case class Service(
  `package`: String,
  name: String, // TODO: use some abstraction to represent names
  customModels: Seq[String] = Seq.empty[String],
  calls: Seq[Call] = Seq.empty[Call],
  autoAcl: Boolean = true
)
