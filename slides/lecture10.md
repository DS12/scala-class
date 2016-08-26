
#Lecture 10: Monoids


---

"In abstract algebra, a branch of mathematics, a monoid is an algebraic structure with

(1) a single associative binary operation

and

(2) an identity element."

<br />
<br />

[https://en.wikipedia.org/wiki/Monoid](https://en.wikipedia.org/wiki/Monoid)

---

	!scala
	trait Monoid[A] {
	  def op(a1: A, a2: A): A
	  def zero: A
    }

---

Note:
Our usual combinators are not inside the `Monoid` trait.

Every lecture so far has repeated the pattern:

* Give a container, like `Option`, or function, like `Rand`
* implement the usual combinators on it

    * `unit`
    * `flatMap`
    * `map`
    * `map2`

*`Monoid` is a departure from this pattern.*

---

Strings under concatenation form a Monoid

	!scala
	trait Monoid[A] {
	  def op(a1: A, a2: A): A
	  def zero: A
    }

	val monoidString = new Monoid[String] {
      def op(s1: String, s2: String) = s1 + s2
      val zero = ""
    }

<br />
<br />

in `common.lecture10.Monoid`

---

	!scala
	val foo = "foo"
	val bar = "bar"
	val foobar = monoidString.op(foo,bar)
	println(foobar)
	// prints "foobar"

<br />
<br />
<br />
<br />

in `slideCode.lecture10.BasicExamples`

---

String Monoid and `foldLeft`

	!scala
	val words: List[String] =
	  List("foo", "bar", "baz", "biz")

	val foobarbazbiz: String =
      words.foldLeft(monoidString.zero)
	                (monoidString.op)

	println(foobarbazbiz)
	// prints "foobarbazbiz"

<br />
<br />
<br />
<br />

in `slideCode.lecture10.BasicExamples`

---

Integers under addition form a monoid

	!scala
    val monoidIntAddition: Monoid[Int] =
	  new Monoid[Int] {
        def op(i1: Int, i2: Int): Int = i1 + i2
        val zero: Int = 0
      }

<br />
<br />
in `common.lecture10.Monoid`

---

Monoids are the natural algebraic basis for fold operations.

	!scala
	val ints = (0 to 10).toList

	val s = ints.foldLeft(monoidIntAddition.zero)
	                     (monoidIntAddition.op)

	println(s)
	// 55

<br />
<br />
<br />

in `slideCode.lecture10.BasicExamples`

---

# Option

	!scala
	sealed trait FPOption[+A] {
	  def orElse[B >: A](otherOp: => FPOption[B]):
	    FPOption[B] =
        this match {
          case Some(get) => this
          case None => otherOp
        }
      ...
	}
<br />
<br />

in `common.lecture4.FPOption`

---
Options form a Monoid using `None` and `orElse`.

	!scala
	def monoidOption[A]: Monoid[FPOption[A]] =
      new Monoid[FPOption[A]] {
        def op(a1: FPOption[A], a2: FPOption[A]):
		  FPOption[A] =
          a1.orElse(a2)

	    val zero: FPOption[A] = None
      }

<br />
<br />

in `common.lecture10.Monoid`


---

	!scala
	val listOptions = List(Some(6), None,
	                       Some(8), Some(9),
						   None, Some(11))

	val folded: FPOption[Int] =
	  listOptions.foldLeft(monoidOption[Int].zero)
	                      (monoidOption[Int].op)

	println(folded)
	// prints "Some(6)"

<br />
Note that `orElse` is "left-biased."  Which `Some` in the list would be returned if `orElse` were "right-biased"?
<br />
<br />
<br />

in `slideCode.lecture10.BasicExamples`

---
# Exercise

In the monoid of Options under `orElse`, `None` is the identity element.  Why?

	!scala
	def monoidOption[A]: Monoid[FPOption[A]] =
      new Monoid[FPOption[A]] {
        def op(a1: FPOption[A], a2: FPOption[A]):
		  FPOption[A] =
          a1.orElse(a2)

	    val zero: FPOption[A] = None
      }

---

---

	!scala
	a1.orElse(None) = a1

and

	!scala
	None.orElse(a2) = a2


---
Endofunctions under composition form a monoid

	!scala
	def endoMonoid[A]: Monoid[A=>A] =
	  new Monoid[A=>A] {
        def op(f1: A=>A, f2: A=>A): A=>A =
          (a: A) => f2(f1(a))

        def zero: A=>A =
          (a: A) => a
      }

<br />
<br />
in `common.lecture10.Monoid`

---
# Every Monoid has a [*dual*](https://en.wikipedia.org/wiki/Dual_%28category_theory%29)

	!scala
	def dual[A](m: Monoid[A]): Monoid[A] =
	  new Monoid[A] {
        def op(x: A, y: A): A = m.op(y, x)
        val zero = m.zero
      }

Dual of our Option Monoid

	!scala

	def monoidOption[A]: Monoid[FPOption[A]] = ...

	def monoidOptionDual[A]: Monoid[FPOption[A]] =
	  dual(monoidOption[A])

---
# Exercise

Which Option from the List will `println` print?

	!scala
	val listOptions = List(Some(6), None, Some(8),
	                       Some(9), None, Some(11))

	val foldedByDual: FPOption[Int] =
	  listOptions.foldLeft(monoidOptionDual[Int].zero)
	                      (monoidOptionDual[Int].op)

	println(foldedByDual)
	// ???

<br />
<br />
in `slideCode.lecture10.BasicExamples`

---

# Exercise

What is the implementation of `op` in the dual of an endofunction monoid?

	!scala
	def dualEndo[A]: Monoid[A=>A] =
	  dual[A=>A](endoMonoid[A])


---

---

# Answer

	!scala
	dualEndo[A] {
      def op(f1: A=>A, f2: A=>A): A=>A =
        (a: A) => f1(f2(a))

	  def zero: A=>A =
	    (a: A) => a
	}

---


---

# Exercise

Under what condition will `m` and `dual(m)` be the same?

---


# Monoid Isomorphism

A homomorphism between two monoids (M, *) and (N, •) is a function f : M → N such that

$$ f(x * y) = f(x) • f(y) for all x, y in M $$

$$ f(e_M) = e_N $$

where $e_M$ and $e_N$ are the identities on M and N respectively.

A bijective monoid homomorphism is called a monoid isomorphism.

Two monoids are said to be isomorphic if there is a monoid isomorphism between them.
<br />
<br />
<br />
<br />
<br />
<br />

[https://en.wikipedia.org/wiki/Monoid#Monoid_homomorphisms](https://en.wikipedia.org/wiki/Monoid#Monoid_homomorphisms)

---

Booleans under `or` form a Monoid

	!scala
	val booleanOr: Monoid[Boolean] =
	  new Monoid[Boolean] {
        def op(x: Boolean, y: Boolean) = x || y
        val zero = false
      }

Booleans under `and` form a Monoid

	!scala
    val booleanAnd: Monoid[Boolean] =
	  new Monoid[Boolean] {
        def op(x: Boolean, y: Boolean) = x && y
        val zero = true
      }

<br />
in `slideCode.lecture10.BooleanIsomorphism`

---

	!scala
	val booleans: List[Boolean] =
	  List(true, true, false, true, false)

	val reducedOr = booleans.reduce(booleanOr.op)
	println(reducedOr)
	// true

	val reducedAnd = booleans.reduce(booleanAnd.op)
	println(reducedAnd)
	// false

<br />
<br />
in `slideCode.lecture10.BooleanIsomorphismExamples`

---

	!scala
	// x && y == !((!x)||(!y))
	// x || y == !((!x)&&(!y))
	def booleanIsomorphism(mb: Monoid[Boolean]):
	    Monoid[Boolean] =
      new Monoid[Boolean] {
        def op(x: Boolean, y: Boolean) =
          !mb.op(!x, !y)
		def zero = !mb.zero
      }

	val booleanOr2: Monoid[Boolean] =
	  booleanIsomorphism(booleanAnd)
	val booleanAnd2: Monoid[Boolean] =
	  booleanIsomorphism(booleanOr)

in `slideCode.lecture10.BooleanIsomorphism`

---

	!scala
	val booleans =
	  List(true, true, false, true, false)

	val reducedOr2 =
	  booleans.foldLeft(booleanOr2.zero)
	                   (booleanOr2.op)
    // true

	val reducedAnd2 =
	  booleans.foldLeft(booleanAnd2.zero)
	                   (booleanAnd2.op)
	// false

<br />
<br />
in `slideCode.lecture10.BooleanIsomorphismExamples`


---


# Derived monoids

	!scala
	def monoidFunction[A,B](monoidB: Monoid[B]):
	  Monoid[A => B] = new Monoid[A => B] {
		def op(f1: A => B, f2: A => B): A => B =
		  (a: A) => {
		    val b1: B = f1(a)
		    val b2: B = f2(a)
		    val b3: B = monoidB.op(b1, b2)
		    b3
		  }
		def zero: Function1[A,B] =
		  (a: A) => monoidB.zero
		}
	}

in `common.lecture10.Monoid`

---

	!scala
	def monoidProduct[A,B](mA: Monoid[A],
	                       mB: Monoid[B]) =
	  new Monoid[(A,B)] {
        def op(ab1: (A,B), ab2: (A,B)):
	        (A,B) = {
          val a3: A = mA.op(ab1._1, ab2._1)
          val b3: B = mB.op(ab1._2, ab2._2)
          (a3, b3)
        }
        val zero: (A,B) = (mA.zero, mB.zero)
      }

in `common.lecture10.Monoid`

---

#Example: Orders

	!scala
	case class Order(price: Double, amt: Double)
	object Order {
		implicit val monoid: Monoid[Order] =
			new Monoid[Order] {
		def combine(o1: Order, o2: Order) =
			new Order(o1.price + o2.price, o1.amt + o2.amt)
		def empty = new Order(0, 0) }
	}

---

#Example: Counting

We can use the monoid of integers under addition to count the elements of a `List`.

	!scala
	val listChars: List[Char] =
	  List('f','o','o','b','a','r')

	val listCounts: List[Int] = listChars.map(_ => 1)

	val charCount =
	  listCounts.foldRight(monoidIntAddition.zero)
	                      (monoidIntAddition.op)

	// 6
<br />
<br />
in `slideCode.lecture10.CountingSimpleExamples`


---

Refactor into a `def` so we can count the elements of any `List`

	!scala
	def countBasic[A](listA: List[A]): Int = {
      val listCount: List[Int] = listA.map(_ => 1)
      listCount.foldLeft(monoidIntAddition.zero)
	                    (monoidIntAddition.op)
    }


`countBasic` maps elements of type `A` to "counts" of type `Int`.  

These `Ints` can then be reduced by the monoid of integers under addition.


<br />
<br />
<br />
<br />
<br />
<br />
in `slideCode.lecture10.CountSimple`

---
# foldMap

Generalize `countBasic`:

`foldMap` maps elements of type `A` to elements of type `B`.

These `B` elements can then be reduced by `Monoid[B]`.

	!scala
	def foldMap[A, B](as: List[A], m: Monoid[B])
	                 (f: A => B): B =
      as.foldLeft(m.zero)((b, a) => m.op(b, f(a)))

<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
in `slideCode.lecture10.FoldList`

---

	!scala
	val listChars: List[Char] =
	  List('f','o','o','b','a','r')
	foldMap(listA, monoidIntAddition)((_: A) => 1)

	// 6

<br />
<br />
in `slideCode.lecture10.CountSimple` and `slideCode.lecture10.CountingExamples`

---

The notion of a monoid is closely related to folding.


Folding necessarily implies a binary associative operation that has an initial value.

A fold is a specific type of [catamorphism](https://en.wikipedia.org/wiki/Catamorphism).


---

#Example: foldRight

	!scala
	def foldMap[A,B](listA: List[A], mb: Monoid[B])
		(f: Function1[A,B]): B = {
		def g(b: B, a: A): B = mb.op(b, f(a))
		foldLeft(listA)(mb.zero)(g)
	}
	def foldRight[A,B](listA: List[A])
		(z: B)
		(f: (A, B) => B): B = {
	    val g: A => (B => B) = f.curried
	    foldMap(listA, (endoMonoid[B]))(g)(z)
	}

---
# Example: Averaging


	!scala
    (a.avg, a.count) + (b.avg, b.count) =
	   ((a.count*a.avg + b.count*b.avg)/
	      (a.count + b.count),
		a.count + b.count)

---

	!scala
	def op(
      a: (Double, Int),
      b: (Double, Int)
    ): (Double, Int) = {
      val cCount: Int = a._2 + b._2
      val cAverage: Double =
        (a._2.toDouble * a._1 + b._2.toDouble * b._1) / cCount
      (cAverage, cCount)
    }

---

	!scala
	val monoidAverageAndCount:
	  Monoid[(Double, Int)] =
      new Monoid[(Double, Int)] {
      def op(
        a: (Double, Int),
        b: (Double, Int)
      ): (Double, Int) = ...

      def zero: (Double, Int) =
        (0.0, monoidIntAddition.zero)

    }

<br />
<br />
in `slideCode.lecture10.Homomorphism`

---

	!scala

	val monoidAverageAndCount:
	  Monoid[(Double, Int)] = ...

	val doubles = (0 to 18).toList.
	              map(i => i.toDouble/10)

    foldMap(doubles, monoidAverageAndCount)
	       {(d: Double) => (d, 1)}

	// (0.8999999999999999,19)

<br />
<br />
in `slideCode.lecture10.HomomorphismExample`

---

---

---

---

The free monoid on a set A corresponds to lists of elements from A with concatenation as the binary operation.
<br />
<br />
A monoid homomorphism from the free monoid to any other monoid $(M,\cdot)$ is a function $f$ such that
$$
f(x_1 \dots x_n) = f(x_1) \cdot \dots \cdot f(x_n)
f() = e
$$
where $e$ is the identity on $M$.

---

Every such homomorphism corresponds to a map operation applying f to all the elements of a list, followed by a fold operation which combines the results using the binary operator.
<br />
<br />
This computational paradigm has inspired the MapReduce software framework.


---
# Generalize away from List

	!scala
	object FoldList {

      def foldMap[A,B](listA: List[A],
	                   monoidB: Monoid[B])
					   (f: Function1[A,B]): B = ...
	  def foldLeft[A, B](as: List[A])
	                    (z: B)
					    (f: (B, A) => B): B = ...
    }

---
# Generalize away from List

	!scala
	trait Foldable[F[_]] {

	  def foldLeft[A, B](fA: F[A])
	                    (z: B)
					    (f: (B, A) => B): B = ...
    }



---

#Homework

Have a look at [Algebird](https://github.com/twitter/algebird).
