package com.lightbend.lagom.spec

/**
 *
 */
object Main extends App {

  private val inputStream = getClass.getResourceAsStream(args(0))

  println(LagomGenerators.swaggerV2ToLagomJava(inputStream, "com.example"))

}
