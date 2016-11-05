package com.datascience.education.tutorialAnswer.lecture4a

import cats.data.Xor
import cats.data.Xor.Left
import cats.data.Xor.Right

import cats.Applicative
import cats.std.list._

import cats.data.NonEmptyList

object TraverseXor {

  /*
   See note at top of `TraverseOption`, regarding why we should not use 
   `traverse` method built into `Xor`: 
   http://typelevel.org/cats/api/index.html#cats.data.Xor@traverse[F[_],AA%3E:A,D]%28f:B=%3EF[D]%29%28implicitF:cats.Applicative[F]%29:F[cats.data.Xor[AA,D]]


   Note the signature of `Xor.traverse`:
   given Xor[E, A],
   and F[_] = List[B]
   the signature of `traverse` is:
   (A => List[B]): List[Xor[E, B]]

   This is the inverse of what we want --- Xor[List[E, B]]

   This means that the `traverse` function we seek belongs to List.
   The return type of this sought-after `traverse` function is
   Xor[List[E, B]].  This matches up with `traverse` implemented on FPiS'
   `Either`.

   Scala Collections' `List` does not provide `traverse`.
   
   Cats' `Applicative` does.
   
   */

  /*
   `XorException` is the same trick introduced in chapter 12 of FP in Scala.

   The type inside Applicative may only have one generic (like Option),
   but Xor has two.
   So we must remove one generic from Xor.  XorException is more
   limited than Xor because `Exception` is "hard-wired" into it.
   */
  type XorException[B] = Xor[Exception, B]
  type XorListException[B] = Xor[NonEmptyList[Exception], B]

  implicit val applicativeXorException = new Applicative[XorException] {
    def pure[A](a: A): XorException[A] = Right(a)

    // Members declared in cats.Apply
    def ap[A, B](ff: XorException[A => B])(fa: XorException[A]): XorException[B] =
      (ff, fa) match {
        case (Right(ab), Right(a)) => Right(ab(a))
        case (left1 @ Left(_), _) => left1: XorException[B]
        case (_, left2 @ Left(_)) => left2: XorException[B]
      }

    // Members declared in cats.Cartesian
    override def product[A, B](fa: XorException[A], fb: XorException[B]): XorException[(A, B)] =
      (fa, fb) match {
        case (Right(a), Right(b)) => Right((a, b))
        case (left1 @ Left(_), _) => left1: XorException[(A, B)]
        case (_, left2 @ Left(_)) => left2: XorException[(A, B)]
      }

    // Members declared in cats.Functor
    override def map[A, B](fa: XorException[A])(f: A => B): XorException[B] =
      fa.map(f)

  }

  def traverse[A, B](listA: List[A])(f: A => XorException[B]): XorException[List[B]] =
    listInstance.traverse(listA)(f)(applicativeXorException)

  def sequence[A](lxa: List[XorException[A]]): XorException[List[A]] =
    listInstance.traverse(lxa) { (xa: XorException[A]) => xa }(applicativeXorException)

  implicit val applicativeXorListException = new Applicative[XorListException] {
    def pure[A](a: A): XorListException[A] = Right(a)

    // Members declared in cats.Apply
    def ap[A, B](ff: XorListException[A => B])(fa: XorListException[A]): XorListException[B] =
      (ff, fa) match {
        case (Right(ab), Right(a)) => Right(ab(a))
        case (left1 @ Left(_), _) => left1: XorListException[B]
        case (_, left2 @ Left(_)) => left2: XorListException[B]
      }

    // Members declared in cats.Cartesian
    override def product[A, B](fa: XorListException[A], fb: XorListException[B]): XorListException[(A, B)] =
      (fa, fb) match {
        case (Right(a), Right(b)) => Right((a, b))
        case (left1 @ Left(_), _) => left1: XorListException[(A, B)]
        case (_, left2 @ Left(_)) => left2: XorListException[(A, B)]
      }

    // Members declared in cats.Functor
    override def map[A, B](fa: XorListException[A])(f: A => B): XorListException[B] =
      fa.map(f)

  }

  def traverse2[A, B](listA: List[A])(f: A => XorListException[B]):
      XorListException[List[B]] =
    listInstance.traverse(listA)(f)(applicativeXorListException)

  def sequence2[A](lxa: List[XorListException[A]]):
      XorListException[List[A]] =
    listInstance.traverse(lxa) { (xa: XorListException[A]) =>
      xa }(applicativeXorListException)

}
