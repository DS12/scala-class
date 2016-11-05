package com.datascience.education.tutorialAnswer.lecture4a

import cats._
import cats.std.all._
import cats.implicits._

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.data.NonEmptyList

import cats.data.ValidatedNel


object ValidatedExtra {


  // val nelStringSemigroup = new SemigroupK[NonEmptyList] {
  //   def combineK[A](x: NonEmptyList[A], y: NonEmptyList[A]): NonEmptyList[A] =
  //     x.combine(y)

  // }

  // until this is fixed `orElse` will be used.
  // Task
  def or[E, A](validated1: Validated[E, A], validated2: Validated[E,A])
    (implicit se: Semigroup[E], sa: Semigroup[A]): Validated[E,A] =
    (validated1, validated2) match {
      case (Invalid(e1), Invalid(e2)) => Invalid(se.combine(e1, e2))
      case (Invalid(_), vad2@Valid(_)) => vad2
      case (vad1@Valid(_), Invalid(_)) => vad1
      case (Valid(a1), Valid(a2)) => Valid(sa.combine(a1, a2))
    }


}


object OrValidatedExample extends App {
  import ValidatedExtra._

  val err1: ValidatedNel[Exception, Int] = Invalid(new Exception("foo")).toValidatedNel
  val err2: ValidatedNel[Exception, Int] = Invalid(new Exception("bar")).toValidatedNel

  // implicit val nelSemigroup = SemigroupK[NonEmptyList]

  val one: ValidatedNel[Exception, Int] = Valid(1).toValidatedNel
  val five: ValidatedNel[Exception, Int] = Valid(5).toValidatedNel
  val six: ValidatedNel[Exception, Int] = Valid(6).toValidatedNel

  // println(or(err1, err2))
  // println(or(err1, five))

}
