package com.datascience.education.tutorial.lecture4


// Hide Scala's native Option, Some, and None from this namespace
import scala.{ Option => _ }
import scala.{ Some => _ }
import scala.{ None => _ }


sealed trait FlawedOption[A] {
  import FlawedOption._

  // See FlawedOptionSpec
  // Task (5a)

//   def map[B](f: A => B): FlawedOption[B] = this match {
//     case FlawedSome(a) => FlawedSome(f(a))
//     case FlawedNone => FlawedNone
//   }
//
//   def getOrElse(default: => A): A = this match {
//     case FlawedSome(a) => a
//     case FlawedNone => default
//   }

}

object FlawedOption {


  case class FlawedSome[A](get: A) extends FlawedOption[A]
  case object FlawedNone extends FlawedOption[Nothing]

}

