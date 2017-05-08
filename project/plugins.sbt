addSbtPlugin("com.github.gseitz" % "sbt-release"      % "1.0.2")
addSbtPlugin("org.scalariform"   % "sbt-scalariform"  % "1.6.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray"      % "0.4.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.8.0")
