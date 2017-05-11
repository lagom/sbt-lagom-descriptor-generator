organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lazy val `dummy` = (project in file("."))
  .aggregate(`dummy-api`, `dummy-impl`)

lazy val `dummy-api` = (project in file("dummy-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson
    )
  )

lazy val `dummy-impl` = (project in file("dummy-impl"))
  .enablePlugins(LagomJava && LagomOpenApiPlugin)
  .settings(common: _*)
  .settings(lagomForkedTestSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslTestKit
    )
  )
  .dependsOn(`dummy-api`)

def common = Seq(
  javacOptions in compile += "-parameters"
)


lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false


