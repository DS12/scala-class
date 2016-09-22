package com.datascience.discrete

import breeze.stats.distributions.Bernoulli
import breeze.stats.meanAndVariance
import breeze.stats.mean

object Bern {
  val bern = new Bernoulli(0.6)

  val sample = bern.sample(1)
}

object BernExample extends App {
  import Bern._

  val samples = sample.take(100000)

  // println("Mean of our Bernoulli samples: "+mean(samples))
}

