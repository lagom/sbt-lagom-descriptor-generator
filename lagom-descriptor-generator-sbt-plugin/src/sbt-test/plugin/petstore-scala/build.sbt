organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lazy val `petstore` = (project in file("."))
  .aggregate(`petstore-api`, `petstore-impl`)

lazy val `petstore-api` = (project in file("petstore-api"))
  .enablePlugins(LagomOpenApiPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `petstore-impl` = (project in file("petstore-impl"))
  .enablePlugins(LagomScala)
  .settings(
  )
  .dependsOn(`petstore-api`)


// Custom assertion to check file contents.
// Usage:
//    assertFileContains <relativePath> <seeked String>
// <relativePath>   if relative file path starting on `petstore-api/target`
// <seeked String>  the string we are looking for. Must not span more than one line in the file. This is a
//                  multi-argument so it doesn't need quoting.
InputKey[Unit]("assertFileContains") := {
  val args = Def.spaceDelimited().parsed
  val relativeFilePath = args.head
  val expectedContent = args.tail.mkString(" ")
  try {
    val file: File = (target in `petstore-api`).value / relativeFilePath
    if (!IO.readLines(file).exists(_.contains(expectedContent))) {
      throw new RuntimeException(s"Couldn't find $expectedContent in ${file.getAbsolutePath}.")
    }
  }
  catch {
    // if we are here it's all good
    case e: Exception => ()
  }
}


lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false
