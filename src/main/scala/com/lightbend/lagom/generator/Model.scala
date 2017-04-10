package com.lightbend.lagom.generator

case class CallArgument(name: String, `type`: String)

case class Call(
  method: Method.Method,
  path: String,
  name: String,
  requestType: Option[String] = None,
  responseType: Option[String] = None,
  arguments: Seq[CallArgument] = Seq.empty[CallArgument]
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
  name: String,
  customModels: Seq[String] = Seq.empty[String],
  calls: Seq[Call] = Seq.empty[Call]
)
