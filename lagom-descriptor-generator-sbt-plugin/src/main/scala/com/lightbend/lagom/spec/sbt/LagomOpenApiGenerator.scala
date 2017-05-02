/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.spec.sbt

import java.io.FileInputStream

import com.lightbend.lagom.spec.LagomGeneratorTypes.GeneratedCode
import com.lightbend.lagom.spec.sbt.LagomOpenApiPlugin.autoImport._
import com.lightbend.lagom.spec.{ LagomGeneratorTypes, LagomGenerators, ResourceUtils }
import sbt.Keys._
import sbt._

object LagomOpenApiGenerator {

  /**
   * Decide whether java or scala code should be generated from the dependencies in the project. Will
   * look for dependencies named "lagom-javadsl.*" or "lagom-scaladsl.*" in the list of dependencies.
   *
   * @return
   */
  def dslDiscoveryTask: Def.Initialize[Task[String]] = Def.task {
    // TODO: not sure this is the best approach but
    val deps = allDependencies.value
    if (deps.exists(_.name.contains("lagom-javadsl"))) {
      "java"
    } else if (deps.exists(_.name.contains("lagom-scaladsl"))) {
      "scala"
    } else {
      throw new IllegalArgumentException(s"Can't determine the target language.")
    }
  }

  def lagomOpenAPIGenerateDescriptorTask(): Def.Initialize[Task[Seq[File]]] = Def.task {
    val lang = dslDiscoveryTask.value
    val packageName = organization.value
    val specFiles: Seq[File] = (sources in lagomOpenAPIGenerateDescriptor).value
    val outputDirectory: File = (target in lagomOpenAPIGenerateDescriptor).value

    specFiles.flatMap { specFile =>
      val targetDir = new File(outputDirectory, lang)
      val serviceName = extractServiceName(specFile.getName)
      val output = generate(lang, specFile, packageName, serviceName)

      val generatedFiles: Seq[File] =
        writeFile(targetDir, output.descriptor) +: output.models.map { case (_, genCode) => writeFile(targetDir, genCode) }.toSeq
      generatedFiles
    }
  }

  private def generate(lang: String, specFile: File, packageName: String, serviceName: String): LagomGeneratorTypes.Output = {
    // Decide formatter.
    lang match {
      case "java" =>
        LagomGenerators.openApiV2ToLagomJava(new FileInputStream(specFile.getAbsoluteFile), packageName, serviceName)
      case _ =>
        LagomGenerators.openApiV2ToLagomScala(new FileInputStream(specFile.getAbsoluteFile), packageName, serviceName)
    }
  }

  private def writeFile(folder: File, content: GeneratedCode): File =
    ResourceUtils.writeFile(folder, content.filename, content.fileContents)

  private def extractServiceName(filename: String): String = {
    filename.reverse.dropWhile(_ != '.').drop(1).reverse
  }

}
