package com.lightbend.lagom.spec.sbt

import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.{ AutoPlugin, File, _ }

object LagomOpenApiPlugin extends AutoPlugin {

  object autoImport {
    val lagomOpenAPIGenerateDescriptor = taskKey[Seq[File]]("Generate Lagom Descriptors from OpenAPI specs.")
  }

  import autoImport._

  override def trigger = noTrigger

  override def requires = JvmPlugin

  override def projectSettings = inConfig(Compile)(openApiSettings) ++ inConfig(Test)(openApiSettings)

  val openApiSettings = Seq(
    sourceDirectory in lagomOpenAPIGenerateDescriptor := sourceDirectory.value / "openapi",
    resourceDirectory in lagomOpenAPIGenerateDescriptor := sourceDirectory.value / "openapi",

    target in lagomOpenAPIGenerateDescriptor :=
      crossTarget.value / "openapi" / Defaults.nameForSrc(configuration.value.name),
    lagomOpenAPIGenerateDescriptor := LagomOpenApiGenerator.lagomOpenAPIGenerateDescriptorTask().value,
    sourceGenerators <+= lagomOpenAPIGenerateDescriptor,
    // TODO: review managedSources
    managedSourceDirectories += (target in lagomOpenAPIGenerateDescriptor).value / "java",
    unmanagedResourceDirectories += (resourceDirectory in lagomOpenAPIGenerateDescriptor).value,
    watchSources in Defaults.ConfigGlobal ++= (sources in lagomOpenAPIGenerateDescriptor).value
  ) ++
    inTask(lagomOpenAPIGenerateDescriptor)(
      Seq(
        includeFilter := GlobFilter("*.json") || GlobFilter("*.yaml"),

        managedSourceDirectories := Nil,
        unmanagedSourceDirectories := Seq(sourceDirectory.value),
        sourceDirectories := unmanagedSourceDirectories.value ++ managedSourceDirectories.value,
        unmanagedSources <<= Defaults.collectFiles(sourceDirectories, includeFilter, excludeFilter),
        managedSources := Nil,
        sources := managedSources.value ++ unmanagedSources.value,

        managedResourceDirectories := Nil,
        unmanagedResourceDirectories := Seq(resourceDirectory.value),
        resourceDirectories := unmanagedResourceDirectories.value ++ managedResourceDirectories.value,
        unmanagedResources <<= Defaults.collectFiles(resourceDirectories, includeFilter, excludeFilter),
        managedResources := Nil,
        resources := managedResources.value ++ unmanagedResources.value

      )
    )
}
