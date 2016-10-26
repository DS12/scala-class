package com.datascience.education.tutorial.lecture3

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

  // type Logged[A] = ???

  // Task (1b)
  // def factorial(n: Int): ??? = ???


  // Task 1c
  // def factorialAsync(n: Int): ??? = ???


}

object FactorialWriterExample extends App {
  import FactorialWriter._

  // Task 1b

}

object FactorialWriterAsyncExample extends App {
  import FactorialWriter._


  // Task 1d


}

