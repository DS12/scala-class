
package com.datascience.education.tutorial.lecture4





object Covariance {
  sealed trait Coin
  case object Heads extends Coin
  case object Tails extends Coin

  // 4a

  sealed trait LinkedListInt
  case class ConsInt(h: Int, t: LinkedListInt) extends LinkedListInt
  case object NilInt extends LinkedListInt

  /*
   Demonstrations of generics without covariance - flawed design
   */



  // 4b
  sealed trait LinkedList1[A]
  case class Cons1[A](h: A, t: LinkedList1[A]) extends LinkedList1[A]
  /* bad form;
   a different instance of NilLL for every empty LinkedList?
   */
  case class Nil1[A]() extends LinkedList1[A]

  /*
   We want to re-use the same empty list, like Scala Collections:
   http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.Nil$
   */


  // 4c
  sealed trait LinkedList2[A] {
    def prepend(a: A): LinkedList2[A] = Cons2(a, this)
  }
  case class Cons2[A](h: A, t: LinkedList2[A]) extends LinkedList2[A]

  /*
   Nil2 is an immutable, singleton object that will be re-used
   by LinkedList2's of many different types:
   LinkedList2[Int],
   LinkedList2[String],
   etc.

   A case object cannot create a generic type like a case class,
   explaining why this is not Nil2[A]

   `Nothing` is a subtype of every type,
   so is an apporiate concrete type to fill generic `A`
   in LinkedList2[A].
   */
  case object Nil2 extends LinkedList2[Nothing]


  // 4d
  sealed trait LinkedList3[+A] {
    def map[B](f: A => B): LinkedList3[B] = this match {
      case Cons3(h, t) => Cons3(f(h), t.map(f))
      case Nil3 => Nil3
    }

    // def prepend(a: A): LinkedList3[A] =
    //   Cons3(a, this)

  }
  case class Cons3[+A](h: A, t: LinkedList3[A]) extends LinkedList3[A]
  case object Nil3 extends LinkedList3[Nothing]


  // 4e
  sealed trait LinkedList4[+A] {
    def map[B](f: A => B): LinkedList4[B] = this match {
      case Cons4(h, t) => Cons4(f(h), t.map(f))
      case Nil4 => Nil4
    }

    def prepend[B >: A](b: B): LinkedList4[B] =
      Cons4(b, this)

  }
  case class Cons4[+A](h: A, t: LinkedList4[A]) extends LinkedList4[A]
  case object Nil4 extends LinkedList4[Nothing]



}
