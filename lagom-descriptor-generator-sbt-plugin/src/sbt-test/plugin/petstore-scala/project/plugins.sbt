
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.3.2")

addSbtPlugin("com.lightbend.lagom" % "lagom-descriptor-generator-sbt-plugin" % "1.4.0-SNAPSHOT")
//lazy val dev = ProjectRef(Path.fileProperty("user.dir").getParentFile, "lagom-descriptor-generator-sbt-plugin")
//lazy val plugins = (project in file(".")).dependsOn(dev)
