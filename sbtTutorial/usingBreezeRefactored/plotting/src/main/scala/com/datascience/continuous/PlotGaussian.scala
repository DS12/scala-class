package com.datascience.continuous

import breeze.stats.distributions.Gaussian

import breeze.plot._

object PlotGaussian extends App {
  val samples = Gauss.sample.take(10000)

  // https://github.com/scalanlp/breeze/wiki/Quickstart#breeze-viz
  val f = Figure()
  val p = f.subplot(0)

  p += hist(samples)


  f.saveas("gaussian_histogram.png")


}
