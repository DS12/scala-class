package com.datascience.reporting

import scala.util.Random

import com.datascience.approximations.MonteCarlo

object PrintPi extends App {

  val rand = new Random

  val n = 4096

  val piEstimate: Double = MonteCarlo.pi(n, rand)

  println(s"Pi is estimated to equal $piEstimate with $n Monte Carlo iterations")

}
