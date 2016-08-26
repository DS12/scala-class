
#Lecture 4: Options

---

#Imperative Error Handling

* Exceptions
* Sentinel Values
* flagged arguments

---

#Exceptions

    !scala
    def mean(xs: Seq[Double]): Double =
      if (xs.isEmpty)
        throw new ArithmeticException("NaN")
      else xs.sum / xs.length

---

The mean function is an example of what’s called a partial function: it’s not defined for some inputs.

A function is typically partial because it makes some assumptions about its inputs that aren’t implied by the input types.

---

#Sentinel Values

    !scala
    def mean1(xs: Seq[Double]): Double =
      if (xs.isEmpty) Double.NaN
      else xs.sum / xs.length

---

#Flagged Arguments

    !scala
    def mean2(xs: Seq[Double], empty: Double): Double =
      if (xs.isEmpty) empty
      else xs.sum / xs.length

---

Exceptions are not type-safe or referentially transparent.

The type of `mean`, `Seq[Double]) => Double` tells us nothing about the fact that exceptions may occur, and the compiler will not force callers of `mean` to make a decision about how to handle those exceptions.

---

`Option` is a *container* that may or may not hold something.

`Option` containers can be *chained* together, with `flatMap`.

---

`Option` itself is generic and has two subclasses: Some[T] or None

    !scala
    trait Option[+A] //base trait
    case class Some[+A](get: A)
	  extends Option[A]
    case object None
	  extends Option[Nothing]

`case class` and `case object` provide built-in constructors and pattern-matching.
`class` and `object` do not.

---

`Option`s are like `List`s with at most a single element.

    !scala
    trait List[+A] //base trait
    case class Cons[+A](head: A, tail: List[A])
	  extends List[A]
    case object Nil
	  extends List[Nothing]

---

#Failure is an `Option`

`Option` tells you that a function might not return what you’re asking for.

    scala> val numbers = Map("one" -> 1, "two" -> 2)
    numbers: Map(one -> 1, two -> 2)
    scala> numbers.get("two")
    res0: Option[Int] = Some(2)
    scala> numbers.get("three")
    res1: Option[Int] = None

---

#Application: Computing a mean

    !scala
    def mean(xs: Seq[Double]): Option[Double] =
      if (xs.isEmpty) None
      else Some(xs.sum / xs.length)

---

The basic interface for Option consists of three methods:

    !scala
    trait Option[+A] {
      def map[B](f: A => B): Option[B]
      def flatMap[B](f: A => Option[B]): Option[B]
      def getOrElse[B >: A](default: => B): B
    }

---

#"Container" pattern

`Option` follows this pattern, along with several other types this we will encounter.

Containers have many methods.  Some of these methods fulfill one of these two purposes:

* combinators for entering the Container
* combinators for exiting the Container -- safely

---

Combinators for entering the Container

* `unit` -- `def unit[A](a: A): Option[A]`
* [`apply`](http://www.scala-lang.org/api/current/index.html#scala.Option$@apply[A](x:A):Option[A]) -- equivalent to `unit` (also sometimes called `point`)
* `lift` -- `def lift[A,B](f: A => B): Option[A] => Option[B]`

---

Combinators for exiting the Container -- safely

* [`fold`](http://www.scala-lang.org/api/current/index.html#scala.Option@fold[B](ifEmpty:=%3EB)(f:A=%3EB):B) -- `def fold[A,B](ifEmpty: => B)(f: (A) => B): B`
* [`getOrElse`](http://www.scala-lang.org/api/current/index.html#scala.Option@getOrElse[B%3E:A]%28default:=%3EB%29:B) -- Given `Option[A]`, `getOrElse(alternativeA: A): A`


---

    !scala
    def getOrElse[B>:A](default: => B): B =
  	  this match {
          case None => default
          case Some(a) => a
        }

The tutorial will explain the necessity of the lower type bound generic `B` in the `getOrElse` combinator.

---

#Maps on Options

    !scala
    def map[B](f: A => B): Option[B] =
  	  this match {
          case None => None
          case Some(a) => Some(f(a))
        }

---

    scala> val a = Some(3)
    a: Some[Int] = Some(3)
    scala> a map (_+2)
    res0: Option[Int] = Some(5)

Making the wildcard function (`_ + 2`) explicit,

	a.map { (i: Int) => i+2 }


---

    scala> val b: Option[Int] = None
    b: Option[Int] = None
    scala> b map (_+2)
    res1: Option[Int] = None

---


#`flatMap` for `Option`

    !scala
    def flatMap[B](f: A => Option[B]): Option[B] =
  	  this match {
          case None => None
          case Some(a) => f(a)
        }

---

`flatMap` is extremely useful when dealing with Options — it will collapse chains of options down to one.

We will talk much more in depth about this pattern when we get to Monads.

---

#Exercise

Implement `flatMap` using the other core methods `map` & `getOrElse`.


Going forward, it will be increasingly important to note which combinators can be implemented using other combinators.  

---

---

    !scala
    def flatMap1[B](f: A => Option[B]): Option[B] =
      map(f) getOrElse None

---

#Reduction of for loops

    !scala
    for {
      i <- List(0, 1)
    } yield(i + 1)

    List(0, 1) map {i => i + 1}

---

    !scala
    for {
      i <- List(0, 1)
      j <- List(2, 3)
    } yield(i * j)

    List(0, 1) flatMap {
      i => List(2, 3) map {
        j => i * j
      }
    }

---

#'Looping' over Options

Given `def foo(x: Int, y: Int): Int = x+y`

    scala>  val a = Some(3); val b = Some(4)
    a: Some[Int] = Some(3)
    b: Some[Int] = Some(4)
    scala>   for {
         |     x <- a
         |     y <- b
         |   } yield { foo(x, y) }
    res0: Option[Int] = Some(7)

---

Equivalently

Given `def foo(x: Int, y: Int): Int = x+y`

    scala>  val opA = Some(3); val opB = Some(4)
    opA: Some[Int] = Some(3)
    opB: Some[Int] = Some(4)
  	scala> opA.flatMap { a =>
  	        	opB.map { b => foo(a,b) }
      		}  			
    res1: Option[Int] = Some(7)

Why use `map` on `opB`, rather than `flatMap`?  

---

A different `foo`

`def foo(x: Int, y: Int): Option[Int] = if(x<0) Some(x+y) else None`

    scala> val opA = Some(3); val opB = Some(4)
    opA: Some[Int] = Some(3)
    opB: Some[Int] = Some(4)
  	scala> opA.flatMap { a =>
          		opB.flatMap { b => foo(a,b) }
      		}			
    res2: Option[Int] = None

---

    scala> val c = None
    c: None.type = None
    scala>   for {
         |     x <- a
         |     y <- c
         |   } yield foo(x, y)
    res3: Option[Int] = None

---

#Exercise

Provided a function `mean`,

  	!scala
  	mean(xs: Seq[Double]): Option[Double] = ...

implement a `variance` function with the following signature:

    !scala
    variance(xs: Seq[Double]): Option[Double]

---


---

    !scala
    def variance(xs: Seq[Double]): Option[Double] =
      mean(xs) flatMap { m =>
        mean(xs.map(x => math.pow(x - m, 2)))
	  }

---

#Lifting Functions

    !scala
    def lift[A,B](f: A => B): Option[A] => Option[B] =
      _ map f

The wildcard above is an anonymous function

  	!scala
  	def lift[A,B](f: A => B): Option[A] => Option[B] =
  	  (opA: Option[A]) => opA.map(f)


---

    scala> lift(math.abs)
    res4: Option[Int] => Option[Int] = <function1>
    scala> lift(math.pow)
    <console>:38: error: type mismatch;
     found   : (Double, Double) => Double
     required: ? => ?

---

#Exercise

Implement a `map2` function with the following signature:

    !scala
    def map2[A,B,C](a: Option[A], b: Option[B])
    (f: (A, B) => C): Option[C]

---

---

    !scala
    def map2[A,B,C](a: Option[A], b: Option[B])
	  (f: (A, B) => C): Option[C] =
      a flatMap (aa => b map (bb => f(aa, bb)))

---

#this pattern may look familiar

    !scala
    def map2[A,B,C](a: Option[A], b: Option[B])
	  (f: (A, B) => C): Option[C] =
      a flatMap {
        i => b map {
          j => f(i, j)
        }
      }

---

We could even implement `map2` with a for-comprehension:

    !scala
    def map2[A,B,C](a: Option[A], b: Option[B])
	  (f: (A, B) => C): Option[C] =   
      for {
        i <- a
        j <- b
      } yield f(i, j)


---

# Try

`Try` is a general-purpose function for converting from an exception-based API to a container-based API. This is a common pattern in *FP in Scala*.

  	!scala
  	def Try[A](a: => A): Option[A] =
  	  try { Some(a) }
  	  catch { case e: Exception => None }


The `None` returned is not informative about the exception thrown.  Next lecture's type `Either` fixes this.

---

`=>` in a function argument means the argument is lazy.  `=>` does not memoize.
If `a` were evaluated eagerly, an exception thrown in its evaluation would be outside the try-catch clause.

Think of lazy argument `a: => A` as a function with no input: `() => A`.

  	!scala
  	def Try[A](lazyA: () => A): Option[A] =
  	  try { Some( lazyA() ) }
  	  catch { case e: Exception => None }

---

#`sequence` and `traverse`

`def sequence[A](a: List[Option[A]]): Option[List[A]]`

`def traverse[A, B](a: List[A])(f: A => Option[B]): Option[List[B]]`

These are two combinators we will see repeatedly throughout the course.

Both are derived from `flatMap` and `map`.

---

    scala> sequence(List(Some(1), Some(2)))
    res5: Option[List[Int]] = Some(List(1, 2))
    scala> sequence(List(Some(1), None))
    res6: Option[List[Int]] = None

---

Given `def foo(x: Int) = if (x==2) None else Some(x)`

    scala> traverse(List(1,2,3))(foo)
    res7: Option[List[Int]] = None
    scala> traverse(List(1,3,4))(foo)
    res8: Option[List[Int]] = Some(List(1, 3, 4))

---

#Exercise

Implement `sequence[A](a: List[Option[A]]): Option[List[A]]`.

---

---

    !scala
    def sequence[A](a: List[Option[A]]):
    Option[List[A]] =
      a.foldRight[Option[List[A]]]
        (Some(Nil))
        ((x,y) => map2(x,y)(_ :: _))

---

    !scala
    def sequence1[A](a: List[Option[A]]):
    Option[List[A]] =
      a match {
        case Nil => Some(Nil)
        case h :: t => h flatMap (hh => sequence(t) map (hh :: _))
      }

---

`traverse` can be trivially implemented with `sequence` and `map`.

    !scala
    def traverse1[A, B](a: List[A])(f: A => Option[B]):
	  Option[List[B]] =
      sequence a.map(f)

However this implementation is inefficient.

---

#Exercise

Implement `traverse` so that it traverses the list only once.

---

---

    !scala
    def traverse[A, B] (a: List[A])(
    f: A => Option[B]): Option[List[B]] =
        a.foldRight[Option[List[B]]] (Some(Nil)) {
          (h,t) => map2(f(h),t)(_ :: _)
        }

---

    !scala
    def traverse1[A, B] (a: List[A])(
	  f: A => Option[B]): Option[List[B]] =
      a match {
        case Nil => Some(Nil)
        case h :: t => f(h) flatMap {
          x: B => traverse(t)(f) map { xt => x :: xt }
        }
      }

---

    !scala
    def traverse2[A, B] (a: List[A])(
	  f: A => Option[B]): Option[List[B]] =
      a match {
        case Nil => Some(Nil)
        case h :: t => traverse(t)(f) flatMap { xt =>
          f(h) map { x: B => x :: xt }
        }
      }


---

The fact that `traverse1` and `traverse2` are equivalent is a simple result of the Monad laws.

`traverse` is not equivalent to the other two, it uses only `map2` and is therefore an applicative functor.

We'll discuss this more in the weeks to come.

---

#Homework 

Have a look at `Either` and `Validation` in [*Cats*](https://github.com/typelevel/cats).
