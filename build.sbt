import bintray.Keys._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

lazy val `sbt-lagom-descriptor-generator` = project.in(file("."))

sbtPlugin := true

name := "sbt-lagom-descriptor-generator"
organization := "com.lightbend.lagom.generator"

// Scala settings
scalaVersion := Version.scala
crossScalaVersions := List(scalaVersion.value, "2.10.6")
scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.7",  // target "jvm-1.8" was not added until scala 2.11.5 (https://issues.scala-lang.org/browse/SI-8966)
  "-encoding", "UTF-8"
)
unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value)
unmanagedSourceDirectories in Test := List((scalaSource in Test).value)

// Library dependencies
libraryDependencies ++= List(
  Library.swaggerParser,
  Library.scalaTest
)

// Scalariform settings
SbtScalariform.scalariformSettings
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(DanglingCloseParenthesis, Force)

// Release + Bintray settings
licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
publishMavenStyle := false
bintrayPublishSettings
repository in bintray := "sbt-plugins"
bintrayOrganization in bintray := Some("sbt-lagom-descriptor-generator")

// Scripted test settings
scriptedSettings
scriptedLaunchOpts += {version apply { v => s"-Dproject.version=$v" }}.value
