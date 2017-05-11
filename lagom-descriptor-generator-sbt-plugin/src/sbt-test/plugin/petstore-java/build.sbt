organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lazy val `petstore` = (project in file("."))
  .aggregate(`petstore-api`, `petstore-impl`)

lazy val `petstore-api` = (project in file("petstore-api"))
  .enablePlugins(LagomOpenApiPlugin)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson
    )
  )

lazy val `petstore-impl` = (project in file("petstore-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
  )
  .dependsOn(`petstore-api`)

def common = Seq(
  javacOptions in compile += "-parameters"
)


lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false
