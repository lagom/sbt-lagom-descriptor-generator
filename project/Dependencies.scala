/*
 * Copyright © 2014-2016 Lightbend, Inc. All rights reserved.
 */

import sbt._

object Version {
  val scalaTest         = "3.0.1"
  // scala version must stay on 2.10.6 since this code is meant to be runnable from sbt.
  val scala             = "2.10.6"
  val swaggerParser     = "1.0.28"
}

object Library {
  val scalaTest              = "org.scalatest"   %% "scalatest"      % Version.scalaTest
  val swaggerParser          = "io.swagger"      % "swagger-parser"  % Version.swaggerParser

}
