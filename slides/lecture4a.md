
#Lecture 4a: Either, Validation and [*Cats*](https://github.com/typelevel/cats)


![](images/lecture4a/cats.png)



<!--Products and Coproducts -->

<!---
http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html
http://danielwestheide.com/blog/2012/12/26/the-neophytes-guide-to-scala-part-6-error-handling-with-try.html
-->

---

#Either

`Either` is a "chained container" that holds one of two things.

Like `Option`, the possible value inside this chained container can be accessed with `flatMap`.

Like a set of `Option`s, a set of `Eithers` can be chained together with a sequence of `flatMap`s.

`Either` solves our problem from the previous lecture of `Option`'s uninformative `None`.



---
The `Option` trait had two concrete children: `Some` and `None`.  Think of `Some` as the "right" type and `None` as the "left" type.  `Option` has only one generic type.  `Either` has two generic types: left/incorrect and right/correct.

`Either` is not just used for catching errors, just like `None` in `Option` is not always a "bad thing."

---

	!scala
	trait Either[+E,+A]
	case class Left[+E](e: E) extends Either[E, Nothing]
	case class Right[+A](a: A) extends Either[Nothing, A]



A `Left` instance of `Either` will have `Nothing` for its "right" type.

A `Right` instance of `Either` will have `Nothing` for its "left" type.

The generic letter for the "left" type is "E" because it is usually a subtype of Java's `Exception`.

---

# A few familiar combinators

	!scala
	def map[B](f: A => B): Either[E, B]
    def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B]

Given an `Either[Exception, Integer]`, these combinators only let me operate on the `Integer`.  They are *right-biased*.

Usually, we intend to "fail fast" whenever an `Either` becomes `Left[Exception]`.

---

#Usage of `Either` will resemble this

Given a set of `Either`s:

	!scala
	val eitherA: Either[Exception, A] = ...
	val eitherB: Either[Exception, B] = ...
	val eitherC: Either[Exception, C] = ...

a function to produce a `D`:

	!scala
	def f(a: A, b: B, c: C): D = ...

and a sequence of `flatMap`s:

	!scala
	val eitherD: Either[Exception, D] =
	  eitherA.flatMap { a =>
	    eitherB.flatMap { b =>
		  eitherC.map { c =>
		    f(a, b, c)
		  }
	    }
      }
---

Given:

	!scala
	val eitherRecord1: Either[Exception, Record] = Right(record)
	val eitherRecord2: Either[Exception, Record] =
	  Left(RetrievalException("retrieval of Record2 failed"))
	val record3: Either[Exception, Record] = Right(record3)

a function to produce a `Collection`:

	!scala
	def makeCollection(r1: Record, r2: Record, r3: Record): Collection = ...

and a sequence of `flatMap`s:

	!scala
	val eitherCollection = eitherRecord1.flatMap { record1 =>
	  eitherRecord2.flatMap { record2 =>
	    eitherRecord3.flatMap { record3 =>
		  makeCollection(record1, record2, record3)
	    }
	  }
	}

	val eitherCollection =
	  Left(RetrievalException("retrieval of Record2 failed"))
---

Did the prior two examples make full use of "chaining"?  Were the prior examples "sequential"?  No on all counts.  The input `Either`s were not dependent upon each other.

An example that makes better use of "chaining";
given types `A`, `B`, `C`, and `D`:

	!scala
	val eitherA: Either[Exception, A] = ...
	def eitherB(a: A): Either[Exception, B] = ...
	def eitherC(b: B): Either[Exception, C] = ...
	def eitherD(c: C): Either[Exception, D] = ...

and a sequence of `flatMap`s:

	!scala
	val eitherD: Either[Exception, D] =
	  eitherA.flatMap { (a: A) =>
	    eitherB(a).flatMap { (b: B) =>
		  eitherC(b).flatMap { (c: C) =>
		    eitherD(c)
	    }
      }
	// three anonymous functions used here

---

The anonymous functions used in the "chain" on the previous slide are actually redundant.

`=:=` is type eqivalency

	!scala
	def eitherB(a: A): Either[Exception, B] =:= A => Either[Exception, B]
	def eitherC(b: B): Either[Exception, C] =:= B => Either[Exception, C]
	def eitherD(c: C): Either[Exception, D] =:= C => Either[Exception, D]

We don't need to write anonymous functions when `eitherB`, `eitherC`, and `eitherD` already fit the signature of the argument expected by `flatMap`.

The "chain" of the previous slide, rewritten without this redundancy:

	!scala
	val eitherD: Either[Exception, D] =
	  eitherA
	    .flatMap(eitherB)
		.flatMap(eitherC)
		.flatMap(eitherD)

Redundancy is fine if it makes the code "self-documenting," though.

---

Sending a `Request` to a distant server; the `Response` may or may not arrive.  If the `Response` does arrive, its `Payload` may or may not be corrupted.

	!scala
	val request: Request = ...
	def sendRequest(req: Request): Either[Exception, Response] = ...
	def unpackResponse(res: Response): Either[Exception, Payload] = ...


and a sequence of `flatMap`s:

	!scala
	val eitherPayload: Either[Exception, Payload] =
	  sendRequest(request).flatMap { response =>
     	unpackResponse(response)
	  }

Rewritten to not be "hard-wired" to a particular value `request`:

	!scala
	def getPayload(request: Request): Either[Exception, Payload] =
	  sendRequest(request).flatMap { response =>
     	unpackResponse(response)
	  }

---

The left side of `Either` is *typically* an `Exception` of some form, and is usually intended to be the "end of the chain."  To prioritize typical usage, combinators on most implementations of `Either` are "right-biased."

Nevertheless, sometimes we *do* want to operate on the "left" type.  Implementations of `Either` vary by library in how this is provided, if at all.

Scalaz' `Either` provides `leftMap`:

	!scala
	(A \/ B) {
    	def leftMap[C](f: A => C): (C \/ B)
	}


This is particularly useful when the "left" type is not a bad thing like an exception.

`Either` can be used for more than containing exceptions.



---

`def Try` from previous lecture, rewritten for `Either`

	!scala
	def tryEither[A](a: => A): Either[Throwable, A] =
	  try { Right(a) }
	  catch { case t: Throwable => Left(throwable) }

An improvement, taken from `Scalaz`

	!scala
	import scala.util.control.NonFatal

	def tryEither[A](a: => A): Either[Throwable, A] =
	  try { Right(a) }
	  catch { case NonFatal(throwable) => Left(throwable) }



---

# Lower Type Bound Generics

(Lower Type Bounds)

Last lecture, we left unexplained the "the necessity of the lower type bound generic `B` in the `getOrElse` and `orElse` combinators."

	!scala
    trait Option[+A] {
	  .
	  .
	  .
      def getOrElse[B >: A](default: => B): B
      def orElse[B >: A](ob: => Option[B]): Option[B]
      .
    }

---



Here, in `Either`, we see the same pattern; lower type bound generic `EE`:


	!scala
    sealed trait Either[+E,+A] {
      .
	  .
	  .
      def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B]

	  // This combinator has two lower type bound generics:

      def orElse[EE >: E, B >: A](b: => Either[EE, B]): Either[EE, B]
	  .
	  .
    }

---

First, the explanation for the lower type bound generic in `getOrElse` and `orElse` of `Option`.

An instance of `Some[A]` is an `Option[A]`.

The<sup>*</sup> instance of `None` is an `Option[Nothing]`.

	!scala
    case class Some[+A](get: A) extends Option[A]
    case object None extends Option[Nothing]





\* There is only a single instance of `None`, as it is an `object`.

---

[`Nothing`](http://www.scala-lang.org/api/current/index.html#scala.Nothing) is "at the bottom of Scala's type hierarchy.  `Nothing` is a subtype of every other type."

So `Nothing <: A`.

This explains why an `Option[Integer]` can be:

* `Some[Integer]` (`=:= Option[Integer]`)
* `None` (`=:= Option[Nothing]`)

Next stop, explanation of the necessity of this for `getOrElse` and `orElse`:

---

Given

	!scala
	val failed: Option[Nothing] = None

if not for the lower type bound generic `B >: A`, `getOrElse` would be limited to this signature when called on `failed`:

	!scala
	getOrElse(default: => Nothing): Nothing

Not very useful.

With `B >: A`, `getOrElse` on `failed` becomes:

	!scala
	getOrElse[B >: Nothing](default: => B): B

More useful.

---

`B >: A` is a [*lower type bound*](http://docs.scala-lang.org/tutorials/tour/lower-type-bounds.html);

Generic `B` can be a supertype of `A`, or an equal type to `A`.


It is likely you will use [*upper type bounds*](http://docs.scala-lang.org/tutorials/tour/upper-type-bounds.html) more frequently.  I think their use cases are less convoluted, as well.


---

`Option[Nothing] <: Option[A]` and the `+` before the generic is called [*covariance*](https://twitter.github.io/scala_school/type-basics.html#variance).

Lacking the `+` prefix to its generic type, `Option` would be *invariant* and `Option[Nothing] <: Option[A]` would be *false*.  This is default.


If the generic inside `List` is covariant, then:

"If `X` is a subtype of `Y`, then `List[X]` is a subtype of `List[Y]`." -- *FP in Scala*, section 3.1

---

The same explanation of the necessity of the lower type bound generic, for `Either`

An instance of `Left` still has a "right" type: `Nothing`.

An instance of `Right` still has a "left" type: `Nothing`.

	!scala
	case class Left[+E](e: E) extends Either[E, Nothing]
	case class Right[+A](a: A) extends Either[Nothing, A]

If not for the lower type bound generic `EE`, `flatMap` would be limited to this signature when called an a instance of `Left`:

	!scala
	flatMap[B](f: A => Either[Nothing, B]): Either[Nothing, B]

---

Why would you need to call `flatMap` on an instance of `Left`?

Because at run-time your `Either` may not be a `Right` as you expected; it may be a `Left`.

At run-time, an `Either` may be instantiated as a `Left` or `Right`.

But at compile-time, the combinators must type-check for either way this could go.

---

Given a set of `Either`s:

	!scala
	val eitherA: Either[Exception, A] = ...
	val eitherB: Either[Exception, B] = ...
	val eitherC: Either[Exception, C] = ...

a function to produce a `D`:

	!scala
	def f(a: A, b: B, c: C): D = ...

and a sequence of `flatMap`s:

	!scala
	val eitherD: Either[Exception, D] =
	  eitherA.flatMap { a =>
	    eitherB.flatMap { b =>
		  eitherC.map { c =>
		    f(a, b, c)
		  }
	    }
      }

typechecking at compile-time needs to verify the types *"line up"*
if `eitherA` is `Left` or `Right`, if `eitherB` is `Left` or `Right`, etc.

---

You can find an implementation of `Either` in several places:

* In *FP in Scala*, `fpinscala.errorhandling.Either`
* In *Scalaz*, `scalaz.\/`
    * Also called "Disjunction"
    * `scalaz/core/src/main/scala/scalaz/Either.scala`
    * [ScalaDoc](https://oss.sonatype.org/service/local/repositories/releases/archive/org/scalaz/scalaz_2.11/7.2.1/scalaz_2.11-7.2.1-javadoc.jar/!/index.html#scalaz.$bslash$div$)
* In *Cats*, `cats.data.Xor`
    * `cats/core/src/main/scala/cats/data/Xor.scala`
	* [ScalaDoc](http://typelevel.org/cats/api/#cats.data.Xor)
* Natively, `scala.util.Either`
    * [ScalaDoc](http://www.scala-lang.org/api/current/#scala.util.Either)
	* Must use projection (`.left` or `.right`) to access `flatMap`, `map`, etc.; not right-biased; see ScalaDoc example


---
In an earlier Challenge Question for `Option`, we asked you to implement `flatMap` on `Option` using `map`, `orElse` and `getOrElse`.

`orElse` and `getOrElse` are not "generalized" combinators -- they may not appear in all of the Containers equivalent to `Option`.  

`unit` and `flatMap` *are* generalized combinators.  Implement these from scratch (using pattern matching for `flatMap`) for `Either`.

	!scala
	object Either {
    	def unit[A](a: A): Either[Nothing, A] = ???
		// If we pass an eager `A` to `unit`,
		// the left generic is completely unnecesary -- hence `Nothing`.
		// Note, `unit` is not intended to be used to catch errors.
		// If it were, we'd need a type other than `Nothing`.
    }

	trait Either[E, A] {
		def flatMap[B](f: A => Either[E, B]): Either[E, B] = ???
	}



---

In lab, you will implement `map`, `orElse`, and `getOrElse` using `unit` and `flatMap`, our primitives.

---

#Validation

What if we don't want to "fail fast"?  What if we don't need (or want) sequentiality?  Then we should not be using a "chained container" like `Option` or `Either`.

"Failing fast" and "sequential chaining" was a signature feature of the chained containers we've seen previously:

	!scala
	def sendRequest(req: Request): Either[Exception, Response] = ...
	def unpackResponse(res: Response): Either[Exception, Payload] = ...

	def getPayload(request: Request): Either[Exception, Payload] =
	  sendRequest(request).flatMap { response =>
     	unpackResponse(response)
	  }

We'll have to depart from "chained container."

We'll refer to this new "container" as a *"ringed container"*.

I chose the word "ring" because "ringed container" is to [token ring network](https://en.wikipedia.org/wiki/Token_ring) as "chained container" is to "Ethernet".

---

Features you lose with "Ringed Container"

* Failing fast
    * If I do not receive a `Response`, I will not attempt to extract the `Payload` of the `Response`
* The contents of one container as the argument to the next container
    * We are abandoning sequentiality.  With "chained container", each container had to wait on the container prior

---

Features you gain with "Ringed Container"

* Parallelism, rather than sequentiality
* In `Validation`, the ability to collect more than a single `Exception`
    * Perfect for parallelized parsing tasks

---
# map2

Remember `map2`, a combinator of `Option`?  

	!scala
	def map2[A,B,C](a: Option[A], b: Option[B])
	  (f: (A, B) => C): Option[C]

It is critically important to any "ringed container."  If you associate `flatMap` with "chained containers," associate `map2` with "ringed containers."

---


---

We are going to "hone in" on this feature of `Validation`, mentioned earlier:

 * the ability to collect more than a single `Exception`

Where is this useful?

 * a group of things that are mutually dependent, but not sequential
 * a set of web form fields that need to be validated
     * Each field must be a `Right` for the entire form to be a `Right`
	 * But fields are validated independently
	 * We want independence in validating these fields, so that all errors can be caught at the same time
	 * What if you make multiple mistakes in a web form, and the page only informs you of one mistake per attempt to submit?

---

	!scala
	sealed trait Validation[+E, +A]

	case class Failure[E](head: E, tail: Vector[E] = Vector())
	  extends Validation[E, Nothing]

	case class Success[A](a: A) extends Validation[Nothing, A]

A `Failure` instance of `Validation` will have `Nothing` for its "right" type.

A `Failure` instance of `Validation` will contain at least one instance of `E`.  Its member `tail` may be empty.

In Scalaz, `Failure` contains a `NonEmptyList[E]` rather than `head: E` and `tail: Vector[E]`

A `Success` instance of `Validation` will have `Nothing` for its "left" type.

---
#Challenge Question

Write a function that combines two `Failure`s.  This is a component of other `Validation` combinators you will implement in lab.

	!scala
	def combineFailures[E](f1: Validation[E, Nothing],
	                   f2: Validation[E, Nothing):
					  Validation[E, Nothing] = ???


---


---
	!scala
	def combineFailures[E](f1: Validation[E, Nothing],
	                   f2: Validation[E, Nothing):
					  Validation[E, Nothing] = (f1, f2) match {
	  case (Failure(h1, t1), Failure(h2, t2)) =>
	    Failure(h1, h2::t1:::t2)
	  ...
	}

---

You can find an implementation of `Validation` in several places:

* In *FP in Scala*, `fpinscala.applicative.Validation`
* In *Cats*, `cats.data.Validated`
    * `cats/core/src/main/scala/cats/data/Validated.scala`
	* [ScalaDoc](http://typelevel.org/cats/api/#cats.data.Validated)
* Nowhere in the Scala standard library that I know of

---

If "chained container" has `map2`, why do we need `ringed container`?

Every "chained container" is also a "ringed container."

Earlier we mentioned that choosing the most restricted data type that meets the requirements is good programming practice.

The same applies to choosing "ringed container" over "chained container."

"Ringed container" is more restricted, and therefore more analyzable.

---

[What are the benefits of applicative parsing over monadic parsing?](http://stackoverflow.com/a/7863380/1007926)

"
The main difference between monadic and applicative parsing is in how sequential composition is handled.

You might think that having some extra flexibility can't hurt, but in reality it can. It prevents us from doing useful static analysis on a parser without running it. "

"Ringed container" is a more restricted, and therefore more analyzable, type of container than "chained container."

---

#Tuple

Tuple is a simple product type that groups together simple logical collections of items without using a class.

    scala> val hostPort = ("localhost", 80)
    hostPort: (String, Int) = (localhost, 80)
    scala> hostPort._1
    res0: String = localhost
    scala> hostPort._2
    res1: Int = 80

---

We are concluding our discussion of `Option`, `Either` and `Validation` with `Tuple` to expose the categorical relationship between the first 3 types and `Tuple`.

`Tuple` is a *Product* in category theory.

`Option`, `Either` and `Validation` are each a *Coproduct* in category theory.

`Tuple` is a simpler type to understand than `Option`, `Either` or `Validation`.

`Either` and `Validation` are each a dual to `Tuple2`.

---

Tuple is implemented as a case class, is not iterable, and is indexed starting from 1.

    !scala
    case class Tuple2[+T1, +T2](_1: T1, _2: T2)
      extends Product2[T1, T2]
      with Product
      with Serializable


---

Tuples fit with pattern matching nicely.

    !scala
    hostPort match {
      case ("localhost", port) => ...
      case (host, port) => ...
    }

---

Tuple has some special sauce for simply making Tuples of 2 values

    scala> 1 -> 2
    res0: (Int, Int) = (1,2)
    scala> 1.->(2)
    res0: (Int, Int) = (1,2)

---

#Tuples and Maps

Maps can be thought of as sets of tuples

    scala> val a = 'a' -> 97
    a: (Char, Int) = (a,97)
    scala> Map(a)
    res0: scala.collection.immutable.Map[Char,Int] = Map(a -> 97)
    scala> Map(a,a)
    res0: scala.collection.immutable.Map[Char,Int] = Map(a -> 97)

---

So the functions you write work on a pair of the keys and values in the Map.

    scala> val ext= Map("steve" -> 100, "bob" -> 101, "joe" -> 201)
    ext: Map[String,Int] = Map((steve,100), (bob,101), (joe,201))

---

    scala> ext.filter((namePhone: (String, Int)) => namePhone._2 < 200)
    res0: Map[String,Int] = Map((steve,100), (bob,101))

.notes: Because it gives you a tuple, you have to pull out the keys and values with their positional accessors. Yuck! Lucky us, we can actually use a pattern match to extract the key and value nicely.

---

    scala> ext.filter({case (name, extension) => extension < 200})
    res0: Map[String,Int] = Map((steve,100), (bob,101))

.notes: Why does this work? Why can you pass in a partial pattern match?
Because PartialFunction[A,B] is a subtype of A=>B

---

#Homework

Read Chapter 5 of _Functional Programming in Scala_.
