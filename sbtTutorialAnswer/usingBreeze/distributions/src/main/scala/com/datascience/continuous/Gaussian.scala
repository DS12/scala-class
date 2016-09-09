package com.datascience.continuous

import breeze.stats.distributions.Gaussian
import breeze.stats.mean

object Gauss {
  val zeroToOne = Gaussian(0,1)

  val sample = zeroToOne.sample(100000)

}

object GaussExample extends App {
  import Gauss._

  println("Mean of our Gaussian sample: "+mean(sample))

}
