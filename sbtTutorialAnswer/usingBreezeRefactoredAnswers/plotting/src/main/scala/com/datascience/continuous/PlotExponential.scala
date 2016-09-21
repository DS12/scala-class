package com.datascience.continuous

import breeze.stats.distributions.Exponential
import breeze.plot._


object PlotExponential extends App {

  val samples = Exp.sample.take(1000000)

  // https://github.com/scalanlp/breeze/wiki/Quickstart#breeze-viz
  val f = Figure()
  val p = f.subplot(0)

  p += hist(samples)



  f.saveas("exponential_histogram.png")

}
