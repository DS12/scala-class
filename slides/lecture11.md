#Lecture 11: Monads

---


Monads are one of the most common abstractions in Scala, and one that most Scala programmers are familiar with even if they don’t know the name.
<br />
<br />
We even have special syntax in Scala to support monads: for comprehensions.
<br />
<br />
Despite the ubiquity of the concept, Scala lacks a concrete type to encompass things that can be flatMapped. This is one of the benefits of Cats.

---

Formally, a monad for a type `F[A]` has:

* an operation `flatMap` with type `(F[A], A => F[B]) => F[B]`
* an operation `pure` with type `A => F[A]`.
<br />
<br />
Most of the constructs we consider in this class are monadic: `List`, `Vector`, `Option`, `Xor`, `State`, `Future`, `Parser`, `Reader`, `Writer`, `IO` etc.

---

`pure` (often referred to as `unit` or `return`) abstracts over the constructor of our monad.
<br />
<br />
`flatMap` (often referred to as `bind` and given the infix notation `>>=`) chains structured computations together in a way that allows interference between the structure and computation layers.


---

`pure` and `flatMap` must obey three laws:

1. Left identity: `pure(a) flatMap f == f(a)`
2. Right identity: `m flatMap pure == m`
3. Associativity: `(m flatMap f) flatMap g == m flatMap (x=>f(x) flatMap g)`

---

#Exercise

A monad is also a functor. Given implementations of `flatMap` and `pure`, write an implementation of `map`:

    !scala
    import scala.language.higherKinds
    def flatMap[F[_], A, B](value: F[A])
      (func: A => F[B]): F[B]
    def pure[F[_], A](value: A): F[A]
    def map[F[_], A, B](value: F[A])
      (func: A => B): F[B] = ???

---


---

    !scala
    def map[F[_], A, B](value: F[A])
      (f: A => B): F[B] =
      flatMap(value)(a => pure(f(a)))

This factorization demonstrates the inherent lack of interference between the structure and computation layers in `map`.

---

#Exercise

There are several ways to define a monad in terms of combinations of primitive combinators. The combination we've seen most often is `unit`, `map`, and `flatMap`.
<br />
<br />
A second combination is `unit`, `map`, and `join`, where `join` has the following type signature:

    !scala
    def join[A](mma: F[F[A]]): F[A] = ???

Implement `join` in terms of `unit`, `map`, and/or `flatMap`.

---

---

    !scala
    def join[A](mma: F[F[A]]): F[A] =
      flatMap(mma)(ma => ma)

---

As an aside, the [definition of a monad](https://en.wikipedia.org/wiki/Monad_(category_theory)) used in category theory is stated in terms of `pure` and `join`.

---

Interestingly, a monad is also a kind of monoid.
<br />
<br />
Think a type like `(M, M) => M` as `M² => M` (taking 2 `M`s or the product of `M` with itself), and `M` as `1 => M` (where `1` is the `Unit` type).
<br />
<br />
Then we can think of a type like `F[F[A]] => F[A]` as `F²[A] => F[A]` or just `F² ~> F` (where `~>` denotes a [natural transformation](http://en.wikipedia.org/wiki/Natural_transformation)) and `A => F[A]` as `1[A] => F[A]` (where `1` is the identity functor) or just `1 ~> F`:

---

|             | `zero`/`unit`   | `op`/`join`|
--------------|-----------------|------------|
| `Monoid[M]` | `1 => M`        | `M² => M`  |
| `Monad[F]`  | `1 ~> F`        | `F² ~> F`  |
<br />
<br />
These are the same kind of thing, except `Monoid[M]` is operating in a category where the objects are Scala types and the arrows are Scala functions, and `Monad[F]` is operating in a category where the objects are Scala functors and the arrows are natural transformations.

---

Specifically (generally?), a monad is a monoid in the category of endofunctors.
<br />
<br />
See this [StackOverflow question](http://stackoverflow.com/questions/3870088/a-monad-is-just-a-monoid-in-the-category-of-endofunctors-whats-the-problem) and its answers for more information.

---

#The Monad Type Class

The Cats monad type class is `cats.Monad`.
<br />
<br />
Monad extends two other type classes: `FlatMap`, which provides the `flatMap` method, and `Applicative`, which extends `Functor`.
<br />
<br />
Monads are essentially applicative functors with a `join`. We’ll discuss applicatives next week.

---

#Monad Syntax

The syntax for monads comes from three places:

* `cats.syntax.flatMap` provides syntax for `flatMap`;
* `cats.syntax.functor` provides syntax for `map`;
* `cats.syntax.applicative` provides syntax for `pure`.

This derives from the layout of the equivalent typeclasses in Haskell.

---

    !scala
    import scala.language.higherKinds
    import cats.Monad
    import cats.syntax.functor._
    import cats.syntax.flatMap._
    import cats.syntax.applicative._
    import cats.std.list._
    import cats.std.option


Note that in practice it’s often easier to import everything in one go from `cats.implicits`.

---

# Default Instances

Cats provides instances for all the monads in the standard library (`Option`, `List`, `Vector` and so on) via `cats.std`:

    !scala
    import cats.Monad
    import cats.std.list._
    import cats.std.option
    Monad[Option].pure(3)
    //res0: Option[Int] = Some(3)
    Monad[Option].flatMap(res0)(a => Some(a + 2))
    //res1: Option[Int] = Some(5)
    Monad[List].pure(3)
    //res2: List[Int] = List(3)
    Monad[List].flatMap(res2)(x => List(x, x*10))
    //res3: List[Int] = List(3, 30)

---

    !scala
    def sumSquare[A[_] : Monad]
      (a: Int, b: Int): A[Int] = {
        val x = a.pure[A]
        val y = b.pure[A]
        x flatMap (x => y map (y => x*x + y*y))
    }
    sumSquare[Option](3, 4)
    //res4: Option[Int] = Some(25)
    sumSquare[List](3, 4)
    //res5: List[Int] = List(25)

---

Cats also provides smart constructors and infix notation for bind:

    !scala
    4.some >>= { x:Int => (x*2).some }
    //res6: Option[Int] = Some(8)

---

#Defining Custom Monads

We can define a monad for a custom type simply by providing the implementations of `flatMap` and `pure`:

    !scala
    val myOptionMonad = new Monad[Option] {
      def flatMap[A, B](value: Option[A])
        (f: A => Option[B]): Option[B] =
          value flatMap f
      def pure[A](value: A): Option[A] =
          Some(value)
    }

---

When you define your own monads you must ensure that they satisfy the monad laws.
<br />
<br />
Understanding what makes sense structurally for a `Functor`, `Applicative`, and `Monad` can tell you what is potentially an valid instance before you’ve written any code.

---

#Exercise

Determine whether `CountMe` is a valid monad.

    !scala
    case class CountMe[A](count: Int, data: A)

    val countMeMonad = new Monad[CountMe] {
      def flatMap[A, B](value: CountMe[A])
        (f: A => CountMe[B]): CountMe[B] = {
          val x: CountMe[B] = f(value.data)
          CountMe(value.count+1, x.data)
        }
      def pure[A](value: A): CountMe[A] =
          CountMe(0, value)
    }

---

---

    !scala
    def foo(a: Char) = CountMe(0,a.toInt)
    val a = countMeMonad.pure('a')
    countMeMonad.flatMap(a)(foo _)
    //res7: CountMe[Int] = CountMe(1,97)
    foo('a')
    //res8: CountMe[Int] = CountMe(0,97)

---

#Identity Monad

The `Id` monad provides a monad instance for plain values:

    !scala
    import cats.Id
    import cats.syntax.flatMap._
    val a: Id[Int] = 3
    // a: cats.Id[Int] = 3
    val b: Id[Int] = a.flatMap(_ + 2)
    // b: cats.Id[Int] = 5
    val c: Id[Int] = a + 2
    // c: cats.Id[Int] = 5

This seems confusing. How can we flatMap over an `Id[Int]` and simply add a number to it?

---

The answer is in the definition of `Id`:

    !scala
    type Id[A] = A

<br />

`Id[A]` is simply a type alias for `A` itself. Cats provides the type class instances to allow us to map and flatMap on elements with type `Id[A]`, but Scala still allows us to operate on them as plain values of type `A`.

---

#Exercise

Create your own monad instance for `Id[A]`. How would you define `flatMap` and `pure`?

---

---

    !scala
    val myId = new Monad[Id] {
      def flatMap[A, B](a: Id[A])
        (f: A => Id[B]): Id[B] = f(a)
      def pure[A](a: A): Id[A] = a      
    }

---

    !scala
    myId.flatMap(a)(_ + 2)
    //res9: cats.Id[Int] = 5
    myId.flatMap("hi")(x => myId.map(" there!")(y => x+y))
    //res10: cats.Id[String] = hi there!

---

#Writer Monad

`cats.data.Writer` is a monad that lets us carry a log along with a computation.
<br />
<br />
A `Writer[W, A]` carries two values: a log of type `W` and a result of type `A`.
<br />
<br />
We can use it to record messages, errors, or additional data about a computation, and extract the log with the final result.

---

One common use for Writers is logging during multi-threaded computations, where traditional logging can result in interleaved messages from different contexts.
<br />
<br />
With a `Writer` the log for the computation is appended to the result, so we can run concurrent computations without mixing log messages.

---

We can create a `Writer` from a log and a result as follows:

    !scala
    import cats.data.Writer
    import cats.std.vector._
    Writer(Vector("It all starts here."), 123)
    //res11: WriterT[Vector[String],Int] = ...

We’ve used a `Vector` to hold our log as it has a more efficient append operation than List.

---

Notice that the type of the writer is actually `WriterT[Id, Vector[String], Int]` instead of `Writer[Vector[String], Int]` as we might expect.
<br />
<br />
`WriterT` is an example of an important concept called a “monad transformer”, which we will discuss later.

---

As with other monads, we can also create a `Writer` using the `pure` syntax.

In order to use `pure` the log has to be a type with a `Monoid`. This tells Cats what to use as the initial empty log:

    !scala
    import cats.syntax.applicative._
    type Logged[A] = Writer[Vector[String], A]
    123.pure[Logged]
    // res12: Logged[Int] = WriterT((Vector(),123))

---

We can create a `Writer` from a log using the `tell` syntax. The `Writer` is initialized with the value `Unit`:

    !scala
    import cats.syntax.writer._
    Vector("msg1", "msg2").tell
    //res13: Writer[Vector[String],Unit] = ...

---

If we have both a result and a log, we can create a `Writer` in two ways: using the `Writer.apply` method or the `writer` smart constructor:

    !scala
    import cats.syntax.writer._
    val a = Writer(Vector("msg1", "msg2"), 123)
    //a: WriterT[Id,Vector[String],Int] = ...
    val b = 123.writer(Vector("msg1", "msg2"))
    //b: Writer[Vector[String],Int] = ...

---


We can extract the result and log from a `Writer` using the value and written methods respectively:

    val result = a.value
    //result: Id[Int] = 123
    val log = a.written
    //log: Id[Vector[String]] = Vector(msg1, msg2)

---

or both at once using the `run` method:

    !scala
    a.run
    //res14: Id[(Vector[String], Int)] = ...


---

#Composing and Transforming Writers

When we transform or map over a `Writer`, its log is preserved. When we `flatMap`, the logs of the two `Writer`s are appended.
<br />
<br />
For this reason it’s good practice to use a log type that has an efficient append operation, such as a `Vector`.

---

    !scala
    val writer1 = for {
      a <- 10.pure[Logged]
      _ <- Vector("a", "b").tell
      b <- 32.writer(Vector("x", "y"))
      } yield a + b
    //writer1 = WriterT((Vector(a, b, x, y),42))
    writer1.run
    //res15 = (Vector(a, b, x, y),42)

---

In addition to transforming the result with `map` and `flatMap`, we can transform the log with the `mapWritten` method

    !scala
    writer1.mapWritten(_.map(_.toUpperCase))
    //res16: WriterT[Id,Vector[String],Int] = ...
    res16.run
    //res17 = ???

---

We can also transform both log and result simultaneously using `bimap` or `mapBoth`.
`bimap` takes two function parameters, one for the log and one for the result. `mapBoth` takes a single function of two parameters:

    !scala
    val writer3 = writer1.bimap(
      log => log.map(_.toUpperCase),
      result => result * 100
    )
    writer3.run
    //???

---

    !scala
    val writer4 = writer1.mapBoth {
      (log, result) => val log2 = log.map(_ + "!")
        val result2 = result * 1000
        (log2, result2)
    }
    writer4.run
    //???

---

Interestingly, we can also swap the log and the result.

    !scala
    val writer6 = writer1.swap
    writer6.run
    //res18 = (42,Vector(a, b, x, y))

---

This requires a new monoid for the log, which cats provides implicitly.

    !scala
    for {
      a <- writer6
      _ <- 10.tell
    } yield a
    //res19 = ???

---

Finally, we can clear the log with the `reset` method.

    !scala
    val writer5 = writer1.reset
    writer5.run
    //res20 = (Vector(),42)

---

#Reader Monad

`cats.data.Reader` is a monad that allows us to compose operations that depend on some input.
<br />
<br />
Instances of `Reader` wrap up functions of one argument, providing us with useful methods for composing them.

---

One common use for Readers is injecting dependencies and configurations.
<br />
<br />
If we have a number of operations that all depend on some external configuration (e.g. a `SparkContext`), we can chain them together using a `Reader`.
<br />
<br />
The `Reader` produces one large operation that accepts the configuration as a parameter and runs everything as specified.

---

    !scala
    //basic implementation
    case class Reader[E,A](run: E => A) {
      def flatMap[B](f: A => Reader[E,B]): Reader[E,B] =
        Reader[E,B] { e => f(run(e)).run(e) }
      def map[B](f: A => B): Reader[E,B] =
        Reader[E,B] { e => f(run(e)) }
    }
    object Reader {
      def ask[R]: Reader[R, R] = Reader(r => r)
    }

---

We can create a `Reader[A, B]` from a function of type `A => B` using the `Reader.apply` constructor:

    !scala
    import cats.data.Reader
    def double(a: Int): Int = a*2
    val doubleReader: Reader[Int, Int] = Reader(double)
    // doubleReader: Reader[Int,Int] = Kleisli(<function1>)
    val double2: Int => Int = doubleReader.run
    // double2: Int => Int = <function1>
    double2(10)
    // res0: Int = 20

---

Note that `Reader` is implemented in terms of another type called `Kleisli`.
<br />
<br />
`Kleisli` arrows are a more general form of the `Reader` monad that generalize over the type constructor of the result type. We will discuss these more when we get to monad transformers.
<br />
<br />
The type name `Kleisli` refers to a [Kleisli Category](https://en.wikipedia.org/wiki/Kleisli_category).

---

#Composing Readers

The power of `Reader`s comes from their `map` and `flatMap` methods, both of which represent kinds of function composition.

The `map` method simply extends the computation in the `Reader` by passing its result through a function:

    !scala
    val mapReader = doubleReader.map(_ + "!")
    mapReader.run(10)
    //???

---

The `flatMap` method is more interesting. It allows us to combine two readers that depend on the same input type:

    !scala
    val add1Reader: Reader[Int, Int] = Reader(_ + 1)
    val addReaders: Reader[Int, Int] =
      for {
        x <- doubleReader
        y <- add1Reader
      } yield x + y
    addReaders.run(10)
    //???

---

Notice that the same input value is passed to both `doubleReader` and `add1Reader`.
<br />
<br />
This is the value of the `Reader` monad, which ensures that the same “configuration” (in this case an input number) is passed to each part of the system.

---

We can use the output of the prior step to determine which step to run next. For example:

    !scala
    val sub5Reader: Reader[Int, Int] = Reader(_ - 5)
    val sequencingEx: Reader[Int, (Int, Int)] =
      for {
        x <- doubleReader
        y <- if(x > 20) sub5Reader else add1Reader
      } yield (x, y)
    sequencingEx.run(5)
    //???
    sequencingEx.run(15)
    //???

---

#Exercise: Dependency Injection

    !scala
    def areaR(r: Int): Reader[Double,Double] =
      Reader { pi => pi * r * r }
    val areaRR: Reader[Int,Reader[Double,Double]] =
      Reader { r => areaR(r) }
    def volumeRR(h: Int): Reader[Int,Reader[Double,Double]] =
      areaRR map { areaR =>
        areaR map { a => a * h }
      }
    val volumeRRR = Reader { h: Int => volumeRR(h) }
    //Reader[Int,Reader[Int,Reader[Double,Double]]] = Kleisli(<function1>)
    //What does this remind you of?

---

    !scala
    volumeRR(2) run 1
    //???
    volumeRRR run 2 run 1
    //???

---

---

    !scala
    volumeRR(2) run 1
    //res1: Id[Reader[Double,Double]] = Kleisli(<function1>)
    volumeRRR run 2 run 1
    //res2: Id[Reader[Double,Double]] = Kleisli(<function1>)
    volumeRR(2) run 1 run 3.14
    //res3: Id[Double] = 6.28
    volumeRRR run 2 run 1 run 3.14
    //res4: Id[Double] = 6.28

---

#Reader vs State

    !scala
    //Reader[S,A] has a run: S => A
    def flatMap[B](f: A => Reader[S, B]): Reader[S, B] =
      Reader[S, B] { s => {
      val a = run(s)
      f(a).run(s)
    }
    //State[S,A] has a run: S => (S, A)
    def flatMap[B](f: A => State[S, B]): State[S, B] =
      State[S, B] { s => {
       val (s1, a) = run(s)
       f(a).run(s1)
    }

---

#Writer vs State

The `Writer` and `State` monads are also [closely related](http://stackoverflow.com/questions/23942890/is-the-writer-monad-effectively-the-same-as-the-state-monad).
<br />
<br />
The difference is that `Writer` is much more limited, in that it doesn't allow you to read the accumulated state.
<br />
<br />
The only thing you can do with the state in a Writer is use the monoid to tack more stuff onto the end.

---

#Exercise

Fix `CountMe`s flatMap so that it is a valid monad.

    !scala
    case class CountMe[A](count: Int, data: A)

    val countMeMonad = new Monad[CountMe] {
      def flatMap[A, B](value: CountMe[A])
        (f: A => CountMe[B]): CountMe[B] = {
          val x: CountMe[B] = f(value.data)
          CountMe(value.count+1, x.data)
        }
      def pure[A](value: A): CountMe[A] =
          CountMe(0, value)
    }

---

---

    !scala
    case class CountMe[A](count: Int, data: A)

    val countMeMonad2 = new Monad[CountMe] {
      def flatMap[A, B](value: CountMe[A])
        (f: A => CountMe[B]): CountMe[B] = {
          val x: CountMe[B] = f(value.data)
          CountMe(value.count+x.count, x.data)
        }
      def pure[A](value: A): CountMe[A] =
          CountMe(0, value)
    }

---

    !scala
    def foo(a: Char) = CountMe(0,a.toInt)
    val a = countMeMonad2.pure('a')
    countMeMonad2.flatMap(a)(foo _)
    //res0: CountMe[Int] = CountMe(0,97)
    foo('a')
    //res1: CountMe[Int] = CountMe(0,97)

---

Note that this improvement essentially makes our new `CountMe` monad into a specialized version of `Writer`.
<br />
<br />
Another 'improvement' would have been to drop the count altogether. This would have created a `CountMe` monad equivalent to `Reader`.

---
#Homework

Have a look at monad transformers in [*Cats*](https://github.com/typelevel/cats).

---

#Links
[The essence of functional programming](http://homepages.inf.ed.ac.uk/wadler/papers/imperative/imperative.ps), Phillip Wadler, 1992
<br />
<br />
Imperative functional programming, Simon Peyton Jones (one of the creators of Haskell) and Wadler.
