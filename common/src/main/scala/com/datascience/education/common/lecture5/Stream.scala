package com.datascience.education.common.lecture5

trait Stream[+A] {
  import Stream._

  def foldRight[B](z: => B)(f: (A, => B) => B): B = // The arrow `=>` in front of the argument type `B` means that the function `f` takes its second argument by name and may choose not to evaluate it.
    this match {
      case Cons(h,t) => f(h(), t().foldRight(z)(f)) // If `f` doesn't evaluate its second argument, the recursion never occurs.
      case _ => z
    }

  def append[B>:A](s: => Stream[B]): Stream[B] =
    foldRight(s)((h,t) => cons(h,t))

  def flatMap[B](f: A => Stream[B]): Stream[B] = {
    def g(a: A, sb: => Stream[B]) = f(a).append(sb)

    foldRight(empty[B])(g)
  }

  def map[B](f: A => B): Stream[B] = {
    def g(a: A, sb: => Stream[B]): Stream[B] = cons(f(a), sb)

    foldRight(empty[B])(g)
  }

  def tail: Stream[A] = this match {
    case cons(h, t) => t
    case empty => empty
  }

  def zip[B](streamB: Stream[B]): Stream[(A, B)] = {
    val initialState: (Stream[A], Stream[B]) =
      (this, streamB)

    def f(s: (Stream[A], Stream[B])):
        Option[((A, B), (Stream[A], Stream[B]))] = s match {
      case (cons(a, tailA), cons(b, tailB)) =>
        Some(((a, b), (tailA, tailB)))
      case (empty, _) => None
      case (_, empty) => None
    }
    unfold(initialState)(f): Stream[(A, B)]

  }

  def zipAll[B](s2: Stream[B]): Stream[(Option[A], Option[B])] = {
    val s = (this, s2)
    def f(tuple: (Stream[A], Stream[B])):
        Option[((Option[A], Option[B]), (Stream[A], Stream[B]))] =
      tuple match {
        case (cons(a, ta), cons(b, tb)) => Some((Some(a), Some(b)), (ta, tb))
        case (cons(a, ta), empty) => Some((Some(a), None), (ta, empty))
        case (empty, cons(b, tb)) => Some((None, Some(b)), (empty, tb))
        case _ => None
      }

    unfold(s)(f)
  }

  def zipWith[B,C](s2: Stream[B])(f: (A,B) => C): Stream[C] =
    unfold((this, s2)) {
      case (Cons(h1,t1), Cons(h2,t2)) =>
        Some((f(h1(), h2()), (t1(), t2())))
      case _ => None
    }
  

  def merge[B >: A](s2: Stream[B]): Stream[B] = {
    val zipped: Stream[(Option[A], Option[B])] = this.zipAll(s2)

    zipped.flatMap { (tuple: (Option[A], Option[B])) =>
      //println("flatten this tuple: "+tuple)
      tuple match {
        case (Some(a: A), Some(b: B)) => cons(a, unit(b))
        case (Some(a: A), None) => unit(a)
        case (None, Some(b: B)) => unit(b)
        case (None, None) => empty
      }
    }
  }

  def enumerate: Stream[(Int, A)] =
    from(0).zip(this)

  def exists(p: A => Boolean): Boolean =
    foldRight(false)((a, b) => p(a) || b)

  def find(p: A => Boolean): Option[A] = {
    def f(a: A, op: => Option[A]) = if(p(a)) Some(a) else op

    foldRight(None: Option[A])(f)
  }

  def filter(f: A => Boolean): Stream[A] =
    foldRight(empty[A])((h,t) =>
      if (f(h)) cons(h, t)
      else t)
  

  def printRecursively(upTo: Int): Unit = this match {
    case cons(h, lazyTail) if upTo > 1 =>
      println(h)
      lazyTail.printRecursively(upTo - 1)
    case cons(h, lazyTail) if upTo <= 1 =>
      println(h)
    case Empty => println("reached end of stream")
  }

  def take(n: Int): Stream[A] = this match {
    case Cons(h, t) if n > 1 => cons(h(), t().take(n - 1))
    case Cons(h, _) if n == 1 => cons(h(), empty)
    case _ => empty
  }
  

  def takeRecursive(n: Int): Stream[A] = this match {
    case cons(h, t) if n > 0 =>
      cons(h, t.take(n - 1))
    case _ if n == 0 => Stream.empty
    case Empty => Stream.empty
  }

  def takeViaUnfold(n: Int): Stream[A] =
    unfold((this,n)) {
      case (Cons(h,t), 1) => Some((h(), (empty, 0)))
      case (Cons(h,t), n) if n > 1 => Some((h(), (t(), n-1)))
      case _ => None
    }
  
  def print(n: Int): Unit = {
    def f(a: A, remaining: => Int): Int = {
      Predef.print(a + " ")
      remaining - 1
    }
    this.take(n).foldRight(n)(f)

    println()
  }
  

  /*
   Not possible to implement print with `unfold` as
   it would give Stream[Unit]
   */

  def headOption: Option[A] = {
    def f(a: A, op: => Option[A]) = Some(a)
    this.foldRight(None: Option[A])(f)
  }


  /*
  from FP in Scala answers
  The function can't be implemented using `unfold`, since `unfold` generates elements of the `Stream` from left to right. It can be implemented using `foldRight` though.

  The implementation is just a `foldRight` that keeps the accumulated value and the stream of intermediate results, which we `cons` onto during each iteration. When writing folds, it's common to have more state in the fold than is needed to compute the result. Here, we simply extract the accumulated list once finished.
  */
  def scanRight[B](z: B)(f: (A, => B) => B): Stream[B] =
    foldRight((z, Stream.unit(z)))((a, p0) => {
      // p0 is passed by-name and used in by-name args in f and cons. So use lazy val to ensure only one evaluation...
      lazy val p1 = p0
      val b2 = f(a, p1._1)
      (b2, cons(b2, p1._2))
    })._2


  def toListFinite(n: Int): List[A] = {
    def f(a: A, la: => List[A]) = a::la

    this.take(n).foldRight(List[A]())(f)
  }

}


case object Empty extends Stream[Nothing]
case class Cons[+C](h: () => C, t: () => Stream[C])
  extends Stream[C]

object Stream {

  def empty[C]: Stream[C] = Empty
  object cons {
    def apply[C](hd: => C, tl: => Stream[C]): Stream[C] = {
      // println("cons apply")
      lazy val head = hd
      lazy val tail = tl
      Cons(() => head, () => tail)
    }

    def unapply[C](cs: Cons[C]): Option[(C, Stream[C])] =
      Some((cs.h(), cs.t()))
  }

  def unit[A](a: => A): Stream[A] = cons(a, empty)

  def apply[A](as: A*): Stream[A] =
    if (as.isEmpty) empty
    else cons(as.head, apply(as.tail: _*))
  

  def listToStream[A](la: List[A]): Stream[A] =
    unfold(la){(listA: List[A]) => listA match {
      case h::t => Some((h, t))
      case Nil => None
    }
    }

  def unfold[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] =
    f(z) match {
      case Some((h,s)) => cons(h, unfold(s)(f))
      case None => empty
    }
  

  def from(i: Int): Stream[Int] = cons(i, from(i + 1))



}


object StreamExamples extends App {

  import Stream._

  def countFrom(n: Int): Stream[Int] =
    unfold(n)((n0: Int) => Some(n0, n0+1))

  def countFromTo(lowerInclusive: Int, upperExclusive: Int):
      Stream[Int] =
    unfold(lowerInclusive){(n0: Int) =>
      if (n0 < upperExclusive) Some(n0, n0+1)
      else None
    }


  println("count from 5, print 6 elements")
  countFrom(5).print(6)

  println("count from 5 until 8, print 6 elements")
  countFromTo(5,8).print(6)
  println("------------------------------")

  def sawtooth(upperBoundExclusive: Int): Stream[Int] =
    Stream.unfold(0){(i: Int) =>
      Some((i, (i + 1) % upperBoundExclusive))}

  val sawtooth7: Stream[Int] = sawtooth(7)

  sawtooth7.print(14)


  println("find 4 in sawtooth function")
  println(sawtooth7.find((i: Int) => i==4))

  println("------------------------------")
  println("foldRight examples")

  val ints = from(0)

  def limitInts(i: Int, u: => Unit): Unit =
    if(i<=20) {
      println(i)
      u
    }

  ints.foldRight(())(limitInts)


  println("------------------------------")
  println("Append one infinite streams to another infinite stream")
  val sawtooth12To19: Stream[Int] = sawtooth7.map { i => i+12 }

  val appended = sawtooth7.append(sawtooth12To19)

  appended.print(32)

  val appended2 = sawtooth7.take(15).append(sawtooth12To19)

  println("first Stream limited by `take`")
  appended2.print(32)


  println("------------------------------")
  println("Merging/interspersing Streams")

  /*
   Broken when `take` not used.  Endless cycle of appends
   */
  val merged: Stream[Int] =
    sawtooth7.take(64).merge(sawtooth12To19.take(15))

  merged.print(64)

  println("------------------------------")
  println("head option")

  val emptyOp = Stream.empty[Int].headOption
  println(emptyOp)

  val sawtoothHeadop = sawtooth12To19.headOption
  println(sawtoothHeadop)

  println("------------------------------")
  println("Fibonacci")

  def fibonacciHelper(a: Int, b: Int): Stream[Int] =
    Stream.cons(a, fibonacciHelper(b, a+b))

  val fibonacci: Stream[Int] = fibonacciHelper(0, 1)

  fibonacci.print(22)


  println("---------------------------------")


  val zeroes: Stream[Int] = Stream.cons(0, zeroes)
  val oneZero = Stream.cons(1, zeroes)

  oneZero.take(16).print(12)

  println("----------------------------------")
  println("scanRight")

  def foo(i: Int, acc: => Double) = (0.5*i) + acc

  oneZero.take(6).scanRight(0.0)(foo).print(12)


  println("----------------------------------")


  println("decay")



  println(oneZero.take(6).foldRight(0.0)(foo))


  println("----------------------------------")
  
  println("scanRight")

  def bar(i: Int, acc: => Double) = i + acc
  oneZero.take(6).scanRight(0.0)(bar).print(12)


  println("----------------------------------")
  println("scanRight")

  def baz(i: Int, acc: => Int) = i + acc

  Stream.from(0).take(6).scanRight(0)(baz).print(12)
  

}
