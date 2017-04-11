/*
 * Copyright © 2014-2016 Lightbend, Inc. All rights reserved.
 */

import sbt._

object Version {
  val lagom             = "1.4.0-SNAPSHOT"
  val scalaTest         = "3.0.1"
  val scala             = "2.10.6"
  val swaggerParser     = "1.0.28"
}

object Library {
  val scalaTest              = "org.scalatest"   %% "scalatest"      % Version.scalaTest
  val swaggerParser          = "io.swagger"      % "swagger-parser"  % Version.swaggerParser

}
