package com.datascience.education.tutorialAnswer.lecture4

import scala.Option
import scala.Some
import scala.None

import cats.Applicative
import cats.instances.list._


object SafeDivision {

  // Task (2a)
  // answer
  // def safeDiv(x: Int, y: Int): Option[Double] =
  //   try {
  //     val d: Double = x.toDouble / y
  //     if (d.isNaN || d.isPosInfinity || d.isNegInfinity)
  //       None
  //     else
  //       Some(d)
  //   } catch {
  //     case ae: java.lang.ArithmeticException => None
  //   }

  def safeDivInt(numerator: Int, denominator: Int): Option[Int] =
    try {
      Some(numerator / denominator)
    } catch {
      case ae: java.lang.ArithmeticException => None
    }

  def squareRootFrac(numerator: Int, denominator: Int): Option[Double] =
    safeDivInt(numerator, denominator).flatMap { _ =>
      val squareRoot: Double = math.sqrt(numerator.toDouble / denominator)
      if (squareRoot.isNaN)
        None
      else
        Some(squareRoot)
    }


  def squareRootFrac(tup: (Int, Int)): Option[Double] =
    squareRootFrac(tup._1, tup._2)


  import TraverseOption._
  
  // Task (2b)
  // answer
  def traverseFractions(ll: List[(Int, Int)]): Option[List[Double]] = 
    traverse(ll)(squareRootFrac)


}


object SafeDivisionExamples extends App {

  import SafeDivision._


  println("sqrt(7/2)")
  println(squareRootFrac(7,2))

  println("sqrt(7/0)")
  println(squareRootFrac(7,0))

  println("sqrt(-4/3)")
  println(squareRootFrac(-4,3))


}

object SafeDivisionTraversalExamples extends App {
  import SafeDivision._


  val a = (6 to 11).toList
  val b = (-3 to 2).toList
  val fracsFailing: List[Tuple2[Int, Int]] = a.zip(b)

  val optionDoubles1: Option[List[Double]] =
    traverseFractions(fracsFailing)

  println("Option[List[Double]] in one step, using `traverse`. ")
  println("should fail")
  println(optionDoubles1)


  println("-----------------")
  val c = (6 to 11).toList
  val d = (2 to 7).toList
  val fracsSuccessful: List[Tuple2[Int, Int]] = c.zip(d)

  println("These fractions do not include an undefined number")
  val optionList2: Option[List[Double]] =
    traverseFractions(fracsSuccessful)

  println("Option[List[Double]] in one step, using `traverse`: ")
  println(optionList2)

}


