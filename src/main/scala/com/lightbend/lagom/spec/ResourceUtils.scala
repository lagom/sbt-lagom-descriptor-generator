package com.lightbend.lagom.spec

import java.io.InputStream

import scala.io.{ BufferedSource, Source }

/**
 *
 */
object ResourceUtils {

  /**
   * Loads the contents from a resource into an in-mem string.
   */
  def loadContents(resourceName: String): String = {
    val source = Source.fromInputStream(resource(resourceName))
    loadContents(source)
  }

  /**
   * Callers are responsible of releasing the resource.
   */
  def resource(resourceName: String): InputStream = {
    val stream = getClass.getResourceAsStream(resourceName)
    if (stream == null) throw new IllegalArgumentException(s"Can't locate resource $resourceName. ")
    else stream
  }

  /**
   * Loads the contents from an InputStream into an in-mem string. Will close the stream after completion.
   */
  def loadContents(inputStream: InputStream): String = {
    val source = Source.fromInputStream(inputStream)
    loadContents(source)
  }

  private def loadContents(source: BufferedSource) = {
    try {
      source.getLines().mkString("\n")
    } finally {
      source.close()
    }
  }

}
