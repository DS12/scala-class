

#Monad Transformers

---


Next week we will see that this is also possible to do with arbitrary applicatives.
<br />
<br />
That is, given arbitrary applicatives `A1[_]` and `A2[_]` and no other knowledge we can contsruct an applicative on `A1[A2[_]]`.

---

Can we do the same with monads? That is, given two monads, can we make one monad out of them in a generic way?

	!scala
	def compose[M1[_] : Monad, M2[_] : Monad] = {
		new Monad[M1[M2[_]]] {
			def pure[A](a: A): M1[M2[A]] =
				a.pure[M2].pure[M1]
			def flatMap[A, B](fa: M1[M2[A]])
			(f: A => M1[M2[B]]): M1[M2[B]] = ???
		}
	}


---

Applying `f` to the internals of `fa`, we'd end up with something of type `M1[M2[M1[M2[B]]]]`.
<br />
<br />
We would then like to swap layers to get something of type `M1[M1[M2[M2[B]]]]`, then use the `join`s from `M1` and `M2` to get a result of type `M1[M2[B]]`.                                              <br />
<br />
However there's nothing in the monad API that will allow us to do that for arbitrary monads `M1` and `M2`.
<br />
<br />
So we can’t compose monads in general.

---              

This is not greatly surprising because we use monads to model effects and effects don’t in general compose.
<br />
<br />
However, many monads can be made to compose with monad-specific glue code. For these cases we can use monad transformers to compose them.
<br />
<br />
Monad transformers allow us to squash together monads, creating one monad where we previously had two or more. With this transformed monad we can avoid nested calls to flatMap.

---

The basic transformer pattern enables us to cope with the following type transitions, where `M2` is the polymorphic outer structure, and `M1` is the concrete type that the transformer was built for.

	!scala
	M2[M1[M2[B]]] =>
	M2[M2[B]] =>
	M2[B] =>
	M1[M2[B]]

---

Lets have a look at how this works for the identity monad's transformer:

	!scala
	case class IdT[F[_], A](value: F[A]) {
	  def pure[F[_], A](a: A)
			(implicit F: Applicative[F]): IdT[F, A] =
				IdT(F.pure(a))
	  def flatMap[B](f: A => IdT[F, B])
			(implicit F: FlatMap[F]): IdT[F, B] =
	    	IdT(F.flatMap(value)(f.andThen(_.value)))
	}		

---

Many data types have a monad transformer equivalent that allows us to compose the Monad instance of the data type with any other Monad instance.
<br />
<br />
For instance, `OptionT[F[_], A]` allows us to compose the monadic properties of `Option` with any other `F[_]`, such as a `List`.
<br />
<br />
This allows us to work with nested contexts/effects in a nice way (for example, in for-comprehensions).

---

We can create instances with pure as usual:

	!scala
	import cats.data.OptionT
	type ListOption[A] = OptionT[List, A]
	// defined type alias ListOption

Cats provides a library of such transformers: `XorT` for composing `Xor` with other monads, `OptionT` for composing `Option`, and so on.

---


`ListOption` is a monad that combines the properties of `List` and `Option`.

	!scala
	import cats.Monad
	import cats.std.list._
	import cats.syntax.applicative._
	val a: ListOption[Int] = 42.pure[ListOption]
	//a: ListOption[Int] = OptionT(List(Some(42)))


Note how we build it from the inside out: we pass `List`, the type of the outer monad, as a parameter to `OptionT`, the transformer for the inner monad.

<!-- 
https://gist.github.com/tpolecat/1227e22e3161b5816e014c00650f3b57
no ListT
-->
---

#Quick Aside

Note the imports in the code samples above—they hint at how everything bolts together.
<br />
<br />
We import `cats.syntax.applicative` to get the pure syntax. pure requires an implicit parameter of type `Applicative[ListOption]`.
<br />
<br />
We haven’t met applicatives yet, but all monads are also applicatives so we can ignore that difference for now.

---

We need an `Applicative[ListOption]` to call `pure`. We already have `cats.data.OptionT` in scope, which provides the implicits for `OptionT`.
<br />
<br />
However, in order to generate our `Applicative[ListOption]`, the implicits for `OptionT` also require an `Applicative` for `List`.
<br />
<br />
Hence the additional import from `cats.std.list`.

---

Notice we’re not importing `cats.syntax.functor` or `cats.syntax.flatMap`.
<br />
<br />
This is because `OptionT` is a concrete data type with its own explicit `map` and `flatMap` methods.
<br />
<br />
However it wouldn’t hurt to import the syntax—the compiler will simply ignore it in favour of the explicit methods.


---

#Monad Transformers in Cats

So monad transformers don’t have their own type class. This makes them a bit different from the other abstractions we’ve seen.
<br />
<br />
We use monad transformers to _build_ monads, which we then use via the `Monad` type class.

---

The transformed `map` and `flatMap` methods allow us to use both component monads without having to recursively unpack and repack values at each stage in the computation.

	!scala
	val a = 10.pure[ListOption]
	// a: ListOption[Int] = OptionT(List(Some(10)))
	val b = 32.pure[ListOption]
	// b: ListOption[Int] = OptionT(List(Some(32)))
	a flatMap { (x: Int) =>
	  b map { (y: Int) =>
		x+y
	  }
	}

---

The main points of interest when using monad transformers are:

* the available transformer classes
* building stacks of monads using transformers
* constructing instances of a monad stack
* pulling apart a stack to access the wrapped monads

---


By convention, in Cats a monad `Foo` will have a transformer class called `FooT`.
<br />
<br />
Some of the available instances are:

* `cats.data.OptionT` for `Option`;
* `cats.data.XorT` for `Xor`;
* `cats.data.ReaderT`, `cats.data.WriterT`, and `cats.data.StateT`;
* `cats.data.IdT` for the `Id` monad.

In fact, many monads in Cats are defined by combining a monad transformer with the Id monad.

---

The first type parameter to a monad transformer is the outer monad in the stack—the transformer itself provides the inner monad.
<br />
<br />
For example, our `ListOption` type above was built using `OptionT[List, A]` but the result was effectively a `List[Option[A]]`.

---

Many monads and all transformers have at least two type parameters, so we have to define type aliases for intermediate stages.
<br />
<br />
For example, suppose we want to wrap `Xor` around `Option`. `Option` is the innermost type so we want to use the `OptionT` monad transformer.

---

We need to use `Xor` as the first type parameter. However, `Xor` itself has two type parameters and monads only have one.

Therefore we need a type alias to make everything the correct shape:

	!scala
	type ErrorOr[A] = String Xor A
	type ErrorOptionOr[A] = OptionT[ErrorOr[A], A]
	//error: ErrorOr[A] takes no type parameters, expected: one
	type ErrorOptionOr[A] = OptionT[ErrorOr, A]
	val a = 41.pure[ErrorOptionOr]
	val b = a.flatMap(x => (x + 1).pure[ErrorOptionOr])
	//b = ???

---

Now let’s add another monad into our stack. We create a `Future` of an `Xor` of `Option`. Again we build this from the outside in:

	!scala
	import scala.concurrent.Future
	import cats.data.{XorT, OptionT}
	type FutureXor[A] = XorT[Future, String, A]
	type FutureXorOption[A] = OptionT[FutureXor, A]

---

Our `map` and `flatMap` methods on `FutureXorOption` now cut through three layers of abstraction:

	!scala
	import scala.concurrent.ExecutionContext.Implicits.global
	import cats.std.future._
	val answer: FutureXorOption[Int] = for {
		  a <- 10.pure[FutureXorOption]
      b <- 32.pure[FutureXorOption]
		} yield a + b
	//???

---

---

	!scala
	answer
	//res0 = OptionT(XorT(Success(Right(Some(42)))))

---

Once we’ve used a monad transformer, we can unpack it using its value method.

Each call to value unpacks a single monad transformer, so we may need more than one call to completely unpack a large stack:

	!scala
	import cats.data.{Writer, XorT, OptionT}
	type Logged[A] = Writer[List[String], A]
	type LoggedFallable[A] = XorT[Logged, String, A]
	type LoggedFallableOpt[A] = OptionT[LoggedFallable, A]

---

	!scala
	val packed = 123.pure[LoggedFallableOpt]
	val foo = packed.value
	//???
	val bar = foo.value
	//???
	val baz = bar.value
	//???


---

#Default Instances

Many monads in Cats are defined using the corresponding transformer and the `Id` monad. This is reassuring as it confirms that the APIs for these monads and transformers are identical.

`Reader`, `Writer`, and `State` are all defined in the following way:

	!scala
	type Reader[E, A] = ReaderT[Id, E, A]
	type Writer[W, A] = WriterT[Id, W, A]
	type State[S, A] = StateT[Id, S, A]

---

In other cases monad transformers have separate definitions to their corresponding monads. In these cases, the methods of the transformer tend to mirror the methods on the monad.
<br />
<br />
For example, `OptionT` defines `getOrElse`, and `XorT` defines `fold`, `bimap`, `swap`, and other useful methods.


---

#Kleisli Arrows

One of the most useful properties of functions is that they compose. That is, given a function `A => B` and a function `B => C`, we can combine them to create a new function `A => C`.
<br />
<br />
It is through this compositional property that we are able to write many small functions and compose them together to create a larger one that suits our needs.

---

Sometimes however, our functions will need to return monadic values. For instance, consider the following set of functions.

	!scala
	val parse: String => Option[Int] =
	  s => 	if (s.matches("-?[0-9]+"))
						Some(s.toInt)
					else None			
	val reciprocal: Int => Option[Double] =
	  i => 	if (i != 0) Some(1.0 / i)
					else None

---

As it stands we cannot use `Function1.compose` to compose these two functions. The output type of parse is `Option[Int]` whereas the input type of reciprocal is `Int`.
<br />
<br />
`Kleisli` enables composition of functions that return a monadic value, for instance an `Option[Int]` or a `Xor[String, List[Double]]`, without having functions take an `Option` or `Xor` as a parameter.

---

Depending on the properties of the `F[_]`, we can do different things with `Kleisli`s.
<br />
<br />
For instance, if `F[_]` has a `FlatMap[F]` instance (we can call `flatMap` on `F[A]` values), we can compose two `Kleisli`s much like we can two functions.

---

	!scala
	import cats.FlatMap
	import cats.syntax.flatMap._
	case class Kleisli[F[_], A, B](run: A => F[B]) {
	  def compose[Z](k: Kleisli[F, Z, A])
			(implicit F: FlatMap[F]): Kleisli[F, Z, B] =
	    	Kleisli[F, Z, B](z => k.run(z).flatMap(run))
	}


What is the type of `k.run(z)`? Why is `compose` parametrized by `Z`?

---


Returning to our earlier example:

	!scala
	import cats.std.option._
	val parse = Kleisli(
	  (s: String) => try {
	    Some(s.toInt) } catch {
	      case _: NumberFormatException => None
	      })
	val reciprocal = Kleisli(
	  (i: Int) => if (i == 0) None
	              else Some(1.0 / i)
	              )
	val foo = reciprocal.compose(parse)
	foo.run("5")
	//res0 = ???

---

Like `Reader[A,B]`, `Kleisli[F[_], A, B]` is essentially a wrapper around a function. The only difference is that the function has type `A => F[B]` instead of type A` => B`.
<br />
<br />
Thus `Kleisli` can be viewed as the monad transformer for functions.


---

Cats defines a `ReaderT` type alias along the lines of:

	!scala
	type Id[A] = A
	type ReaderT[F[_], A, B] = Kleisli[F, A, B]
	type Reader[A, B] = Kleisli[Id, A, B]
	object Reader {
	  def apply[A, B](f: A => B): Reader[A, B] =
			Kleisli[Id, A, B](f)
	}

The `ReaderT` type alias exists to allow users to use the `Kleisli` companion object as if it were `ReaderT`.

---

Why not just rename `Kleisli` to `Reader`?
<br />
<br />
[Historical reasons](https://github.com/typelevel/cats/issues/382) for one, but its also worth noting that `F[_]` having a `FlatMap` (or a `Monad`) instance is not a hard requirement for Cats' `Kleisli`.
<br />
<br />
We can also do useful things with weaker requirements on Kleisli arrows, for example [Finagle](https://twitter.github.io/finagle/) represents RPC services as `A => Future[B]`, and it's useful to be able to work with these things as Kleisli arrows.

The `A` in this case isn't an environment—it's just the input to a function.

---

One simple example is mapping over ranges, which only requires that `F[_]` have a `Functor` instance (e.g. is equipped with map: `F[A] => (A => B) => F[B]`).

	!scala
	import cats.Functor
	final case class Kleisli[F[_], A, B](run: A => F[B]) {
	  def map[C](f: B => C)
			(implicit F: Functor[F]): Kleisli[F, A, C] =
	    	Kleisli[F, A, C](a => F.map(run(a))(f))
	}

---

This is an area of active research!
<br />
<br />
Daniel Spiewak's[emm](https://github.com/djspiewak/emm)(following [eff](http://okmij.org/ftp/Haskell/extensible/)) offers an interesting alternative to monad transformers in Scala.

From the `README`:

> The Emm monad provides a syntactically lightweight, type-inference friendly data type for composing effects. The general motivation is very similar to monad transformers, but the end result is far more user friendly and also significantly more general.
