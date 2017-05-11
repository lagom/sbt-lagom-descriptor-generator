addSbtPlugin("com.github.gseitz" % "sbt-release"      % "1.0.2")
addSbtPlugin("org.scalariform"   % "sbt-scalariform"  % "1.6.0")

// can't use sbt-bintray 0.4.0 because --> https://github.com/sbt/sbt-bintray/issues/104
//addSbtPlugin("org.foundweekends" % "sbt-bintray"      % "0.4.0")
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")


addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.5.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.8.0")
