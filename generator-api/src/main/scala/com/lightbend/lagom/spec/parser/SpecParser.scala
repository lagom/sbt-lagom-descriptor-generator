package com.lightbend.lagom.spec.parser

import java.io.InputStream

import com.lightbend.lagom.spec.LagomGenerator
import com.lightbend.lagom.spec.model.Service

/**
 *
 */
trait SpecParser[T] {
  val parse: LagomGenerator[T]#Parse

  val convert: LagomGenerator[T]#Convert

  /**
   * Convenience method to [[parse]] and then [[convert]] in a single step.
   */
  val read: (InputStream) => Service = { input =>
    (parse andThen convert)(input)
  }
}
