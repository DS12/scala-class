package com.datascience.education.tutorialAnswer.lecture4a

object SafeDivisionOption {

  def safeDivInt(numerator: Int, denominator: Int): Option[Int] =
    try {
      Some(numerator / denominator)
    } catch {
      case ae: java.lang.ArithmeticException => None
    }

  def squareRootFrac(numerator: Int, denominator: Int): Option[Double] =
    safeDivInt(numerator, denominator).flatMap { _ =>
      val squareRoot: Double =
        math.sqrt(numerator.toDouble / denominator)
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


object SafeDivisionOptionExamples extends App {

  import SafeDivisionOption._


  println("sqrt(7/2)")
  println(squareRootFrac(7,2))

  println("-----------------")

  println("sqrt(7/0)")
  println(squareRootFrac(7,0))

  println("-----------------")

  println("sqrt(-4/3)")
  println(squareRootFrac(-4,3))


}


import cats.data.Xor
import cats.data.Xor.Left
import cats.data.Xor.Right

object SafeDivisionXor {

  // Task 2a
  // answer
  def safeDivInt(numerator: Int, denominator: Int): Xor[Exception, Int] =
    try {
      Right(numerator / denominator)
    } catch {
      case ae: java.lang.ArithmeticException =>
        Left(ae)
    }


  // alternative answer  2a
  def safeDivInt2(numerator: Int, denominator: Int): Xor[Exception, Int] =
    Xor.catchOnly[Exception](numerator / denominator)


  /*
   This answer is not acceptable because we don't want to return a Throwable
   */
  // http://typelevel.org/cats/api/index.html#cats.data.Xor$@catchNonFatal[A](f:=>A):cats.data.Xor[Throwable,A]

  // def safeDivInt3(numerator: Int, denominator: Int): Xor[Throwable, Int] =
  //   Xor.catchNonFatal(numerator / denominator)




  // Task 2b
  def squareRootFrac(numerator: Int, denominator: Int):
      Xor[Exception, Double] =
    safeDivInt(numerator, denominator).flatMap { _ =>
      val squareRoot: Double =
        math.sqrt(numerator.toDouble / denominator)
      if (squareRoot.isNaN)
        Left(new Exception("Square root is undefined"))
      else
        Right(squareRoot)
    }


  def squareRootFrac(tup: (Int, Int)): Xor[Exception, Double] =
    squareRootFrac(tup._1, tup._2)



}

object SafeDivIntXorExamples extends App {
  import SafeDivisionXor._

  println("7/2")
  println(safeDivInt(7,2))

  println("-----------------")

  println("7/0")
  println(safeDivInt(7,0))


}


object SquareRootFracXorExamples extends App {
  import SafeDivisionXor._

  println("sqrt(7/2)")
  println(squareRootFrac(7,2))

  println("-----------------")

  println("sqrt(7/0)")
  println(squareRootFrac(7,0))

  println("-----------------")

  println("sqrt(-4/3)")
  println(squareRootFrac(-4,3))



}




