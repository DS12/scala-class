package com.datascience.education.tutorialAnswer.lecture3

import scala.concurrent._

import scala.concurrent.ExecutionContext.Implicits.global

object AsynchronousFactorial {

  def factorial(n: Int): Int = {
    val fact = if(n == 0) 1 else n*factorial(n-1)

    Thread.sleep(500)
    println(s"factorial($n) = $fact")

    fact
  }

  def factorialAsync(n: Int): Future[Int] = Future(factorial(n))

  def printFactorial(n: Int): Future[Int] = {
    val fut: Future[Int] = factorialAsync(n)

    fut.onSuccess {
      case f => println(s"factorial $n is $f")
    }

    fut
  }

}

object AsynchronousFactorialsExample extends App {

  import AsynchronousFactorial._

  val fut10 = printFactorial(10)
  val fut20 = printFactorial(20)

  (1 to 30).foreach(i => {Thread.sleep(500); println(s"unrelated: $i")})

}

import cats.syntax.applicative._
import cats.syntax.writer._
import cats.data.Writer

object FactorialWriter {

  // Task (1a)
  import cats.std.vector._
  type Logged[A] = Writer[Vector[String], A]

  // Task (1b)
  def factorial(n: Int): Logged[Int] = {
    // val fact = if(n == 0) 1 else n*factorial(n-1)

    val fact: Logged[Int] =
      if(n == 0)
        1.writer(Vector("hit bottom"))
      else
        factorial(n-1).flatMap( f =>
          (n*f).writer(Vector(s"factorial($n) = ${n*f}"))
        )

    Thread.sleep(500)
    //Writer(Vector(s"factorial($n) = $fact"), fact)

    fact
  }

  // Task 1c
  def factorialAsync(n: Int): Future[Logged[Int]] =
    Future(factorial(n))

}

object FactorialWriterExample extends App {
  import FactorialWriter._

  // Task 1b
  val fact10 = factorial(10)

  val out = fact10.run

  println(out)

}

object FactorialWriterAsyncExample extends App {
  import FactorialWriter._


  // Task 1d
  // use transformer here, later
  val futureFact10 = factorialAsync(10)
  val futureFact12 = factorialAsync(12)

  val fact10 = futureFact10.map(write => write.run)
  val fact12 = futureFact12.map(write => write.run)

  futureFact10.onSuccess {
    case tup => println(tup)
  }

  futureFact12.onSuccess {
    case tup => println(tup)
  }


}

