# Option Tutorial

## Part 0

In this tutorial we will understand the usage patterns, combinators, and implementation of `Option`.

We begin with usage examples of the [Scala Collections `Option`](http://www.scala-lang.org/api/current/index.html#scala.Option).

Follows is an explanation of the necessity of covariance and type bounds in `List`.

We conclude with fixing a flawed implementation of `Option` to strengthen our grip on subtyping, covariance, and lower type bounds.  We will also implement some combinators.


--------------------

## Part 1: `Option` Usage Patterns

`tutorial/src/main/scala/com/datascience/education/tutorial/lecture4/EmptySet.scala`

Consider these methods:

`sumList` and `sumList2` sum the elements of a `List[Int]`.
`prodList` and `prodList2` multiply the elements of a `List[Double]`.

```
  def sumList(l: List[Int]): Int = l match {
    case Nil => 0
    case x :: xs => x + sumList(xs)
  }
  def prodList(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case x :: xs => x * prodList(xs)
  }
  
  def sumList2(l: List[Int]) = foldRight(l, 0)(_ + _)
  def prodList2(l: List[Double]) = foldRight(l, 1.0)(_ * _)
  
```

In this implementation, two math errors are permitted:  
The sum of an empty collection of Ints is not 0,
and the product of an empty collection of Doubles in not 1.0.

Previously, we had no way of describing the *absence* of an answer.  A conventional solution would have return a sentinel value like `Double.NaN`.  We can do better.

### Task (1a): `sum`

Use [`map`](http://www.scala-lang.org/api/current/index.html#scala.Option@map[B](f:A=>B):Option[B]), [`orElse`](http://www.scala-lang.org/api/current/index.html#scala.Option@orElse[B>:A](alternative:=>Option[B]):Option[B]), and the [`Option` constructor](http://www.scala-lang.org/api/current/index.html#scala.Option$@apply[A](x:A):Option[A]) to implement `sum`.  Use pattern matching like `sumList` does:

```
	def sum(l: List[Int]): Option[Int] = ???
```	

Do not "exit" the `Option` to perform operations on its contents -- a common anti-pattern with "containers" like `Option`.

### Task (1b): `product`

Use the same combinators to implement `product`.  Use pattern matching like `prodList` does:

```
  def product(l: List[Double]): Option[Double] = ???
```

### Task (1c): `sum2`

Use the same combinators to implement `sum2`.  Use `foldRight` like `sumList2` does:

```
  def sum2(l: List[Int]): Option[Int] = ???
```

### Task (1d): `product2`

Use the same combinators to implement `product2`.  Use `foldRight` like `prodList2` does:

```
  def product2(l: List[Double]): Option[Double] = ???
```

---------------------

## Part 2: Safe Division

`tutorial/src/main/scala/com/datascience/education/tutorial/lecture4/SafeDivision.scala`

### Task (2a): `safeDiv`

Implement 

```
  def safeDiv(x: Int, y: Int): Option[Double] = ???
```

`safeDiv` should catch division by zero and results that are `NaN`, positive infinity, or negative infinity.  Use methods defined on Java's `Double` class to determine these last three conditions.  

Use a `try-catch` statement.  The user of this method will not be concerned with `try-catch` though.  See [The Scala try, catch, finally syntax (multiple exceptions, wildcard operator)](http://alvinalexander.com/scala/scala-try-catch-finally-syntax-examples-exceptions-wildcard
).  You will not need to catch multiple types of exceptions, or the `finally` statement.

[JavaDoc for `Double`](https://docs.oracle.com/javase/7/docs/api/java/lang/Double.html)

Catch a `java.lang.ArithmeticException`.  

Do not catch any `Exception`.  This cannot be tested in this example, but is applicable to Cats' `Xor`.

Test your work with runnable class `SafeDivisionExamples`.

This fills a similar purpose to [`Try` in the Scala Collections](http://www.scala-lang.org/api/current/index.html#scala.util.Try) or [`Xor.catchNonFatal` in Cats](https://static.javadoc.io/org.typelevel/cats-core_2.11/0.7.2/index.html#cats.data.Xor$@catchNonFatal[A](f:=>A):cats.data.Xor[Throwable,A])

### Task (2b): `traverseFractions`

Implement

```
	def traverseFractions(ll: List[(Int, Int)]): Option[List[Double]] = ???
```


The input is a list of tuples, where the first member of each tuple is the numerator, and the second member is the denominator. The output should contain the values of each of these fractions as type `Double`.

Find and use method `traverse` defined in object `TraverseOption`, located in file
`tutorial/src/main/scala/com/datascience/education/tutorial/lecture4/TraverseOption.scala`

Provided in `TraverseOption`:

```
def traverse[A, B](listA: List[A])(f: A => Option[B]): Option[List[B]] = ...
```


A note about `TraverseOption`:

`traverse` as implemented in FPiS Chapter 4 is simplified.

`(A => Option[B]) => Option[List[B]]`, as it is in Chapter 4, really should be a combinator on `List`, not `Option`.

[Cats implements it this way:](http://typelevel.org/cats/api/index.html#cats.Applicative@traverse[A,G[_],B](value:G[A])(f:A=>F[B])(implicitG:cats.Traverse[G]):F[G[B]])

```
def traverse[A, G[_], B](value: G[A])(f: (A) ⇒ F[B])(implicit G: Traverse[G]): F[G[B]]
```

The `traverse` method with type signature that matches up with Chapter 4 resides in  [`listInstance`](http://typelevel.org/cats/api/index.html#cats.instances.package$$list$)


### Task (2c): `traverseSqrtFractions`

Implement

```
def traverseSqrtFractions(ll: List[(Int, Int)]): Option[List[Double]] =
```

The input is again a list of tuples, where the first member of each tuple is the numerator, and the second member is the denominator. However, in this task, output for each tuple the *square root* of the fraction instead. You should use a combinator to string together functions to calculate the fraction and then the square root of the fraction. Be sure to consider the case where the square root is `NaN`.  

---------------------

## Part 3: Scala Collections [`Map`](http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.Map) and `Option`

`tutorial/src/main/scala/com/datascience/education/tutorial/lecture4/Employees.scala`

Previously we retrieved values from a `Map` unsafely.  If the key did not exist in the map, a run-time exception was thrown.

[With the `get` method](http://www.scala-lang.org/api/current/index.html#scala.collection.Map@get(key:A):Option[B]
), we can retrieve a value for a key safely.

### Task (3a): `employeeEmail`

Implement 

```
  def employeeEmail(id: UUID): Option[Email] = ???
```

This should be a one-liner.

Test your work with runnable class `EmployeesExample`.

---------------------

## Part 4: Covariance and Type Bounds in `List`

`tutorial/src/main/scala/com/datascience/education/tutorial/lecture4/Covariance.scala`

This is a walk-through.  These concepts will be used in the tasks of <b>Part 5</b>.

### (4a): A Linked List of Fixed Type
```
scala> import com.datascience.education.tutorial.lecture4.Covariance._
import com.datascience.education.tutorial.lecture4.Covariance._
```

A simple linked list that has no need for a generic type:

```
  sealed trait LinkedListInt
  case class ConsInt(h: Int, t: LinkedListInt) 
    extends LinkedListInt
  case object NilInt extends LinkedListInt
```

`LinkedListInt` is a trait (abstract) so cannot be used as a value:


```
scala> val il3: LinkedListInt = LinkedListInt 
<console>:14: error: not found: value LinkedListInt
       val il3: LinkedListInt = LinkedListInt
                                ^
```								

Scala supports subtyping.  A concrete child can (and should) fill-in for the abstract parent:

```
scala> val il: LinkedListInt = ConsInt(4, ConsInt(5, NilInt))
il: com.datascience.education.tutorial.lecture4.Covariance.LinkedListInt = ConsInt(4,ConsInt(5,NilInt))

scala> val il2: LinkedListInt = NilInt 
il2: com.datascience.education.tutorial.lecture4.Covariance.LinkedListInt = NilInt
```

More subtly, we are applying `NilInt` to argument `t` of `ConsInt` in `ConsInt(5, NilInt)`.

`NilInt` is a subtype of `LinkedListInt`.  Because Scala supports subtying, `NilInt` is an acceptable fill-in for `LinkedListInt`.


### (4b): `case class` Misuse

A first attempt to add a generic type to our linked list:

```
  sealed trait LinkedList1[A]
  case class Cons1[A](h: A, t: LinkedList1[A]) 
	extends LinkedList1[A]
  case class Nil1[A]() extends LinkedList1[A]
```

Again, the concrete children successfully substitute for the type of the abstract parent:

```
scala> val l1: LinkedList1[Int] = Cons1(4, Cons1(5, Nil1()))
l1: com.datascience.education.tutorial.lecture4.Covariance.LinkedList1[Int] = Cons1(4,Cons1(5,Nil1()))

scala> val l2: LinkedList1[Int] = Nil1()
l2: com.datascience.education.tutorial.lecture4.Covariance.LinkedList1[Int] = Nil1()
```


Nevertheless, this implementation needs improvement.  A `case class` that takes no arguments is poor form, and should likely be replaced with a `case object`.  This implementation necessitates many instances of `Nil1` for each type of empty `LinkedList1`: an empty `LinkedList1[Double]`, an empty `LinkedList1[String]` and so on.

### (4c): Pitfall of Invariance

We want to justify and emulate the design choices of the Scala Collections with regards to [`Nil`, the empty Scala Collections `List`](http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.Nil$).  This is only a single `Nil` -- it is a `case object`.

We re-write `Nil1` into a `case object`.  A `case object` cannot provide a generic type, because a `case object` is a singleton.  There are not supposed to be many "versions" of a `case object` floating around, with different internal types.

```
  sealed trait LinkedList2[A]
  case class Cons2[A](h: A, t: LinkedList2[A]) 
	extends LinkedList2[A]
  case object Nil2 extends LinkedList2[Nothing]
```
We need a concrete type to fill in the generic type `A` where `Nil2` extends `LinkedList2`.

`Nothing` is a candidate for two reasons:

* It is a concrete type
* It is a subtype of every other type - but this reason jumps ahead to covariance


Let's try instantianting a few `LinkedList2`s in the same way that we instantiated `LinkedListInt` and `LinkedList1`:

```
scala> val l1: LinkedList2[Int] = Cons2(6, Cons2(7, Nil2))

<console>:14: error: type mismatch;
 found   : com.datascience.education.tutorial.lecture4.Covariance.Nil2.type
 required: com.datascience.education.tutorial.lecture4.Covariance.LinkedList2[Int]
Note: Nothing <: Int 
(and com.datascience.education.tutorial.lecture4.Covariance.Nil2.type <: com.datascience.education.tutorial.lecture4.Covariance.LinkedList2[Nothing]), 
but trait LinkedList2 is invariant in type A.
You may wish to define A as +A instead. (SLS 4.5)
       val l1: LinkedList2[Int] = Cons2(6, Cons2(7, Nil2))
                                                    ^
```
Let's boil down this compiler error.

We are trying to apply a `Nil2` as argument `t` in `Cons2[A](h: A, t: LinkedList2[A]) `.

`Nil2` is an instance of `LinkedList2[Nothing]`.

It is equivalent to say, then;

We are trying to apply a `LinkedList2[Nothing]` as argument `t` in `Cons2[A](h: A, t: LinkedList2[A]) `.

Because of *invariance*, there is *no relationship* between `LinkedList2[Nothing]` and `LinkedList2[A]`.  

`NilInt` is a subtype of `LinkedListInt`.  `NilInt` is an acceptable fill-in for `LinkedListInt`.

`LinkedList2[Nothing]` is *not* a subtype of `LinkedList2[A]`.  `LinkedList2[Nothing]` is *not* an acceptable fill-in for `LinkedList2[A]`.

The compiler suggests *covariance* as a solution.


Another example of the same problem:

```
scala> val l2: LinkedList2[Int] = Nil2
<console>:14: error: type mismatch;
 found   : com.datascience.education.tutorial.lecture4.Covariance.Nil2.type
 required: com.datascience.education.tutorial.lecture4.Covariance.LinkedList2[Int]
Note: Nothing <: Int 
(and com.datascience.education.tutorial.lecture4.Covariance.Nil2.type <: com.datascience.education.tutorial.lecture4.Covariance.LinkedList2[Nothing]), 
but trait LinkedList2 is invariant in type A.
You may wish to define A as +A instead. (SLS 4.5)
       val l2: LinkedList2[Int] = Nil2
                                  ^
```



These problems exhibit a [difference between inheritance and subtyping](https://en.wikipedia.org/wiki/Inheritance_(object-oriented_programming)#Inheritance_vs_subtyping).  


`Nil2` *inherits from* `LinkedList[A]`, but `Nil2` is not a proper subtype of `LinkedList[A]`.  In this situation, inheritance may still be useful for shared common methods, i.e. we could put some common code in the body of `trait LinkedList[A]` that `Nil2` would inherit:

```
  sealed trait LinkedList2[A] {
    // flaw here revealed later...
    def prepend(a: A): LinkedList2[A] = Cons2(a, this)
  }
  case class Cons2[A](h: A, t: LinkedList2[A]) 
	extends LinkedList2[A]
  case object Nil2 extends LinkedList2[Nothing]
```


### (4d): Covariance

We want `LinkedList2[Nothing]` to be a subtype of `LinkedList2[A]`.  A covariance annotation will provide this:

```
  sealed trait LinkedList3[+A]
  case class Cons3[+A](h: A, t: LinkedList3[A]) 
    extends LinkedList3[A]
  case object Nil3 extends LinkedList3[Nothing]
```

`LinkedList3[Nothing]` can fill in for `LinkedList3[A]`:

```
scala> val l1: LinkedList3[Int] = Cons3(6, Cons3(7, Nil3))
l1: com.datascience.education.tutorial.lecture4.Covariance.LinkedList3[Int] = Cons3(6,Cons3(7,Nil3))

scala> val l2: LinkedList3[Int] = Nil3
l2: com.datascience.education.tutorial.lecture4.Covariance.LinkedList3[Int] = Nil3
```

### (4e): Necessity of Type Bounds

Let's add some functionality to `LinkedList3`.

A `LinkedList3` with elements will apply `f` to each element `A`.

`Nil3` has nothing to "map" over, so returns itself.

`map` is a recursive function, and will encounter `Nil3` sooner or later.  Because `Nil3` is a subtype of `LinkedList3[A]`, this is okay.

```
  sealed trait LinkedList3[+A] {
    def map[B](f: A => B): LinkedList3[B] = this match {
      case Cons3(h, t) => Cons3(f(h), t.map(f))
      case Nil3 => Nil3
    }
  }
  case class Cons3[+A](h: A, t: LinkedList3[A]) 
    extends LinkedList3[A]
  case object Nil3 extends LinkedList3[Nothing]
```

```
scala> val l1: LinkedList3[Int] = Cons3(65, Cons3(66, Nil3))
l1: com.datascience.education.tutorial.lecture4.Covariance.LinkedList3[Int] = Cons3(65,Cons3(66,Nil3))

scala> l1.map(_.toChar)
res0: com.datascience.education.tutorial.lecture4.Covariance.LinkedList3[Char] = Cons3(A,Cons3(B,Nil3))
```


Other methods we could implement have problems, like `prepend`:

```
  sealed trait LinkedList3[+A] {
    def prepend(a: A): LinkedList3[A] = Cons3(a, this)
  }
  case class Cons3[+A](h: A, t: LinkedList3[A]) 
	extends LinkedList3[A]
  case object Nil3 extends LinkedList3[Nothing]
```

We have assumed that this will work:

```
val empty: LinkedList3[Nothing] = Nil3
val five: LinkedList3[Int] = empty.prepend(5)

```

But the type signature of `prepend` in `empty` is:

```
def prepend(a: Nothing): LinkedList3[Nothing] = Cons3(a, this)
```

`Int` is a supertype of `Nothing` and so cannot fill in the argument of `prepend`.

The compiler warns us:

```
covariant type A occurs in contravariant position in type A of value a
[error]     def prepend(a: A): LinkedList3[A] =
[error]                 ^
[error] one error found
[error] (tutorials/compile:compileIncremental) Compilation failed
[error] Total time: 1 s, completed Jun 21, 2016 3:12:05 PM

```

We need type bounds.  `prepend` must accept as an argument a *supertype* of `Nothing`.


```

  sealed trait LinkedList4[+A] {
    def map[B](f: A => B): LinkedList4[B] = this match {
      case Cons4(h, t) => Cons4(f(h), t.map(f))
      case Nil4 => Nil4
    }

    def prepend[B >: A](b: B): LinkedList4[B] =
      Cons4(b, this)

  }
  case class Cons4[+A](h: A, t: LinkedList4[A]) 
    extends LinkedList4[A]
  case object Nil4 extends LinkedList4[Nothing]
  
```

Now `LinkedList4[Nothing]` can be "upgraded" to `LinkedList4[Int]`.  `prepend` compiles:

```

scala> val empty: LinkedList4[Nothing] = Nil4
empty: com.datascience.education.tutorial.lecture4.Covariance.LinkedList4[Nothing] = Nil4

scala> empty.prepend(5)
res1: com.datascience.education.tutorial.lecture4.Covariance.LinkedList4[Int] = Cons4(5,Nil4)

```

More concisely:

```
scala> Nil4.prepend(5)
res0: com.datascience.education.tutorial.lecture4.Covariance.LinkedList4[Int] = Cons4(5,Nil4)
```


This Part has demonstrated that variance and type bounds often go hand-in-hand.

----------------------

## Part 5: Covariance and Type Bounds in `Option`; `Option` Combinators

`tutorial/src/test/scala/com/datascience/education/tutorial/lecture4/FlawedOptionSpec.scala`
`tutorial/src/main/scala/com/datascience/education/tutorial/lecture4/FPOption.scala`

<b>Part 4: Covariance and Type Bounds in `List`</b> iterated through several incorrect implementations of `List` to approximate the version in the Scala Collections.

In this Part, you will do the same for `Option` (`FPOption`, explained soon).


To eliminate any ambiguity between the Scala Collections `Option` and our implementation, the parts of implementation will be named:

* `FPOption`
* `FPSome`
* `FPNone`

The Scala Collections `Option` is a default import.  In a `.scala` file, this `Option`, `Some` and `None` can be eliminated from scope with the following:

```
import scala.{ Option => _ }
import scala.{ Some => _ }
import scala.{ None => _ }
```

We like to debug in the REPL, and this trick does not work in the REPL; the Scala Collections `Option` remains in scope:

```
scala> import scala.{ Option => _ }
import scala.{ Some => _ }
import scala.{ None => _ }

import scala.{Option=>_}

scala> import scala.{Some=>_}

scala> import scala.{None=>_}

scala> 
scala> :t Option 
Option.type
```

### Task (5a): Compilation errors of `FlawedOption`

Investigate the compilation errors for the given implementation of `FlawedOption`, and use these compilation errors as a clue for the solution to the next task.  

In `tutorial/src/test/scala/com/datascience/education/tutorial/lecture4/FlawedOptionSpec.scala`, a test is provided which checks that this implementation `shouldNot compile`. You should modify this test so that it will fail by changing it to `should compile`, and look at the error for failing when the test is ran. To run this specific test in `sbt`, use `testOnly com.datascience.education.tutorial.lecture4.FlawedOptionSpec`. Remember to change the tests back otherwise your tests will continue to fail.

### Task (5b): `map` and `getOrElse`

Uncomment `map` and `getOrElse` inside trait `FPOption`.  Use what you've learned in <b>Part 4</b> to fix the trait and the given methods: `map` and `getOrElse`.

Test your implementations of the combinators with the runnable class `FPOptionExamples5b`.


### Task (5c): `orElse`

Use methods already implemented to implement `orElse`.  

`orElse` could be called a *non-primitive* combinator.  A *primitive* combinator would not be implemented on top of other combinators.

Its signature needs to be fixed.

Test your implementation with runnable class `FPOptionExamples5c`

### Task (5d): `flatMap`

Again, use methods already implemented to implement `flatMap`.

```
  def flatMap[B](f: A => FPOption[B]): FPOption[B] = ???
```

In this case, `flatMap` is a non-primitive combinator.  In later chapters and in general, `flatMap` is a primitive combinator.

In contrast to Task 5b, there is *no enforced relationship* between types `A` and `B` in `flatMap`.

Test your implementation with the runnable class `FPOptionExamples5d`.

### Task (5e): `map2`

Implement `map2`, another non-primitive combinator.  

```
  def map2[B, C](opB: FPOption[B])(f: (A,B) => C): FPOption[C] = ???
```

Test your implementation with runnable class `FPOptionExamples5e`.

-------------------------

## Tests

Various tests have already been implemented in the following files:  
 
`tutorial/src/test/scala/com/datascience/education/tutorial/lecture4/SafeDivisionSpec.scala`  
`tutorial/src/test/scala/com/datascience/education/tutorial/lecture4/EmptySetSpec.scala`  
`tutorial/src/test/scala/com/datascience/education/tutorial/lecture4/EmployeesSpec.scala`  
`tutorial/src/test/scala/com/datascience/education/tutorial/lecture4/FlawedOptionSpec.scala`  
`tutorial/src/test/scala/com/datascience/education/tutorial/lecture4/FPOptionSpec.scala`  

Uncomment all of the tests that are commented out in these files and run them to check your implementations in this tutorial.

-------------------------
## Resources

[Lower Type Bounds; necessity for implementation of `List`](http://docs.scala-lang.org/tutorials/tour/lower-type-bounds.html)


[ScalaDoc for Scala Collections `Option`](http://www.scala-lang.org/api/current/index.html#scala.Option)

[ScalaDoc for `Nothing`](http://www.scala-lang.org/api/current/index.html#scala.Nothing)


[Cats Traverse typeclass](http://typelevel.org/cats/typeclasses/traverse.html)

[Herding Cats: Traverse](http://eed3si9n.com/herding-cats/Traverse.html)
