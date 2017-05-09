import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt.Keys.{ crossScalaVersions, homepage, libraryDependencies, scalacOptions, unmanagedSourceDirectories }

import scala.collection.immutable
import scalariform.formatter.preferences._
import de.heikoseeberger.sbtheader._


lazy val `root` = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(name := "sbt-lagom-descriptor-generator")
  .aggregate(
    `generator-api`,
    `openapi-parser`,
    `lagom-renderer-javadsl`,
    `lagom-renderer-scaladsl`,
    `runner`,
    `lagom-descriptor-generator-sbt-plugin`
  )
  .settings(commonSettings: _*)
  .settings(
    publishLocal := {},
    publishArtifact in Compile := false,
    publish := {}
  )

lazy val commonSettings =
  Seq(
    organization := "com.lightbend.lagom",
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    homepage := Some(url("https://github.com/lagom/sbt-lagom-descriptor-generator")),
    sonatypeProfileName := "com.lightbend",
    // Scala settings
    scalaVersion := Version.scala,
    crossScalaVersions := List(scalaVersion.value, "2.10.6")
    ) ++
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

lazy val commonScalariformSettings: immutable.Seq[Setting[_]] =
  SbtScalariform.scalariformSettings ++ Seq(
    // Scalariform settings
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(DanglingCloseParenthesis, Force)
  )

def bintraySettings = Seq(
  publishMavenStyle := false
) ++
  Seq(
    bintrayOrganization := Some("lagom"),
    bintrayRepository := "sbt-plugin-releases",
    bintrayPackage := "lagom-descriptor-generator-sbt-plugin",
    bintrayReleaseOnPublish := false
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


lazy val scripteTestsSettings =
  scriptedSettings ++
    Seq(
      scriptedLaunchOpts += {
        version apply { v => s"-Dproject.version=$v" }
      }.value
    )

def RuntimeLibPlugins = AutomateHeaderPlugin && Sonatype && PluginsAccessor.exclude(BintrayPlugin)
//def RuntimeLibPlugins = AutomateHeaderPlugin && Sonatype && PluginsAccessor.exclude(BintrayPlugin)
def SbtPluginPlugins = AutomateHeaderPlugin && BintrayPlugin && PluginsAccessor.exclude(Sonatype)


lazy val `lagom-descriptor-generator-sbt-plugin` = project
  .in(file("lagom-descriptor-generator-sbt-plugin"))
  .settings(
    releaseSettings,
    bintraySettings,
    commonSettings,
    commonScalariformSettings
  )
  .settings(scripteTestsSettings: _*)
  .enablePlugins(SbtPluginPlugins)
  .settings(
    name := "lagom-descriptor-generator-sbt-plugin",
    sbtPlugin := true,
    publishTo := {
      if (isSnapshot.value) {
        //         Bintray doesn't support publishing snapshots, publish to Sonatype snapshots instead
        Some(Opts.resolver.sonatypeSnapshots)
      } else publishTo.value
    },
    publishMavenStyle := isSnapshot.value
  ).dependsOn(`runner`)

lazy val `runner` = project
  .in(file("runner"))
  .enablePlugins(RuntimeLibPlugins)
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    commonScalariformSettings,
    Defaults.itSettings,
    libraryDependencies ++= List(
      Library.scalaTest % "test,it"
    )
  )
  .dependsOn(
    `openapi-parser`,
    `lagom-renderer-javadsl`,
    `lagom-renderer-scaladsl`
  )


lazy val `lagom-renderer-javadsl` = project
  .in(file("lagom-renderers") / "javadsl")
  .enablePlugins(RuntimeLibPlugins)
  .settings(
    commonSettings,
    commonScalariformSettings,
    libraryDependencies ++= List(
      Library.scalaTest % "test"
    )
  ).dependsOn(`generator-api`)

lazy val `lagom-renderer-scaladsl` = project
  .in(file("lagom-renderers") / "scaladsl")
  .enablePlugins(RuntimeLibPlugins)
  .settings(
    commonSettings,
    commonScalariformSettings,
    libraryDependencies ++= List(
      Library.scalaTest % "test"
    )
  ).dependsOn(`generator-api`)

lazy val `openapi-parser` = project
  .in(file("spec-parsers") / "openapi")
  .enablePlugins(RuntimeLibPlugins)
  .settings(
    commonSettings,
    commonScalariformSettings,
    libraryDependencies ++= List(
      Library.swaggerParser,
      Library.scalaTest % "test"
    )
  ).dependsOn(`generator-api`)

lazy val `generator-api` = project
  .in(file("generator-api"))
  .enablePlugins(RuntimeLibPlugins)
  .settings(
    commonSettings,
    commonScalariformSettings,
    libraryDependencies ++= List(
      Library.scalaTest % "test"
    )
  )
