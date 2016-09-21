package com.datascience.continuous

// https://github.com/scalanlp/breeze/wiki/Quickstart#breezestatsdistributions

import breeze.stats.distributions.Poisson
import breeze.stats.meanAndVariance

object Poi {
  val poi = new Poisson(3.0)

  val sample = poi.sample(1)

}

// object PoiExample extends App {
//   import Poi._


//   val samples = sample.take(100000)

//   println("Mean of our Poisson samples: "+meanAndVariance(samples))

// }
