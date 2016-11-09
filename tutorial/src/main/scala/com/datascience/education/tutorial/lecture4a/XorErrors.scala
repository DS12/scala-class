package com.datascience.education.tutorial.lecture4a

import cats.data.Xor
import cats.data.Xor.Left
import cats.data.Xor.Right
import cats.data.NonEmptyList


object XorErrors {
  import XorHelpers._
  
  type XorErrors[E, A] = Xor[NonEmptyList[E], A]

  // use Right and Left instead
  // def right[E,A](a: A): XorErrors[Nothing, A] = Right(a)
  // def left[E](e: E): XorErrors[E, Nothing] = Left(NonEmptyList(e))


  // think of map2 as an `And` operation
  def map2[E, A, B, C](xorErrors1: XorErrors[E,A], xorErrors2: XorErrors[E,B])
    (f: Function2[A, B, C]): XorErrors[E,C] =
    (xorErrors1, xorErrors2) match {
      case (Right(a1), Right(b2)) => Right(f(a1,b2))
      case (Left(e1), Right(b2)) => Left(e1)
      case (Right(a1), Left(e2)) => Left(e2)
      case (Left(e1), Left(e2)) => Left(concat(e1, e2))
    }

  // not really good form
  def concatErrors[E]
    (xorErrors1: XorErrors[E,Nothing], xorErrors2: XorErrors[E,Nothing]):
      XorErrors[E,Nothing] = (xorErrors1, xorErrors2) match {
    case (Left(e1), Left(e2)) => Left(concat(e1, e2))
  }
    

  def map4[Err, A, B, C, D, E](xorErrors1: XorErrors[Err, A],
    xorErrors2: XorErrors[Err, B],
    xorErrors3: XorErrors[Err, C],
    xorErrors4: XorErrors[Err, D])
    (f: Function4[A, B, C, D, E]): XorErrors[Err, E] = {

    val xorAB: XorErrors[Err, Tuple2[A,B]] = map2(xorErrors1, xorErrors2)((_,_))
    val xorCD: XorErrors[Err, Tuple2[C,D]] = map2(xorErrors3, xorErrors4)((_,_))

    val g: Function2[Tuple2[A, B], Tuple2[C, D], E] =
      (tupAB: Tuple2[A,B], tupCD: Tuple2[C,D]) => {
        val a: A = tupAB._1
        val b: B = tupAB._2
        val c: C = tupCD._1
        val d: D = tupCD._2
        f(a,b,c,d): E
      }


    val xorE: XorErrors[Err, E] = map2(xorAB, xorCD)(g)

    xorE
  }


  def and[E, A](xorErrors1: XorErrors[E,A], xorErrors2: XorErrors[E,A]): XorErrors[E,A] =
    (xorErrors1, xorErrors2) match {
      case (Right(a1), Right(a2)) => Right(a1)
      case (Left(e1), Right(a2)) => Left(e1)
      case (Right(a1), Left(e2)) => Left(e2)
      case (Left(e1), Left(e2)) => Left(concat(e1, e2))
    }

  def or[E, A](xorErrors1: XorErrors[E,A], xorErrors2: XorErrors[E,A]): XorErrors[E,A] =
    (xorErrors1, xorErrors2) match {
      case (Right(a1), Right(a2)) => Right(a1)
      case (Left(e1), Right(a2)) => Right(a2)  // information lost about Left error
      case (Right(a1), Left(e2)) => Right(a1)  // information lost about Right error
      case (Left(e1), Left(e2)) => Left(concat(e1, e2))
    }





}
