package com.datascience.education.tutorial.lecture4


import scala.Option
import scala.Some
import scala.None

import cats.Applicative
import cats.Traverse
import cats.std.list._


object SafeDivision {

  // Task (2a)
  def safeDiv(x: Int, y: Int): Option[Double] =
    ???

  def divTuple(tup: (Int, Int)): Option[Double] =
    safeDiv(tup._1, tup._2)


  import TraverseOption._
  
  // Task (2b)
  def traverseFractions(ll: List[(Int, Int)]): Option[List[Double]] = 
    ???


  // Task (2c)
  def traverseSqrtFractions(ll: List[(Int, Int)]): Option[List[Double]] =
    ???

}


object SafeDivisionExamples extends App {

  import SafeDivision._

  println("Divide 7 by 2")
  println(divTuple((7,2)))

  println("Divide 7 by 0")
  println(divTuple((7,0)))

}

object SafeDivisionTraversalExamples extends App {
  import SafeDivision._

  val a = (6 to 11).toList
  val b = (-3 to 2).toList
  val fracsFailing: List[Tuple2[Int, Int]] = a.zip(b)

  val optionList1: Option[List[Double]] =
    traverseFractions(fracsFailing)

  println("Option[List[Double]] in one step, using `traverse`: ")
  println("should fail")
  println(optionList1)


  println("-----------------")
  val c = (6 to 11).toList
  val d = (2 to 7).toList
  val fracsSuccessful: List[Tuple2[Int, Int]] = c.zip(d)

  println("These fractions do not include an undefined number")
  val optionList2: Option[List[Double]] =
    traverseFractions(fracsSuccessful)

  println("Option[List[Double]] in one step, using `traverse`: ")
  println(optionList2)

  println("-----------------")

  val optionSqrt1: Option[List[Double]] = traverseSqrtFractions(fracsFailing)
  println("Square root of fractions using `traverse`: ")
  println("Should fail")
  println(optionSqrt1)

  val optionSqrt2: Option[List[Double]] = traverseSqrtFractions(fracsSuccessful)
  println("Square root of fractions using `traverse`: ")
  println("These fractions do not include an undefined number and should succeed")
  println(optionSqrt2)

}


