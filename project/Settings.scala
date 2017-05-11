import bintray.BintrayPlugin.autoImport._
import com.typesafe.sbt.SbtPgp.autoImportImpl.PgpKeys
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import de.heikoseeberger.sbtheader.HeaderKey.headers
import de.heikoseeberger.sbtheader.{ HeaderPattern, Seq }
import sbt.Keys._
import sbt.ScriptedPlugin.{ scriptedLaunchOpts, scriptedSettings }
import sbt._
import sbtrelease.ReleasePlugin.autoImport._

import scala.collection.immutable
import scalariform.formatter.preferences.{ AlignSingleLineCaseStatements, DanglingCloseParenthesis, DoubleIndentClassDeclaration, Force }

object Settings {

  def headerLicenseSettings =
    Seq(
      headers := headers.value ++ Map(
        "scala" -> (
          HeaderPattern.cStyleBlockComment,
          """|/*
             | * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
             | */
             |""".stripMargin
        ),
        "java" -> (
          HeaderPattern.cStyleBlockComment,
          """|/*
             | * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
             | */
             |""".stripMargin
        )
      ), scalacOptions ++= List(
        "-unchecked",
        "-deprecation",
        "-language:_",
        "-target:jvm-1.7", // target "jvm-1.8" was not added until scala 2.11.5 (https://issues.scala-lang.org/browse/SI-8966)
        "-encoding", "UTF-8"
      ),
      unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value),
      unmanagedSourceDirectories in Test := List((scalaSource in Test).value),
      unmanagedSourceDirectories in IntegrationTest := List((scalaSource in Test).value)
    )

  def commonScalariformSettings: immutable.Seq[Setting[_]] =
    SbtScalariform.scalariformSettings ++ Seq(
      // Scalariform settings
      ScalariformKeys.preferences := ScalariformKeys.preferences.value
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
        .setPreference(DoubleIndentClassDeclaration, true)
        .setPreference(DanglingCloseParenthesis, Force)
    )

  def scriptedTestsSettings =
    scriptedSettings ++
      Seq(
        scriptedLaunchOpts += {
          version apply { v => s"-Dproject.version=$v" }
        }.value
      )

  // release-related settings

  def bintraySettings =
    Seq(
      bintrayOrganization := Some("lagom"),
      bintrayRepository := "sbt-plugin-releases",
      bintrayPackage := "lagom-descriptor-generator-sbt-plugin",
      bintrayReleaseOnPublish := false
    )

  def publishMavenStyleSettings = Seq(
    publishMavenStyle := false
  )

  def releaseSettings: Seq[Setting[_]] = Seq(
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseTagName := (version in ThisBuild).value,
    releaseProcess := {
      import ReleaseTransformations._

      Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishArtifacts,
        releaseStepTask(bintrayRelease in thisProjectRef.value),
        releaseStepCommand("sonatypeRelease"),
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    }
  )


}