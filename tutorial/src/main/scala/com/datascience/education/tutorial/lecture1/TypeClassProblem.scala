package com.datascience.education.tutorial.lecture1

import scala.language.higherKinds

object ModelProblem {

  // Task (6a)
  trait Plottable[Domain, Range, T[Domain, Range]] {
    // The `= ???` is necessary to prevent compiler error; but this should be abstract!
    // Remove `= ???` upon implementation of the signature of `points`
    def points/*???*/ = ???


    def plot(name: String, t: T[Domain, Range], input: List[Domain]): Unit = {
      //val y = points(t, input)
      val y = ???
      println(s"$name: $y")
    }
    
  }

  class Model[Domain, Range](val pdf: Domain => Range)

  // Task (6b)
  implicit object PlotDoubleDoubleModel extends Plottable[Double, Double, Model] {

    ???
  }

  // Task (6c)
  implicit object PlotDoubleDoubleFunction extends Plottable[Double, Double, Function1] {

    ???
  }

  // Task (6d)
  def plotter[D,R,T[D,R]](t: T[D,R], ld: List[D], name: String)
    (implicit plottable: Plottable[D,R,T]): Unit = ???


  def gaussianPDF(u: Double, t: Double)(d: Double): Double =
    (1/(math.sqrt(2*t*t*math.Pi)))*math.pow(math.E, -1*math.pow(d - u, 2)/(2*t*t))

  val gaussianModel = new Model(gaussianPDF(1.0,2.0))

}

object PlotExample extends App {

  import ModelProblem._


  val x = (-16 to 16).toList.map(_.toDouble).map(_/10.0)
  plotter(gaussianModel, x, "gaussianpdf.png")


}

