package com.datascience.education.tutorial.lecture4

import cats.Traverse
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
    Traverse[List].traverse(listA)(f)(applicativeOption)

  def sequence[A](loa: List[Option[A]]): Option[List[A]] =
    Traverse[List].traverse(loa) { (oa: Option[A]) => oa }(applicativeOption)

}

object TraverseExample extends App {
  import TraverseOption._

  val capitalLetterCodes: List[Int] = List(65, 66, 88, 89, 90)

  println("ASCII codes of a few capital letters")
  println(capitalLetterCodes)

  def capitalLetter(i: Int): Option[Char] =
    if (i >= 65 && i <= 90) Some(i.toChar)
    else None

  println("Use `traverse` to apply `capitalLetter(i: Int): Option[Char]` and receive Option[List[Char]]")

  val allCap: Option[List[Char]] =
    traverse(capitalLetterCodes)(capitalLetter)

  println(allCap)

  println("-------------------------")

  println("ASCII codes of some upper case letters and other ASCII characters")
  val mixedLetterCodes: List[Int] = List(60, 61, 62, 65, 66, 88, 89, 90, 98)
  println(mixedLetterCodes)

  println("Use `traverse` to apply `capitalLetter(i: Int): Option[Char]` and receive Option[List[Char]]")

  val severalCap: Option[List[Char]] =
    traverse(mixedLetterCodes)(capitalLetter)

  println(severalCap)

}

object SequenceExample extends App {
  import TraverseOption._

  val capitalLetterCodes: List[Int] = List(65, 66, 88, 89, 90)

  println("ASCII codes of a few capital letters")
  println(capitalLetterCodes)

  def capitalLetter(i: Int): Option[Char] =
    if (i >= 65 && i <= 90) Some(i.toChar)
    else None

  println("mapped to capital letters:")

  val capitalLetters: List[Option[Char]] =
    capitalLetterCodes.map { (i: Int) => capitalLetter(i) }
  println(capitalLetters)

  println("Use `sequence` to invert the containers  List[Option[Char]] => Option[List[Char]]: ")
  val optionListCapitalLetters: Option[List[Char]] =
    sequence(capitalLetters)
  println(optionListCapitalLetters)

  println("-------------------------")

  println("ASCII codes of some upper case letters and other ASCII characters")
  val mixedLetterCodes: List[Int] = List(60, 61, 62, 65, 66, 88, 89, 90, 98)
  println(mixedLetterCodes)

  println("Only the ASCII codes for upper case letters are converted to characters: ")
  val mixedLetters: List[Option[Char]] =
    mixedLetterCodes.map { (i: Int) => capitalLetter(i) }
  println(mixedLetters)

  println("Use sequence to invert the containers  List[Option[Char]] => Option[List[Char]]: ")
  val notAllCapital: Option[List[Char]] =
    sequence(mixedLetters)
  println(notAllCapital)

}

