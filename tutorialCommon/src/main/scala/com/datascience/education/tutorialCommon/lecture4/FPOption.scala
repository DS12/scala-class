package com.datascience.education.tutorialCommon.lecture4

// Hide Scala's native Option, Some, and None from this namespace
import scala.{ Option => _ }
import scala.{ Some => _ }
import scala.{ None => _ }


/*
 AbstractFPOption is an attempt to unify tests with monomorphism.
 Both exercise and answers would inherit from this trait.
 Spec would be defined on the trait.
 The complication is in keeping the 
 FPOption, FPSome, and FPNone implementations outside of 
 the TutorialCommon sub-project, but defining tests upon them.

 A similar problem was solved for Email and Employee types in another
 lecture 4 exercise, using generics.

 We want to keep these three classes right in front of the student -
 in the tutorial exercises sub-project - 
 rather than hiding them in the tutorial common sub-project where 
 common specs are defined.

 Both this attempt and the typeclass attempt at test unification 
 will be shelved for now.

 */
// trait AbstractFPOption[+A] {
//   import FPOption._

//   def map[B](f: A => B): FPOption[B] = this match {
//     case FPSome(a) => FPSome(f(a))
//     case FPNone => FPNone
//   }

//   // Part (5b)
//   // Answer 5b
//   def getOrElse[B >: A](default: => B): B = this match {
//     case FPSome(a) => a
//     case FPNone => default
//   }



//   // Task 5c
//   // Answer 5c
//   def orElse[B >: A](opB: FPOption[B]): FPOption[B] =
//     this.map((a: A) => FPSome(a)).getOrElse(opB)


//   // Task 5d
//   // Answer 5d
//   def flatMap[B](f: A => FPOption[B]): FPOption[B] =
//     this.map((a: A) => f(a)).getOrElse(FPNone)


//   // Task 5e
//   // Answer 5e
//   def map2[B, C](opB: FPOption[B])(f: (A,B) => C): FPOption[C] =
//     this.flatMap { (a: A) =>
//       opB.map { (b: B) => f(a,b) }
//     }
// }



// import scala.language.higherKinds
// import cats.Monad

// abstract class FPOptionTypeclass[
//   FPOption[_], FPSome[_] <: FPOption[_], FPNone[_] <: FPOption[_]](
//   implicit FPOption : Monad[FPOption]
// ){
//   val optionHello: FPOption[String] = FPOption.pure("hello")
//   val option65: FPOption[Int] = FPOption.pure(65)
//   val option64: FPOption[Int] = FPOption.pure(64)
//   val noInt: FPOption[Int]

//   def capitalLetter(i: Int): FPOption[Char]
  

// }

