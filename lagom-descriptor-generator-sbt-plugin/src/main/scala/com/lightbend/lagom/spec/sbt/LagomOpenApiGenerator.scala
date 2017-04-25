/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.spec.sbt

import java.io.FileInputStream

import com.lightbend.lagom.spec.LagomGeneratorTypes.GeneratedCode
import com.lightbend.lagom.spec.sbt.LagomOpenApiPlugin.autoImport._
import com.lightbend.lagom.spec.{ LagomGenerators, ResourceUtils }
import sbt.Keys._
import sbt._

object LagomOpenApiGenerator {

  def lagomOpenAPIGenerateDescriptorTask = Def.task {
    val packageName = organization.value
    val specFiles: Seq[File] = (sources in lagomOpenAPIGenerateDescriptor).value
    val outputDirectory: File = (target in lagomOpenAPIGenerateDescriptor).value

    specFiles.flatMap { specFile =>
      val targetDir = new File(outputDirectory, "java")
      val genTypeOutput =
        LagomGenerators.swaggerV2ToLagomJava(new FileInputStream(specFile.getAbsoluteFile), "com.example")

      val generatedFiles: Seq[File] =
        writeFile(targetDir, genTypeOutput.descriptor) +: genTypeOutput.models.map { case (_, genCode) => writeFile(targetDir, genCode) }.toSeq
      generatedFiles
    }
  }

  private def writeFile(folder: File, content: GeneratedCode): File =
    ResourceUtils.writeFile(folder, content.filename, content.fileContents)

}
