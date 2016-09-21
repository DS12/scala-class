
#Lecture 4a: Either, Xor, and Validated


![](images/lecture4a/cats.png)



<!--Products and Coproducts -->

<!---
http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html
http://danielwestheide.com/blog/2012/12/26/the-neophytes-guide-to-scala-part-6-error-handling-with-try.html
-->

---


#Either

`Either`, like `Option` is a monadic container that can hold one of two things.

`Option` has only one generic type: `Some`

`Either` has two generic types: left/incorrect and right/correct.

---

	!scala
	trait Either[+E,+A] //base trait
	case class Left[+E](e: E) extends Either[E, Nothing]
	case class Right[+A](a: A) extends Either[Nothing, A]

The left side of `Either` is *typically* an `Exception` of some form, and is usually intended to be the "end of the chain."  

---

A `Left` instance of `Either` will have `Nothing` for its "right" type.

A `Right` instance of `Either` will have `Nothing` for its "left" type.

The generic letter for the "left" type is "E" because it is usually a subtype of Java's `Exception`.

---

	!scala
	trait Either[+E,+A] {
	 def map[B](f: A => B): Either[E, B] =
		this match {
		 case Left(e) => Left(e)
		 case Right(a) => Right(f(a))
		}
	 def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B] =
		this match {
		 case Left(e) => Left(e)
		 case Right(a) => f(a)
		}
	}

---
# Lower Type Bounds

In chapter 4 of FPS, the authors note the necessity of the lower type bound generic `B` in the `getOrElse` and `orElse` combinators.

	!scala
	trait Option[+A] {
		def getOrElse[B >: A](default: => B): B
		def orElse[B >: A](ob: => Option[B]): Option[B]
	}

---

`B >: A` is a [*lower type bound*](http://docs.scala-lang.org/tutorials/tour/lower-type-bounds.html);

Generic `B` can be a supertype of `A`, or an equal type to `A`.

It is likely you will use [*upper type bounds*](http://docs.scala-lang.org/tutorials/tour/upper-type-bounds.html) more frequently.


---

An instance of `Some[A]` is an `Option[A]`.

The instance of `None` is an `Option[Nothing]`.

	!scala
	case class Some[+A](get: A) extends Option[A]
	case object None extends Option[Nothing]


---

[`Nothing`](http://www.scala-lang.org/api/current/index.html#scala.Nothing) is at the bottom of Scala's type hierarchy, i.e. `Nothing <: A` for all types `A`.

Since `Option` is covariant, an `Option[Nothing]` can be an `Option[A]` for all types `A`.

---

Given

	!scala
	val failed: Option[Nothing] = None

if not for the lower type bound generic `B >: A`, `getOrElse` would be limited to this signature when called on `failed`:

	!scala
	getOrElse(default: => Nothing): Nothing


---

With `B >: A`, `getOrElse` on `failed` becomes:

	!scala
	getOrElse[B >: Nothing](default: => B): B

---

The same explanation of the necessity of the lower type bound generic holds for `Either`.

If not for the lower type bound generic `EE`, `flatMap` would be limited to this signature when called on an instance of `Left`:

	!scala
	flatMap[B](f: A => Either[Nothing, B]): Either[Nothing, B]

---

Why would you need to call `flatMap` on an instance of `Left`?

Because at run-time your `Either` may not be a `Right` as you expected; it may be a `Left`.

But at compile-time, the combinators must type-check for either way this could go.

---

#Xor

Cats provides an alternative to the Scala standard library's  [Either](http://www.scala-lang.org/api/current/#scala.util.Either) in `cats.data.Xor`.

Why have this?

---

The main reason is that `Either` is unbiased.

This means we must first use projection (`.left` or `.right`) to access `flatMap`, `map`, etc.:

	!scala
	Right(123).flatMap(x => Right(x * 2))
	//error: value flatMap is not a member of scala.util.Right[Nothing,Int]
	Right(123).right.flatMap(x => Right(x * 2))
	//res0: scala.util.Either[Nothing,Int] = Right(246)

---

This makes `Either` inconvenient to use as a monad, especially as the convention in most functional languages is that the left side represents errors.

`Xor` complies with convention and thus supports `map` and `flatMap` directly:

	!scala
	import cats.data.Xor
	val a = Xor.Right(1)
	//a: cats.data.Xor.Right[Int] = Right(1)
	a.flatMap(x => Xor.Right(x + 2))
	//res1: cats.data.Xor[Nothing,Int] = Right(3)

---

The `Xor` object provides the `Xor.left` and `Xor.right` constructors as we saw above, as well as smart constructors via the type class syntax pattern:

	!scala
	import cats.syntax.xor._
	val a = 3.right[String]
	//a: cats.data.Xor[String,Int] = Right(3)
	val b = 4.right[String]
	//b: cats.data.Xor[String,Int] = Right(4)
	for {
		x <- a
		y <- b
	} yield x*x + y*y
	//res2: cats.data.Xor[String,Int] = Right(25)

---

`Xor` also supports familiar additional methods like `getOrElse`, and `orElse`.

We can use `getOrElse` to extract the right value or return a default:

	!scala
	1.right[String].getOrElse(0)
	//res4: Int = 1
	"Error".left[Int].getOrElse(0)
	//res5: Int = 0

---

Like `Either`, `Xor` is typically used to implement fail-fast error handling.

We sequence a number of computations using `flatMap`, and if one fails the remaining computations are not run:

	!scala
	for {
		a <- 1.right[String]
		b <- 0.right[String]
		c <- if(b == 0) "DIV0".left[Int] else (a / b).right[String]
	} yield c * 100
	//res6: cats.data.Xor[String,Int] = Left(DIV0)

---

When using `Xor` for error handling, we need to determine what type we want to use to represent errors. We could use `Throwable` for this as follows:

	!scala
	type Result[A] = Xor[Throwable, A]
	//infix notation
	type Result[A] = Throwable Xor A

---

This gives us similar semantics to `Try` from the Scala standard library.

The problem, however, is that `Throwable` is an extremely broad supertype, and we have little insight into what type of error occurred.

Another approach is to define an algebraic data type to represent the types of error that can occur.

---

#Example: Login Errors

	!scala
	case class User(username: String, password: String)
	sealed trait LoginError
	case class UserNotFound(username: String) extends LoginError
	case class PasswordIncorrect(username: String) extends LoginError
	trait UnexpectedError extends LoginError
	type LoginResult = LoginError Xor User

---

This approach solves the problems we saw with `Throwable`.

It gives us a fixed set of expected error types and a catch-all for anything else that we didn’t expect.

---

	!scala
	User("cem3394", "passw0rd").right
	//res0: Xor[Nothing,User] = Right(User(cem3394,passw0rd))
	UserNotFound("cem3394").left
	//res1: Xor[UserNotFound,Nothing] = Left(UserNotFound(cem3394))
	PasswordIncorrect("cem3394").left
	//res2: Xor[PasswordIncorrect,Nothing] = Left(PasswordIncorrect(cem3394))
	
---

#Cartesian

`Cartesian` is a type class that allows us to “tuple” values within a context.

If we have two objects of type `F[A]` and `F[B]`, a `Cartesian[F]` allows us to combine them to form an `F[(A, B)]`.

Recall that this was a motivating example when we introduced the type class pattern in lecture 2.

---

#Example: Combining Options

The code below summons a type class instance for Option and uses it to zip two values:

	!scala
	import cats.Cartesian
	import cats.instances.option._
	Cartesian[Option].product(Some(123), Some("abc"))
	//res0: Option[(Int, String)] = Some((123,abc))
	Cartesian[Option].product(None, Some("abc"))
	//res1: Option[(Nothing, String)] = None
	Cartesian[Option].product(Some(123), None)
	//res2: Option[(Int, Nothing)] = None

---

Its type class definition in Cats is:

	!scala
	trait Cartesian[F[_]] {
		def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
	}

---

Note that the parameters `fa` and `fb` are independent of one another.

This contrasts with `flatMap`, in which `fb` is evaluated using `a`:

	!scala
	trait FlatMap[F[_]] {
		def flatMap[A, B](fa: F[A])(fb: A => F[B]): F[B]
	}

---

We can define our own `product` for any monad as:

	!scala
	import cats.Monad
	import cats.syntax.flatMap._
	import cats.syntax.functor._
	import scala.language.higherKinds
	def product[F[_]: Monad, A, B](fa: F[A], fb: F[B]): F[(A,B)] =
		for {
			a <- fa
			b <- fb
		} yield (a, b)

---

Making this choice makes it easier to reason about uses of product for a specific monad instance—we only have to remember the semantics of flatMap to understand how product will work.

We've seen that we can implement product in terms of the monad operations. Why bother with the Cartesian type class then?

---

One reason to have the Cartesian type class is to enforce consistent behavior for all monad instances.

Another reason is that `product` (and in particular the 'tie-fighter' operator `|@|` we’ll see in lecture 12) is more convenient than writing out the for comprehension.

---

The most important reason however is that `Cartesian` is strictly weaker than `Monad`.

Next we'll look at a type (called an applicative) for which we can define `product` but not a monad instance.

---

#Validated

Recall that `Xor` is a monad, so its `product` is implemented in terms of `flatMap`.
<br />
<br />
Let's experiment with this for a bit.

---


	!scala
	import cats.data.Xor
	type ErrorOr[A] = List[String] Xor A
	val a: ErrorOr[Int] = Xor.right(123)
	val b: ErrorOr[String] = Xor.right("abc")
	product(a,b)
	//res0: ErrorOr[(Int, String)] = Right((123,abc))

---

However, if we try to combine two failed `Xors`, only the left-most errors are retained:

	!scala
	val c: ErrorOr[Nothing] = Xor.left(List("Fail 1"))
	val d: ErrorOr[Nothing] = Xor.left(List("Fail 2"))
	product(c,d)
	//res1: ErrorOr[(Nothing, Nothing)] = Left(List(Fail 1))

This is what is meant by 'failing fast'.

---

However fail-fast semantics aren’t always the best choice.
<br />
<br />
For example, when validating a web form, we want to accumulate errors for all invalid fields, not just the first one we find.
<br />
<br />
If we model this with a monad like `Xor`, we fail fast and lose errors.

---

#Example: Parsing

	!scala
	def parseInt(str: String): String Xor Int =
		Xor.catchOnly[NumberFormatException](str.toInt)
			 .leftMap(_ => s"Couldn't read $str")
	//parseInt: (str: String)cats.data.Xor[String,Int]

---

The situation is the same with for comprehensions. The code below fails on the first call to `parseInt` and doesn’t go any further:

	!scala
	for {
		a <- parseInt("a")
		b <- parseInt("b")
		c <- parseInt("c")
	} yield (a + b + c)
	//res0: Xor[String,Int] = Left(Couldn't read a)

---

Cats provides another data type called `Validated` in addition to `Xor`.
<br />
<br />
`Validated` is an example of a non-monadic applicative.
<br />
<br />
This means Cats can provide an error-accumulating implementation of product for `Validated` without introducing inconsistent semantics.


---

`Validated` has two subtypes, `Validated.Valid` and `Validated.Invalid`, that correspond to `Xor.Right` and `Xor.Left`.

We can create instances directly using their `apply` methods:

	!scala
	import cats.data.Validated
	val v = Validated.Valid(123)
	//v: cats.data.Validated.Valid[Int] = Valid(123)
	val i = Validated.Invalid("oops")
	//i: cats.data.Validated.Invalid[String] = Invalid(oops)

---

It is better for type inference to use smart constructors:

	!scala
	import Validated.{valid, invalid}
	val v = valid[String, Int](123)
	//v: Validated[String,Int] = Valid(123)
	val i = invalid[String, Int]("oops")
	//i: Validated[String,Int] = Invalid(oops)

---

And again we can import enriched `valid` and `invalid` methods from `cats.syntax.validated`:

	!scala
	import cats.syntax.validated._
	123.valid[String]
	//res0: Validated[String,Int] = Valid(123)
	"oops".invalid[Int]
	//res1: Validated[String,Int] = Invalid(oops)

---

Unlike the `Xor`’s monad, which cuts the calculation short, `Validated` keeps going to report back all failures.

	!scala
	import cats.data.Validated
	import cats.syntax.cartesian._
	type ErrorOr[A] = Validated[String,A]
	val a: ErrorOr[Nothing] = Validated.Invalid("foo")
	val b: ErrorOr[Nothing] = Validated.Invalid("bar!")
	Cartesian[ErrorOr].product(a,b)
	//res0: ErrorOr[(Nothing, Nothing)] = Invalid(foobar!)

---

Similar to `Writer`, `Validated` accumulates errors using a `Semigroup` (the append part of a `Monoid`).

This means we can use any `Monoid` as an error type, including `String`, `List`, and `Vector`, as well as pure semigroups like `NonEmptyList`s.

---

	!scala
	import cats.Cartesian
	type StringOr[A] = Validated[String, A]
	import cats.std.string._
	Cartesian[StringOr].product(
	  Validated.invalid("Hello"),
	  Validated.invalid(" world")
	)
	//res0 = Invalid(Hello world)

---

Lists have a monoid instance as well:

	!scala
	import cats.std.list._
	type ListOr[A] = Validated[List[String], A]
	Cartesian[ListOr].product(
	  Validated.invalid(List("Hello")),
	  Validated.invalid(List("world"))
	)
	//res1 = Invalid(List(Hello, world))

---

Note that vectors have a more efficient append operation than lists and should be used instead:

	!scala
	import cats.std.vector._
	type VectorOr[A] = Validated[Vector[Int], A]
	Cartesian[VectorOr].product(
	  Validated.invalid(Vector(404)),
	  Validated.invalid(Vector(500))
	)
	//res2 = Invalid(Vector(404, 500))

---
`Xor` vs `Validated`

Which is better?

The answer depends on the semantics we’re looking for.

---

Some points to ponder:

* Sometimes sequentiality (i.e.`flatMap`) is exactly what you need. Perhaps you want fail-fast semantics or the ability use the contents of one container as an argument to the next container.
* Error recovery is important when processing large jobs like Spark pipelines. We don’t want to run a job for an hour and then find it failed on the last element.
* Error reporting is equally important. We need to know what went wrong, not just that something went wrong.
* In a number of cases we want to collect all the errors, not just the first one we encountered.

---

#Homework

Read Chapter 5 of _Functional Programming in Scala_.
