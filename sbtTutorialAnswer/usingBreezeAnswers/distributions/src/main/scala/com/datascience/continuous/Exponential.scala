package com.datascience.continuous

import breeze.stats.distributions.Exponential
import breeze.stats.meanAndVariance

// https://github.com/scalanlp/breeze/wiki/Quickstart#breezestatsdistributions

object Exp {
  val expo = new Exponential(0.5)

  val sample = expo.sample(2048)

}

object ExpExample extends App {
  import Exp._

  val samples = sample.take(1000000)

  println("mean and variance of our exponential distribution samples: "+meanAndVariance(samples))

}
