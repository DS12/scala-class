// package com.datascience.education.tutorialCommon.lecture4

// import org.scalatest._
// import org.scalatest.{FunSuite, Matchers}


// //import FlawedOption._

// trait FlawedOptionSpec extends FunSuite with Matchers {

//   test("`map` should not compile when the type parameter of `FlawedOption` is invariant") {
//      """
// def map[B](f: A => B): FlawedOption[B] = this match {
//     case Some(a) => Some(f(a))
//     case None => None
// }
// """ shouldNot compile
//   }

//   test("`getOrElse` should not compile when the type parameter of `FlawedOption` is invariant") {
//     """
// def getOrElse(default: => A): A = this match {
//     case Some(a) => a
//     case None => default
// }
// """ shouldNot compile
//   }
  
  
// }
