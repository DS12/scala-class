#Lecture 12: Applicatives

---

`Functor` gives us a way to transform any values embedded in structure.
<br />
<br />
`Applicative` is a monoidal functor. It gives us a way to transform any values contained within a structure using a function that is also embedded in the same structure.
<br />
<br />
This means that each application produces the effect of adding structure which is then combined using the monoid laws.

---

#Apply

`Apply` extends the `Functor` type class (which features the familiar `map` function) with a new function `ap`.
<br />
<br />
The `ap` function is similar to map in that we are transforming a value in a context (a context being the `F` in `F[A]`; a context can be `Option`, `List` or `Future` for example).
<br />
<br />
However, the difference between `ap` and `map` is that for `ap` the function that takes care of the transformation is of type `F[A => B]`, whereas for `map` it is `A => B`

---

    !scala
    import cats._
    val double: Int => Int = _ * 2
    Apply[Option].ap(Some(double))(Some(1))
    //res8: Option[Int] = Some(2)
    Apply[Option].ap(Some(double))(None)
    //res9: Option[Int] = None
    Apply[Option].ap(None)(Some(1))
    //res10: Option[Nothing] = None


---

#Applicative

Cats models applicatives using two type classes.
<br />
<br />
The first, `Apply` extends `Cartesian` and `Functor`, adding an `ap` method that applies a parameter to a function within a context.
<br />
<br />
The second, `Applicative` extends `Apply`, adding the `pure` method.

---

    !scala
    trait Apply[F[_]] extends Cartesian[F] with Functor[F] {
      def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]
      def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
        ap(map(fa)(a => (b: B) => (a, b)))(fb)
    }
    trait Applicative[F[_]] extends Apply[F] {
      def pure[A](a: A): F[A]
    }


---

The `pure` method in `Applicative` is the same `pure` we saw in `Monad`.
<br />
<br />
It constructs a new applicative instance from an unwrapped value.
<br />
<br />
In this sense, `Applicative` is related to `Apply` as `Monoid` is related to `Semigroup`.

---

Note also that `product` is a derived combinator (i.e. it is defined in terms of `ap` and `map`).
<br />
<br />
There is an equivalence between `ap`, `map`, and `product` that allows any one of them to be defined in terms of the other two.

* map over F[A] to produce a value of type F[B => (A, B)];
* apply F[B] as a parameter to F[B=>(A,B)] to get a result of type F[(A,B)].

---


By defining one of these three methods in terms of the other two, we ensure that the derived definitions are consistent for all implementations of `Apply`.
<br />
<br />
This is somewhat similar to the relationship between `compose`, `join` and `flatMap` for monads (i.e. if you are given `pure`, `map` and any of the above you can implement the other two).

---

    !scala
    trait Applicative[F[_]] extends Apply[F] {
      def pure[A](a: A): F[A]
      override def map[A, B](fa: F[A])(f: A => B): F[B] =
        ap(pure(f))(fa)
      def map2[A,B,C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
        ap(map(fa)(f.curried))(fb)
      override def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
        map2(fa, fb)((_,_))
    }

---

`map2` is implemented by first currying `f` so we get a function of type `A => B => C`.
<br />
<br />
This is a function that takes `A` and returns another function of type `B => C`.
<br />
<br />
So if we map `f.curried` over an `F[A]`, we get `F[B => C]`.
<br />
<br />
Passing that to `apply` along with the `F[B]` will give us the desired `F[C]`.

---

Given `map` and `product` we could create a `map2` and use it to implement our `ap`:

    !scala
    trait Applicative[F[_]] extends Apply[F] {
      def pure[A](a: A): F[A]
      def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
      def map2[A,B,C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
        map(product(fa, fb)) { case (a, b) => f(a, b) }
      override def ap[A,B](fab: F[A => B])(fa: F[A]): F[B] =
        map2(fab, fa)(_(_))
    }

---

We simply use `map2` to lift a function into `F` so we can apply it to both `fab` and `fa`.
<br />
<br />
The function being lifted here is `_(_)`, which is the same as the lambda notation `(f, x) => f(x)`.
<br />
<br />
That is, it's a function that takes a function `f` and an argument `x`, and simply applies `f` to `x`.

---
#Type Class Hierarchy

<img src="images/lecture12/type-classes.png" height="420">

---

Each type class represents a particular set of sequencing semantics.
<br />
<br />

    !scala
    def     map(f: A => B):     F[A] => F[B]
    def     ap(f:F[A => B]):    F[A] => F[B]
    def flatMap(f: A => F[B]):  F[A] => F[B]

---

Each type class introduces its characteristic methods, and defines all of the functionality from its supertypes in terms of them (e.g. every monad is an applicative, every applicative a cartesian, etc).
<br />
<br />
Therefore inheritance relationships are constant across all instances of a particular type class.

---

For example, Monad defines` product`, `ap`, and `map`, in terms of `pure` and `flatMap`:

    !scala
    trait Monad[F[_]] extends FlatMap[F] with Applicative[F] {
      def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
        flatMap(fa)(a => map(fb)(b => (a, b)))
      def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] =
        flatMap(ff)(f => map(fa)(f))
      def map[A, B](fa: F[A])(f: A => B): F[B] =
        flatMap(a => pure(f(a)))
    }

---

#Cartsian

    !scala
    import cats.Cartesian
    import cats.std.option._
    Cartesian[Option].product(Some(123), Some("abc"))
    //res0: Option[(Int, String)] = Some((123,abc))

---

If either argument evaluates to `None`, the entire result is `None`:

    !scala
    Cartesian[Option].product(None, Some("abc"))
    //res1: Option[(Nothing, String)] = None
    Cartesian[Option].product(Some(123), None)
    //res2: Option[(Int, Nothing)] = None

---

The `|@|` operator, better known as the 'tie fighter', provides infix syntax for this:

    !scala
    (List(1,2,3) |@| List(4,5,6)).tupled
    //List((1,4),(1,5),(1,6),(2,4),(2,5),(2,6),(3,4),(3,5),(3,6))
    (Xor.right(123) |@| Xor.right("abc")).tupled
    //res3: Xor[Nothing,(Int, String)] = Right((123,abc))

---

`|@|` creates an intermediate `builder` object that provides several methods for combining the parameters to create useful data types.

The idiomatic way of using builder syntax is to combine `|@|` and `tupled` in a single expression, going from single values to a tuple in one step:

    !scala
    (
      Option(1) |@|
      Option(2) |@|
      Option(3)
    ).tupled
    //res4: Option[(Int, Int, Int)] = Some((1,2,3))

---

`|@|` is associative:

    !scala
    val three = Option(123) |@| Option("abc") |@| Option(true)
    three.tupled
    //Some((123,abc,true))
    val five = three |@| Option(0.5) |@| Option('x')
    five.tupled
    //Some((123,abc,true,0.5,x))

---


Every builder also has a `map` method that accepts a function of the correct arity and implicit instances of `Cartesian` and `Functor`:

    !scala
    (
      Option(1) |@|
      Option(2)
    ).map(_ + _)
    //res5: Option[Int] = Some(3)

---

Or apply parameters to create a case class:

    !scala
    case class Address(name: String, number: Int, street: String)
    (
      Option("DataScience") |@|
      Option(200)       |@|
      Option("Corporate Pointe")
    ).map(Cat.apply)
    //res6 = Some(Address(DataScience,200,Corporate Pointe))

---

#Applicative laws

The book presents the applicative laws in terms of `map2`:

   * Left identity: `map2(unit(()), fa)((_,a) => a) == fa`
   * Right identity: `map2(fa, unit(()))((a,_) => a) == fa`
   * Associativity: `product(product(fa, fb),fc) == map(product(fa, product(fb, fc)))(assoc)`
   * Naturality: `map2(a,b)(productF(f,g)) == product(map(a)(f), map(b)(g))`

---

The applicative laws are more commonly stated in terms of `ap`.
<br />
<br />
The laws for `ap` are _identity_, _composition_, _homomorphism_, and _interchange_.
<br />
<br />
We'll go through them one at a time.

---

#Identity

The identity law for `apply` is stated as:

    !scala
    ap(pure(id))(v) == v

The identity law says that embedding the identity function in the monoid and applying it to a value results in no change.

.notes: pure id <*> v = v

---

#Composition

The composition law for `ap` is stated as:

    !scala
    ap(u)(ap(v)(w)) ==
    ap(ap(ap(pure(f => g => f compose g))(u))(v))(w)

.notes: pure(.) <*> u <*> v <*> w = u <*> (v <*> w)

---

The composition law says applying `v` to `w` and then applying `u` to that is the same as applying composition to `u`, then `v`, and then applying the composite function to `w`.
<br />
<br />
We might state this law simply as: "function composition in an applicative functor works in the obvious way."
<br />
<br />
This is analagous to the composition law for `Functor`.

---

#Homomorphism

The homomorphism law for `ap` is stated as:

    !scala
    ap(pure(f))(pure(x)) == pure(f(x))

.notes: pure f <*> pure x = pure (f x)

---

The homomorphism law says that idiomatic function application on `pure`s is the same as the `pure` of regular function application.
<br />
<br />
More precisely, `pure` is a homomorphism from `A` to `F[A]` with regard to function application.

---

#Interchange

The interchange law for `ap` is stated as:

    !scala
    ap(u)(pure(y)) == ap(pure(_(y)))(u)

.notes: u <*> pure y = pure ($ y) <*> u

---

The interchange law is essentially saying that `pure` is not allowed to carry an effect with regard to any implementation of our applicative functor.
<br />
<br />
If one argument to `ap` is a `pure`, then the other can appear in either position.

---

The applicative laws taken together can be seen as saying that we can rewrite any expression involving `pure` or `ap` (and therefore by extension `map2`), into a normal form having one of the following shapes:

    !scala
    pure(x)          // for some x
    map(x)(f)        // for some x and f
    map2(x, y)(f)    // for some x, y, and f
    map3(x, y, z)(f) // for some x, y, z, and f
    //...etc

That is, every expression in an applicative functor `A` can be seen as lifting some pure function `f` over a number of arguments in `A`.

---

The applicative laws amount to saying that the arguments to `map`, `map2`, `map3`, etc can be reasoned about independently, and an expression like `flatMap(x)(f)` explicitly introduces a dependency (so that the result of `f` depends on `x`).
<br />
<br />
Note that this reasoning is lost when the applicative happens to be a monad and the expressions involve `flatMap`.

---

#Monads vs Applicatives

There is a tradeoff between applicative APIs and monadic ones.
<br />
<br />
Monadic APIs are strictly more powerful and flexible, but the cost is a certain loss of algebraic reasoning.
<br />
<br />
The difference is easy to demonstrate in theory, but takes some experience to fully appreciate in practice.

---

Consider composition in a monad, combining values with `compose` (Kleisli composition):

    !scala
    val fooM: A => F[B] = ???
    val barM: B => F[C] = ???
    val bazM: A => F[C] = barM compose fooM

---

There is no way that the implementation of the `compose` function in the `Monad[F]` instance can inspect the values `foo` and `bar`.
<br />
<br />
They are functions, so the only way to 'see inside' them is to give them arguments.
<br />
<br />
The values of type `F[B]` and `F[C]` respectively are not determined until the composite function _runs_.

---

Now consider composition in an applicative, combining values with `map2`:

    !scala
    val fooA: F[A] = ???
    val barA: F[B] = ???
    val bazA: F[C] = map2(fooA, barA)(f)

---

Here the implementation of `map2` can actually look at the values `fooA` and `barA`, and take different actions depending on what they are.
<br />
<br />
If `F` is something like `Future`, it might decide to start immediately evaluating them on different threads.
<br />
<br />
If the data type `F` is applicative but _not a monad_, then the implementation has this flexibility universally because an expression in `F` will never involve functions of the form `A => F[B]` that it can't see inside of.

---

Because `Applicative` is 'weaker' than `Monad`, this gives the interpreter of applicative effects more flexibility.
<br />
<br />
`Applicative` is therefore generally preferred to `Monad` when the structure of a computation is fixed a priori.
<br />
<br />
That makes it possible to perform certain kinds of static analysis on applicative values.

---

For example, if we describe a parser without resorting to `flatMap`, this implies that the structure of our grammar is determined before we begin parsing.
<br />
<br />
Therefore, our interpreter or runner of parsers has more information about what it’ll be doing up front and is free to make additional assumptions and use a more efficient implementation strategy.
<br />
<br />
Adding `flatMap` is powerful, but it means we’re generating our parsers dynamically, so the interpreter may be more limited in what it can do.

---

The lesson here is that power and flexibility in the interface often restricts power and flexibility in the implementation.
<br />
<br />
And a more restricted interface often gives the implementation more options.
<br />
<br />
See [this StackOverflow question](http://stackoverflow.com/questions/7861903/what-are-the-benefits-of-applicative-parsing-over-monadic-parsing) for further discussion of the issue with regard to parsers.

---

A more algebraic manifestation of the difference is that, like `Functor` and `Apply`, applicative functors also compose naturally with each other.
<br />
<br />
When you compose one `Applicative` with another, the resulting `pure` operation will lift the passed value into one context, and the result into the other context.
<br />
<br />
We've seen however that monads do not in general compose with each other without some 'hand wiring'.

---

    !scala
    val listOpt = Apply[List] compose Apply[Option]
    val inc = (x:Int) => x + 1
    listOpt.ap(List(Some(double),Some(inc)))(List(Some(2), None, Some(3)))
    //res0 = ???

---


#Example: Futures

A concrete example of the difference between monads and applicatives is the concurrent evaluation of `Future`s.
<br />
<br />
If we have several long-running independent tasks, it makes sense to execute them concurrently.
<br />
<br />
However, monadic comprehension only allows us to run them in sequence.

---


    !scala
    import scala.concurrent._
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    lazy val time0 = System.currentTimeMillis
    def getTime: Long = {
      val time1 = System.currentTimeMillis - time0
      Thread.sleep(1000)
      time1
      }

---

Here three futures are started independently of one another and can execute in parallel:

    !scala
    val applicativeTimes = (
      Future(getTime) |@|
      Future(getTime) |@|
      Future(getTime)
      ).tupled
    Await.result(applicativeTimes, Duration.Inf)
    //res0: (Long, Long, Long) = (1942,1944,1946)


---

This is in contrast to the following monadic combination, which executes them in sequence:

    !scala
    val monadTimes = for {
      a <- Future(getTime)
      b <- Future(getTime)
      c <- Future(getTime)
    } yield (a, b, c)
    Await.result(monadTimes, Duration.Inf)
    //res1: (Long, Long, Long) = (0,1003,2009)


---


#Example: Validated

If we try to combine two failed `Xors`, only the left-most errors are retained:

    !scala
    (Xor.left(List("Fail 1")) |@| List("Fail 2")).tupled
    // res2: ErrorOr[(Nothing, Nothing)] = Left(List(Fail 1))

---

If you think back to our examples regarding `Future`s, you’ll see why this is the case. `Xor` is a monad, so Cats implements product in terms of `flatMap`.
<br />
<br />
As we have seen, `flatMap` implements fail-fast error handling.

---

However fail-fast semantics aren’t always the best choice.
<br />
<br />
When validating a web form, for example, we want to accumulate errors for all invalid fields, not just the first one we find.
<br />
<br />
If we model this with a monad like Xor, we fail fast and lose errors
---

For example, the code below fails on the first call to parseInt and doesn’t go any further:

    !scala
    import cats.data.Xor
    def parseInt(str: String): String Xor Int =       
      Xor.catchOnly[NumberFormatException](str.toInt)
         .leftMap(_ => s"Couldn't read $str")
    for {
      a <- parseInt("a")
      b <- parseInt("b")
      c <- parseInt("c")
    } yield (a + b + c)
    // res0: Xor[String,Int] = Left(Couldn't read a)

---


Cats provides another data type called `Validated` in addition to `Xor`.
<br />
<br />
`Validated` is an example of a non-monadic applicative.
<br />
<br />
This means Cats can provide an error-accumulating implementation of product for `Validated` without introducing inconsistent semantics.


---

`Validated` has two subtypes, `Validated.Valid` and `Validated.Invalid`, that correspond loosely to `Xor.Right` and `Xor.Left`.

We can create instances directly using their apply methods:

    !scala
    import cats.data.Validated
    val v = Validated.Valid(123)
    // v: cats.data.Validated.Valid[Int] = Valid(123)
    val i = Validated.Invalid("oops")
    // i: cats.data.Validated.Invalid[String] = Invalid(oops)

---

However, it is better for type inference to use the `valid` and `invalid` smart constructors, which return a type of `Validated`:

    !scala
    import Validated.{valid, invalid}
    val v = valid[String, Int](123)
    // v: Validated[String,Int] = Valid(123)
    val i = invalid[String, Int]("oops")
    // i: Validated[String,Int] = Invalid(oops)

---

We can import enriched `valid` and `invalid` methods from `cats.syntax.validated` to get some syntactic sugar:

    !scala
    import cats.syntax.validated._
    123.valid[String]
    //res10: Validated[String,Int] = Valid(123)
    "message".invalid[Int]
    //res11: Validated[String,Int] = Invalid(message)

---

    !scala
    (
    "event 1 ok".valid[String] |@|
    "event 2 failed!".invalid[String] |@|
    "event 3 failed!".invalid[String]
    ) map {_ + _ + _}
    //res12: Validated[String,String] = Invalid(event 2 failed!event 3 failed!)

---

Unlike the `Xor`’s monad, which cuts the calculation short, `Validated` keeps going to report back all failures.

The problem, however, is that the error messages are mushed together into one string. Shouldn’t it be something like a list?

---

    !scala
    import cats.std.list._
    import cats.syntax.cartesian._
    (
    List("a").invalid |@|
    List("b").invalid
    ).tupled
    //res13: Validated[List[String],(Nothing, Nothing)] = Invalid(List(a, b))

---

Validated accumulates errors using a `Semigroup` (the append part of a `Monoid`).

This means we can use any `Monoid` as an error type, including `Lists`, `Vectors`, and `Strings`, as well as semigroups like `NonEmptyList`s.

---

#Using `NonEmptyList`

Validation is one place where a `NonEmptyList` comes in handy. Think of it as a list that’s guaranteed to have at least one element.

    !scala
    import cats.data.{ NonEmptyList => NEL }
    NEL(1)
    //OneAnd[[+A]List[A],Int] = OneAnd(1,List())

    NEL(1) |+| NEL(1)
    res151: OneAnd[List,Int] = OneAnd(1,List(1))

---

A semigroup should be formed for `NEL[A]` under the `++` operation, but it’s not there by default atm, so we need to derive it from `SemigroupK`.

Then we can use `NEL[A]` on the invalid side to accumulate the errors:

    !scala
    import cats._, cats.data.Validated, cats.std.all._
    val result = (
      valid[NEL[String], String]("1 ok") |@|
      invalid[NEL[String], String](NEL("2 failed!")) |@|
      invalid[NEL[String], String](NEL("3 failed!"))
    ) map {_ + _ + _}
    //result = Invalid(OneAnd(2 failed!,List(3 failed!)))

---


We can convert back and forth between `Validated` and `Xor` using the `toXor` and `toValidated` methods:

import cats.data.Xor
"Badness".invalid[Int].toXor
// res22: cats.data.Xor[String,Int] = Left(Badness)
"Badness".invalid[Int].toXor.toValidated
// res23: cats.data.Validated[String,Int] = Invalid(Badness)

---

This allows us to switch error-handling semantics on the fly:

// Accumulate errors in an Xor:
(
Xor.left[List[String], Int](List("Fail 1")).toValidated |@| Xor.left[List[String], Int](List("Fail 2")).toValidated
).tupled.toXor
// res25: cats.data.Xor[List[String],(Int, Int)] = Left(List(Fail 1, Fail 2))
// Sequence operations on Validated using flatMap:
for {
a <- Validated.invalid[List[String], Int](List("Fail 1")).toXor b <- Validated.invalid[List[String], Int](List("Fail 2")).toXor
} yield (a, b)
// res27: cats.data.Xor[List[String],(Int, Int)] = Left(List(Fail 1))

---

#Homework

Finish reading Chapter 12 of _Functional Programming in Scala_ (12.6-12.8), and have a look at `Foldable` and `Traverse` in [*Cats*](https://github.com/typelevel/cats).

---

#Links

* [Advanced Scala with Cats](http://underscore.io/books/advanced-scala/)

* [The essence of form abstraction](http://groups.inf.ed.ac.uk/links/formlets/)

* [Applicative Programming with Effects](http://www.soi.city.ac.uk/~ross/papers/Applicative.html)
