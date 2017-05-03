/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.spec

import java.io.{ File, InputStream }
import java.nio.file.{ Files, Paths, StandardOpenOption }

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

  /**
   * @param folder an absolute folder
   * @param filename a relative path to a file (may contain File.separators)
   * @param fileContents
   * @return
   */
  def writeFile(folder: File, filename: String, fileContents: String): File = {
    val path = Paths.get(folder.getAbsolutePath, filename)
    // `path` is tha absolute route to the file so only path.parent must be created as directories
    Files.createDirectories(path.getParent)
    Files.write(
      path,
      fileContents.getBytes,
      StandardOpenOption.CREATE,
      StandardOpenOption.SYNC,
      StandardOpenOption.TRUNCATE_EXISTING
    ).toFile

  }
}
