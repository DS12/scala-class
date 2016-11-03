package com.datascience.education.tutorial.lecture4

import org.scalatest.{FunSuite, Matchers}

class FlawedOptionSpec extends FunSuite with Matchers {

  test("Methods in FlawedOption should not compile when the type parameter of `FlawedOption` is invariant") {
    """
      sealed trait FlawedOption[A] {
        import FlawedOption._

           def map[B](f: A => B): FlawedOption[B] = this match {
             case FlawedSome(a) => FlawedSome(f(a))
             case FlawedNone => FlawedNone
           }

           def getOrElse(default: => A): A = this match {
             case FlawedSome(a) => a
             case FlawedNone => default
           }
      }

      object FlawedOption {
        case class FlawedSome[A](get: A) extends FlawedOption[A]
        case object FlawedNone extends FlawedOption[Nothing]
      }
    """ shouldNot compile
  }

}
