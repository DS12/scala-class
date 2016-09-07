package com.datascience.education.tutorial.lecture4


// Hide Scala's native Option, Some, and None from this namespace
import scala.{ Option => _ }
import scala.{ Some => _ }
import scala.{ None => _ }


sealed trait FlawedOption[A] {
  import FlawedOption._

  // Task (5a)

  // def map[B](f: A => B): FlawedOption[B] = this match {
  //   case Some(a) => Some(f(a))
  //   case None => None
  // }

  // def getOrElse(default: => A): A = this match {
  //   case Some(a) => a
  //   case None => default
  // }



}

object FlawedOption {


  case class Some[A](get: A) extends FlawedOption[A]
  case object None extends FlawedOption[Nothing]

}

