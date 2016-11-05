package com.datascience.education.tutorialAnswer.lecture4a

import cats.data.Xor
import cats.data.Xor.Left
import cats.data.Xor.Right

import cats.data.NonEmptyList

object XorList {
  import TraverseXor._
  // type XorListException[B] = Xor[NonEmptyList[Exception], B]

  def map2[A, B, C](xorA: XorException[A], xorB: XorException[B])(f: (A, B) => C): XorListException[C] = (xorA, xorB) match {
    case (Left(e1), Left(e2)) =>
      Left(NonEmptyList(e1, e2)): XorListException[C]
    case (Left(e1), Right(b)) =>
      Left(NonEmptyList(e1)): XorListException[C]
    case (Right(a), Left(e2)) =>
      Left(NonEmptyList(e2)): XorListException[C]
    case (Right(a), Right(b)) =>
      Right(f(a, b))
  }

}
