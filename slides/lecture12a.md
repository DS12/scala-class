#Lecture 12a: Traversable Functors

---

#Foldable

The `Foldable` type class captures the concept of data structures that we can iterate over. Lists are foldable, as are Vectors and Streams.
<br />
<br />
Foldables give us a way to process values embedded in a structure as if they existed in a sequential order, as we’ve seen with list folding.
<br />
<br />
`Foldable` gives us great use cases for monoids and the `Eval` monad.

---

In general, a fold function allows users to transform one algebraic data type to another.
<br />
<br />
The typical use case is to accumulate a value as we traverse. We supply an accumulator value and a binary function to combine it with an item in the sequence.
<br />
<br />
The function produces another accumulator, allowing us to recurse down the sequence. When we reach the end, the final accumulator is our result.

---

Cats’ `Foldable` abstracts the two operations `foldLeft` and `foldRight` into a type class:

    !scala
    trait Foldable[F[_]] { self =>
      def foldLeft[A, B](fa: F[A], b: B)
                        (f: (B, A) => B): B
      def foldRight[A, B](fa: F[A], lb: Eval[B])
                         (f: (A, Eval[B]) => Eval[B]): Eval[B]
      //...
    }

---

Cats provides out-of-the-box instances of Foldable for a handful of Scala data types: `List`, `Vector`, `Stream`, `Option`, and `Map`.

    !scala
    import cats.Foldable
    val ints = List(1, 2, 3)
    import cats.instances.list._
    Foldable[List].foldLeft(ints, 0)(_ + _)
    //res0: Int = 6

---

The `Foldable` instance for `Map` allows us to fold over its values (as opposed to its keys).
<br />
<br />
Because `Map` has two type parameters, we have to fix one of them to create the single-parameter type constructor we need to summon the `Foldable`:

    !scala
    import cats.std.map._
    type StringMap[A] = Map[String, A]
    val stringMap = Map("a" -> "b", "c" -> "d")
    Foldable[StringMap].foldLeft(stringMap, "nil")(_ + "," + _)
    //res1: String = nil,b,d

---

Foldable defines `foldRight` differently from `foldLeft`, in terms of the `Eval` monad:

    !scala
    def foldRight[A, B](fa: F[A], lb: Eval[B])
      (f: (A, Eval[B]) => Eval[B]): Eval[B]
<br />
<br />
Using `Eval` means folding with `Foldable` is always stack safe, even when the collection’s default definition of `foldRight` is not.

---

For example, the default implementation for `Stream` is not stack safe. We can see the stack depth changing as we iterate across the stream.

    !scala
    import cats.Eval
    import cats.Foldable
    def stackDepth: Int = Thread.currentThread.getStackTrace.length

---

The longer the stream, the larger the stack requirements for the fold. A sufficiently large stream will trigger a `StackOverflowException`:

    !scala
    (1 to 5).toStream.foldRight(0) {
      (item, accum) => println(stackDepth)
      item + accum
    }
    //60
    //58
    //56
    //54
    //52
    //res2: Int = 15

---

However since `Eval`'s `map` and `flatMap` are trampolined, `Foldable`'s `foldRight` maintains the same stack depth throughout:

    !scala
    import cats.std.stream._
    val s = (1 to 5).toStream
    val accum: Eval[Int] = Eval.now(0)
    val result: Eval[Int] =
      Foldable[Stream].foldRight(s, accum) {
        (item: Int, accum: Eval[Int]) =>
          println(stackDepth)
          accum.map(_ + item)
    }

---

    !scala
    result.value
    //55
    //55
    //55
    //55
    //55
    //res3: Int = 15

---


#Folding with Monoids

Cats’ `Foldable` provides us with a host of useful methods defined on top of `foldLeft`.
<br />
<br />
Many of these are facsimiles of familiar methods from the standard library, including `find`, `exists`, `forall`, `toList`, `isEmpty`, and `nonEmpty`:

    !scala
    Foldable[Option].nonEmpty(Option(42))
    //res4: Boolean = true
    Foldable[List].find(List(1, 2, 3))(_ % 2 == 0)
    //res5: Option[Int] = Some(2)

---

In addition to these familiar methods, Cats provides two methods that make use of monoids:
<br />
<br />
* `fold` (and its alias `combineAll`) combines all elements in the sequence using their `Monoid`
* `foldMap` maps a user-supplied function over the sequence and combines the results using a `Monoid`

---

For example, we can use `combineAll` to sum over a `List[Int]`:

    !scala
    import cats.std.int._ // import Monoid[Int]
    Foldable[List].fold(List(1, 2, 3))
    //res6: Int = 6

---

Alternatively, we can use `foldMap` to convert each `Int` to a `String` and concatenate them:

    !scala
    import cats.std.string._ // import Monoid[String]
    Foldable[List].foldMap(List(1, 2, 3))(_.toString)
    //???

---

The interesting thing about `fold` and `foldMap` is that they use a `Monoid` instead of a function to give us the final result.
<br />
<br />
One very important aspect to understand here is that it is the `fold` function that requires the elements of `Foldable` to have a `Monoid` instance, while `Foldable` itself does not have that restriction.

---

    !scala
    trait Foldable[F[_]] { self =>
      //foldLeft, foldRight etc
      def fold[A](fa: F[A])
                 (implicit A: Monoid[A]): A =
        foldLeft(fa, A.empty) { (acc, a) =>
          A.combine(acc, a)
        }

      def foldMap[A, B](fa: F[A])
                       (f: A => B)
                       (implicit B: Monoid[B]): B =
        foldLeft(fa, B.empty) { (b, a) =>
          B.combine(b, f(a))
        }
    }

---

The same goes for `foldM`, which implements left associative monadic folding using an implicit `Monad`:

    !scala
    trait Foldable[F[_]] { self =>
      //foldLeft, foldRight, fold, foldMap etc
      def foldM[G[_], A, B](fa: F[A], z: B)
                           (f: (B, A) => G[B])
                           (implicit G: Monad[G]): G[B] =
        foldLeft(fa, G.pure(z)) { (gb, a) =>
          G.flatMap(gb)(b => f(b, a))
        }
    }

---

    !scala
    import cats.implicits._
    def binSmalls(acc: Int, x: Int): Option[Int] =
      if (x > 9) none[Int] else (acc + x).some
    (Foldable[List].foldM(List(2, 8, 3, 1), 0) {binSmalls})
    //???
    (Foldable[List].foldM(List(2, 10, 3, 1), 0) {binSmalls})
    //???

---

Finally, we can compose `Foldable`s to support deep traversal of nested sequences:

    !scala
    import cats.std.vector._
    val deepFold = Foldable[List].compose(Foldable[Vector])
    val ints = List(Vector(1, 2, 3), Vector(4, 5, 6))
    deepFold fold ints
    //res7: Int = 21

---

#Traverse

In functional programming it is very common to encode "effects" as data types - common effects include Option for possibly missing values, Xor and Validated for possible errors, and Future for asynchronous computations.
<br />
<br />
These effects tend to show up in functions working on a single piece of data - for instance parsing a single String into an Int, validating a login, or asynchronously fetching website information for a user.

---

    !scala
    def parseInt(s: String): Option[Int] = ???
    import cats.data.Xor
    import scala.concurrent.Future
    trait SecError
    trait Token
    def validateLogin(cred: Token): Xor[SecError, Unit] = ???
    trait Profile
    trait User
    def userInfo(user: User): Future[Profile] = ???

---

Each function asks only for the data it actually needs; in the case of userInfo, a single User.
<br />
<br />
We could write a function that takes a list of users and fetches profiles for all of them, but that would be a bit strange.
<br />
<br />
If we just wanted to fetch the profile of just one user, we would either have to wrap it in a List or write a separate function that takes in a single user anyways.

---

More fundamentally, functional programming is about building lots of small, independent pieces and composing them to make larger and larger pieces - does this hold true in this case?
<br />
<br />
Given just `User => Future[Profile]`, what should we do if we want to fetch profiles for a `List[User]`? We could try familiar combinators like `map`:

    !scala
    def profilesFor(users: List[User]) = users.map(userInfo)
    profilesFor: (users: List[User])List[Future[Profile]]

---

Note the return type `List[Future[Profile]]`. This makes sense given the type signatures, but seems unwieldy.
<br />
<br />
We now have a list of asynchronous values, and to work with those values we must then use the combinators on Future for every single one.
<br />
<br />
It would be nicer instead if we could get the aggregate result in a single `Future`, say a `Future[List[Profile]]`.

---

As it turns out, the `Future` companion object has a traverse method on it.
<br />
<br />
However, that method is specialized to standard library collections and `Futures` - there exists a much more generalized form that would allow us to do things like parse a `List[String]` or validate credentials for a `List[User]`.
<br />
<br />
Enter `Traverse`.

---

`Traverse` depends on `Applicative` (and thus `Functor`) as well as `Foldable`:

    !scala
    trait Traverse[F[_]] extends Functor[F] with Foldable[F] { self =>

      def traverse[G[_]: Applicative, A, B]
        (fa: F[A])(f: A => G[B]): G[F[B]]

      def sequence[G[_]: Applicative, A]
        (fga: F[G[A]]): G[F[A]] = traverse(fga)(ga => ga)
      //...
    }

---

`sequence` threads all the `G` effects through the `F` structure to invert the structure from `F[G[_]]` to `G[F[_]]`.
<br />
<br />
`traverse` allows you to transform elements inside the structure like a `Functor`, producing applicative effects along the way, and lift those instances of applicative structure outside of the `Traversable` structure.
<br />
<br />
Given a function which returns a `G` effect, `traverse` threads this effect through the running of this function on all the values in `F`, returning an `F[A]` in a `G` context.

---

In our above examples, `F` is `List`, and `G` is `Option`, `Xor`, or `Future`.
<br />
<br />
For the profile example, `traverse` says given a `List[User]` and a function `User => Future[Profile]`, it can give you a `Future[List[Profile]]`.
<br />
<br />
More generally, `F[_]` is some sort of context which may contain a value (or several). While `List` tends to be among the most general cases, there also exist `Traversable` instances for `Option`, `Xor`, and `Validated` (among others).

---

One way to think of `traverse` is as a generalization of `sequence`:

    !scala
    List(1,2,3).traverse(_.some)
    //res8 = ???
    List(1,2,3).map(_.some).traverse(identity)
    //res9 = ???
    List(1,2,3).map(_.some).sequence
    //res10 = ???


---

Another is as a generalization of `map`:

    !scala
    import cats.syntax.traverse._
    List(1,2,3).traverse[Id, Int]((x: Int) => x + 1)
    //res11 = ???

---

Type signature of `traverse` in relation to `map` and `flatMap`:

    !scala
    def      map(f: A =>   B) : F[A] =>   F[B]
    def traverse(f: A => G[B]): F[A] => G[F[B]]     
    def  flatMap(f: A => F[B]): F[A] =>   F[B]


---

We’re still mapping a function over some embedded value(s), like `map`, but similar to `flatMap`, the function is itself generating more structure.
<br />
<br />
However, unlike `flatMap`, the generated structure is of a different type than the embedded structure.

---

Finally, note the implied relationship between `Foldable` and `Traverse`.

* `Foldable` does not extend `Monoid`, but has methods that rely on monoidal values (e.g. `def fold[M: Monoid](fa: F[M])`)

* `Traverse` does not extend `Applicative`, but has methods that rely on applicative structures (e.g. `def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B])`)

---

`Traverse` can also express `foldMap` and by extension `foldLeft` and `foldRight`.
<br />
<br />
Suppose that our `G` is a type constructor `Const` that takes any type to `Int`, so that `Const[Int, A]` throws away its type argument `A` and just gives us back the underlying `Int`:

    !scala
    import cats.data.Const
    Const(1)
    //res12: Const[Int,Nothing] = Const(1)
    Const(1) map { (_: String) + "!" }
    //res13: Const[Int,String]  = Const(1)

---

When the first type argument forms a `Semigroup`, an `Apply` is derived, and when it forms a `Monoid`, an `Applicative` is derived automatically.

    !scala
    import cats.syntax.apply._
    val a: Const[Int,String => String] = Const(2)
    val b: Const[Int,String] = Const(1)
    a ap b
    //res14: Const[Int,String] = Const(3)
<br />
<br />

With a `Const` functor we can turn any `Monoid` into an `Applicative`, so we should be able to get a `foldMap` from our `traverse`.

---

If we instantiate `G` to be `Const[M, Nothing]`, `traverse` begins to look a lot like `foldMap` from `Foldable`

    !scala
    def traverse[A,B]
                (fa: F[A])
                (f: A => Const[M, Nothing]): Const[M, F[Nothing]]
    def foldMap [A,B]
                (fa: F[A])
                (f: A => M): M

---

    !scala
    def foldMap[A, M: Monoid, F[_]:Traverse]
              (fa: F[A])
              (f: A => M): Const[M,F[Nothing]] =
              fa traverseU { (a: A) => Const((f(a))) }
    }
    foldMap(List('a', 'b', 'c')) { c: Char => c.toInt }
    //res15: Const[Int,List[Nothing]] = Const(294)
    foldMap(Nil) { c: Char => c.toInt }
    //res16: Const[Int,List[Nothing]] = Const(0)

Note that we are using `traverseU`, which is the [Unapply variant](http://eed3si9n.com/herding-cats/Unapply.html) of `traverse`.

---

If we let our aggregator exit the `Const` then we get `foldMap` exactly:

    !scala
    def foldMap[A, M: Monoid, F[_]:Traverse]
              (fa: F[A])
              (f: A => M): M =
    {
      val x = fa traverseU { (a: A) => Const((f(a))) }
      x.getConst
    }
    foldMap(List('a', 'b', 'c')) { c: Char => c.toInt }
    //res17: Int = 294

---

#Traversing lists

Here is the cats implementation of `traverse` for `List`:

    !scala
    def traverse[G[_], A, B]
                (fa: List[A])
                (f: A => G[B])
                (implicit G: Applicative[G]): G[List[B]] =
      foldRight[A, G[List[B]]](fa, Always(G.pure(List.empty))) {
        (a, glb) => G.map2Eval(f(a), glb)(_ :: _)
      }.value
<br />
<br />
Note that `map2Eval` is similar to `map2` but uses `Eval` to allow for laziness in the second argument.

---

Here is a standalone implementation using `ap`:

    !scala
    def myTraverse[G[_], A, B]
                (fa: List[A])
                (f: A => G[B])
                (implicit G: Applicative[G]): G[List[B]] =
      fa.foldRight[G[List[B]]](G.pure(List.empty)) {
        val cons = (h: B, t: List[B]) => h :: t
        (a, glb) => G.ap(f(a).map(cons.curried))(glb)
      }
<br />
<br />
Recall the `ap` type signature: `ap(f: F[A => B]): F[A] => F[B]`. What is being applied here?

---

#Exercise

Let's do a specific example where `A` and `B` are `Int`, and `G` is `Option`:

    !scala
    val l = List(1,2,3)
    val cons = (h: Int, t: List[Int]) => h :: t
    Apply[Option].ap(None.map(cons.curried))(l.some)
    //???
    def f(i: Int): Option[Int] = if(i % 2 == 0) Some(i) else None
    val baz = (a: Int, glb: Option[List[Int]]) =>     
      Apply[Option].ap(f(a).map(cons.curried))(glb)
    baz(1, Some(l))
    //???
    baz(4, Some(l))
    //???

---

#Exercise

Quick `State` monad refresher:

    !scala
    type IntState[A] = State[Int, A]
    def pure[A](a: A): IntState[A]  = State(s => (s, a ))
    def set(s: Int): IntState[Unit] = State(_ => (s, ()))
    val l = List(1,2,3)
    val a = myTraverse(l)(pure)
    a.run(0).value
    //???
    val b = myTraverse(l)(set)
    b.run(0).value
    //???    
<br />
<br />
We'll do a more involved example of traversing with `State` in the tutorial.

---


# `Traverse` Laws

`Traverse[T[_]]` has two laws.
<br />
<br />
There are many ways to state the laws, we present one way here, but you may see them in different configurations elsewhere.

---

#Identity

The identity law for `Traverse` can be stated as:

    !scala
    sequence[Id,A](xs) == xs

The identity law says that traversing in the identity applicative (`type Id[X] = X`) has no effect.

---

#Composition

The composition law for `Traverse` can be stated as:

    !scala
    sequence[Lambda[a => F[G[a]]],A](xs) ==
      map(sequence[F,G[A]](xs))(sequence[G,A])
<br />
<br />
The fusion law says that traversal in `F[_]` followed by traversal in `G[_]` can be fused into one traversal in the composite applicative `F[G[_]]`.

---

#Traversable Monads

Last week we tried to implement `flatMap` with a monad composition:

    !scala
    def flatMap[A, B](fa: M1[M2[A]])(f: A => M1[M2[B]]): M1[M2[B]]
<br />
<br />

We saw that we'd end up with something of type `M1[M2[M1[M2[B]]]]`. This is normally irreducible,

However if `M2` is traversable then we can use its sequence method to swap layers and  get something of type `M1[M1[M2[M2[B]]]]`, then use the `join`s from `M1` and `M2` to get a result of type `M1[M2[B]]`.  

---

The tutorial contains an exercise on combining two monads into one composite monad when the inner monad has a `Traverse` instance.
<br />
<br />
While the types always allow this, the result fails to meet the monad laws unless the traversable monad is also a [_commutative monad_](https://wiki.haskell.org/Monad#Commutative_monads).

---

Two examples of commutative monads are `Reader` and `Option`.
<br />
<br />
Two examples of monads that are _not_ commutative are `List` and `State`.
<br />
<br />
See the [Wikipedia article on the "distributive law between monads"](http://en.wikipedia.org/wiki/Distributive_law_between_monads) and [a more in-depth article on n-lab](http://ncatlab.org/nlab/show/distributive+law).

---

#Example: Futures

Going back to our earlier `Future` example, we can write an `Applicative` instance for `Future` that runs each `Future` concurrently.
<br />
<br />
Then when we traverse a `List[A]` with an `A => Future[B]`, we perform a [scatter-gather](https://en.wikipedia.org/wiki/Gather-scatter_(vector_addressing)) operation
<br />
<br />
Each `A` creates a concurrent computation that will produce a `B` (the scatter), and as the futures complete they will be gathered back into a list.

---

#Example: Parsers

    !scala
    import cats.Semigroup
    import cats.data.{NonEmptyList, OneAnd, Xor}
    import cats.data.{Validated, ValidatedNel}
    import cats.std.list._
    import cats.syntax.traverse._
    def parseIntXor(s: String): Xor[NumberFormatException, Int] =
      Xor.catchOnly[NumberFormatException](s.toInt)
    val x1 = List("1", "2", "3").traverseU(parseIntXor)
    //Right(List(1, 2, 3))
    val x2 = List("1", "abc", "def").traverseU(parseIntXor)
    //Left(NumberFormatException: For input string: "abc")

---

Traversal behavior is closely tied with the behavior of the underlying `Applicative`.

    !scala
    def parseIntValidated(s: String):
      ValidatedNel[NumberFormatException, Int] =
      Validated.catchOnly[NumberFormatException](s.toInt)
               .toValidatedNel   
    List("1", "2", "3").traverseU(parseIntValidated)
    //Valid(List(1, 2, 3))
    val v3 = List("1", "abc", "def").traverseU(parseIntValidated)
    //Invalid(OneAnd(NumberFormatException: For input string:
    //"abc",List(NumberFormatException: For input string: "def")))

---

#Example: Readers

Another interesting effect we can use is `Reader`. Recall that a `Reader[E, A]` is a type alias for `Kleisli[Id, E, A]` which is a wrapper around `E => A`.
<br />
<br />
If we fix `E` to be some sort of environment or configuration, we can use the `Reader` applicative in our traverse.

---

Imagine we have a data pipeline that processes a bunch of data, each piece of data being categorized by a topic.
<br />
<br />
Given a specific topic, we produce a `Job` that processes that topic:

    !scala
    import cats.data.Reader
    trait Context
    trait Topic
    trait Result
    type Job[A] = Reader[Context, A]
    def processTopic(topic: Topic): Job[Result] = ???

---

We'd like to aggregate many topics (a `List[Topic]`) and compose the resulting `Job`s together into one `Job[List[Result]]`.
<br />
<br />
Since `Reader` has an `Applicative` instance, we can do this by traversing over the list with `processTopic`:

    !scala
    def processTopics(topics: List[Topic]) =
      topics.traverse(processTopic)
    //returns Job[List[Result]]

---

When our new job's `run` method is called, it will go through each topic and run its topic-specific job, collecting results as it goes.
<br />
<br />
Note that our job's run method has type signature `Context => Result`, so it requires a `Context` before producing the value we want.
<br />
<br />
For example, in Spark the information needed to run a Spark job (where the master node is, memory allocated, etc.) resides in a `SparkContext`.

---

Going back to the above example, we can see how one may define topic-specific Spark jobs (type `Job[A] = Reader[SparkContext, A]`) and then run several Spark jobs on a collection of topics via `traverse`.
<br />
<br />
We then get back a `Job[List[Result]]`, which is equivalent to `SparkContext => List[Result]`.
<br />
<br />
When finally passed a `SparkContext`, we can run the job and get our results back.

---

Moreover, the fact that our aggregate job is not tied to any specific `SparkContext` allows us to pass in a `SparkContext` pointing to a production cluster, or (using the exact same job) pass in a test `SparkContext` that just runs locally across threads. This makes testing large jobs easy.
<br />
<br />
Finally, this encoding ensures that all the jobs for each topic run on the exact same cluster. At no point do we manually pass in or thread a `SparkContext` through - that is taken care for us by the (applicative) effect of `Reader` and therefore by `traverse`.

---

#Homework

Read Chapter 13 of _Functional Programming in Scala_.

---

#Links

---

#Links

* [Advanced Scala with Cats](http://underscore.io/books/advanced-scala/)

* [The Essence of the Iterator Pattern](http://web.comlab.ox.ac.uk/oucl/work/jeremy.gibbons/publications/#iterator)

* [An Investigation of the Laws of Traversals](http://arxiv.org/pdf/1202.2919)

* [Typeclassopedia](http://www.haskell.org/haskellwiki/Typeclassopedia)
