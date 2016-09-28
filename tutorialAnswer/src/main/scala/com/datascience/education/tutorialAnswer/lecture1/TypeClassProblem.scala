package com.datascience.education.tutorialAnswer.lecture1

import scala.language.higherKinds

object ModelProblem {

  trait Plottable[Domain, Range, T[Domain, Range]] {
    // Task (6a)
    // Answer (6a)
    def points(t: T[Domain, Range], ld: List[Domain]): List[Range]

    import breeze.plot._

    def plot(name: String, t: T[Domain, Range], input: List[Domain]): Unit = {
      val y = points(t, input)
      println(s"$name: $y")
    }

  }

  class Model[Domain, Range](val pdf: Domain => Range)


  implicit object PlotDoubleDoubleModel extends Plottable[Double, Double, Model] {
    // Task (6b)
    // Answer (6b)
    def points(mod: Model[Double, Double], ld: List[Double]): List[Double] =
      ld.map(mod.pdf)

  }

  implicit object PlotDoubleDoubleFunction extends Plottable[Double, Double, Function1] {

    // Task (6c)
    // Answer (6c)
    def points(func: Double => Double, ld: List[Double]): List[Double] =
      ld.map(func)

  }

  // Task (6d)
  def plotter[D,R,T[D,R]](t: T[D,R], ld: List[D], name: String)(implicit plottable: Plottable[D,R,T]): Unit =
    plottable.plot(name, t, ld)   // Answer (6d)

  def gaussianPDF(u: Double, t: Double)(d: Double): Double =
    (1/(math.sqrt(2*t*t*math.Pi)))*math.pow(math.E, -1*math.pow(d - u, 2)/(2*t*t))

  val gaussianModel = new Model(gaussianPDF(1.0,2.0))

}

object PlotExample extends App {

  import ModelProblem._


  val x = (-16 to 16).toList.map(_.toDouble).map(_/10.0)
  plotter(gaussianModel, x, "gaussianpdf.png")


}

object ContinuousUsage {


}

object DiscreteUsage {

  sealed trait Coin
  case object Heads extends Coin
  case object Tails extends Coin

  sealed trait Color
  case object Blue extends Color
  case object Orange extends Color
  case object Red extends Color
  case object Green extends Color
  case object Grey extends Color


}
