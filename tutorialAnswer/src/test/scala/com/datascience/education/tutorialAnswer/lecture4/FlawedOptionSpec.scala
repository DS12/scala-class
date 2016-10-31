package com.datascience.education.tutorialAnswer.lecture4

import org.scalatest.{FunSuite, Matchers}

class FlawedOptionSpec extends FunSuite with Matchers {

  test("`map` should not compile when the type parameter of `FlawedOption` is invariant") {
    """
       def map[B](f: A => B): FlawedOption[B] = this match {
         case FlawedSome(a) => FlawedSome(f(a))
         case FlawedNone => FlawedNone
       }
    """ shouldNot compile
  }

  test("`getOrElse` should not compile when the type parameter of `FlawedOption` is invariant") {
    """
      def getOrElse(default: => A): A = this match {
        case FlawedSome(a) => a
        case FlawedNone => default
      }
    """ shouldNot compile
  }
  
}
