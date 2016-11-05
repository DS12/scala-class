package com.datascience.education.tutorial.lecture4a

import scala.Option
import scala.Some
import scala.None

import cats.Applicative
import cats.std.list._

/*
 `traverse` and `sequence` from Chapter 4, 
 implemented on Scala Collections' `Option`
 
 */

object TraverseOption {
  /*
   `traverse2` and `sequence2` are implemented from scratch (`sequence2` uses `traverse2`)
   `traverse` and `sequence` are implemented on top of Cats.
   It is preferable to use the implementation of `traverse`
   provided by Cats.
   
   Cost of Cats implementation
   Cats' `traverse` implementation obfuscates the solution to our challenge
   
   Benefit of Cats implementation
   `traverse` as implemented in FPiS Chapter 4 is simplified.
   (A => Option[B]) => Option[List[B]] *really* should be a combinator on
   `List`, not `Option`.

   Cats and Scalaz both implement it this way.

   FPiS' implementation of `traverse` for both Option and Either is simplified

   */

  /*
   Implement `prepend`, a helper function for `traverse2`
   */
  private def prepend[A](opA: Option[A], opListA: Option[List[A]]): Option[List[A]] =
    opA.flatMap { (a: A) =>
      opListA.map { (listA: List[A]) => a :: listA }
    }

  /*
   Implement `traverse2` using `prepend`.  Do not use `sequence2`.
   */
  def traverse2[A, B](a: List[A])(f: A => Option[B]): Option[List[B]] =
    a.foldRight[Option[List[B]]](Some(List[B]())) { (a: A, opListB: Option[List[B]]) =>
      val opB: Option[B] = f(a)
      prepend(opB, opListB)
    }

  /*
   Implement `sequence2` using `traverse2`.  
   The body of this method is a one-liner.
   */
  def sequence2[A, B](listOpA: List[Option[A]]): Option[List[A]] =
    traverse2(listOpA) { (opA: Option[A]) => opA }

  /*
   Requirement for production-ready implementation of `traverse` and `sequence` below.
   */
  implicit val applicativeOption = new Applicative[Option] {
    def pure[A](a: A): Option[A] = Some(a)
    // Members declared in cats.Apply
    def ap[A, B](ff: Option[A => B])(fa: Option[A]): Option[B] =
      (ff, fa) match {
        case (Some(ab), Some(a)) => Some(ab(a))
        case _ => None
      }
    // Members declared in cats.Cartesian
    override def product[A, B](fa: Option[A], fb: Option[B]): Option[(A, B)] =
      (fa, fb) match {
        case (Some(a), Some(b)) => Some((a, b))
        case _ => None
      }
    // Members declared in cats.Functor
    override def map[A, B](fa: Option[A])(f: A => B): Option[B] =
      fa.map(f)
  }

  /*
     Here, G = Option, and Applicative[G] = Applicative[Option]
   */
  def traverse[A, B](listA: List[A])(f: A => Option[B]): Option[List[B]] =
    listInstance.traverse(listA)(f)(applicativeOption)

  def sequence[A](loa: List[Option[A]]): Option[List[A]] =
    listInstance.traverse(loa) { (oa: Option[A]) => oa }(applicativeOption)

}


