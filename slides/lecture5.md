
#Lecture 5: Laziness and Streams

---

In this course we will explore a series of methods for separating program description from program evaluation.
<br />
<br />
This line of inquiry leads ultimately to the `Free` monad in lecture 13.

---

In this lecture we will begin by writing *descriptions* for infinitely long processes, but only *evaluate* them for a finite length of time, or within some bound.
<br />
<br />
Laziness is a fundamental component of this.

---

#Example: Transposing a Computation

    !scala
    def foo(i: Int) = {println(i); i + 10}
    def bar(i: Int) = {println(i); i % 2 == 0}
    List(1,2,3).map(foo).filter(bar)
    //1
    //2
    //3
    //11
    //12
    //13
    //res0: List[Int] = List(12)

---

    !scala
    Stream(1,2,3).map(foo).filter(bar).force
    //1
    //11
    //2
    //12
    //3
    //13
    //res1: Stream[Int] = Stream(12)

---


#Eval Monad

`cats.Eval` is a monad that allows us to abstract over different models of evaluation.

It is similar to `fs2.Task` from the Functional Streams for Scala library (see [fs2](https://github.com/functional-streams-for-scala/fs2) and chapter 15 of FPS).

We typically hear of two such evaluation models: eager and lazy.


---

What do these terms mean?

Eager computations happen immediately, whereas lazy computations happen only upon access.

For example, Scala `val`s are eager, memoized definitions.

---

We can see this using a computation with a visible side-effect:

    !scala
    val x = { println("Computing x"); 1+1 }
    //Computing x
    //x: Int = 2
    x
    //res0: Int = 2
    x
    //res1: Int = 2

---

By contrast, `def`s are lazy and not memoized:

    !scala
    def y = { println("Computing y"); 1+1}
    //y: Int
    y
    //Computing y
    //res2: Int = 2
    y
    //Computing y
    //res3: Int = 2

---

Last but not least, lazy vals are lazy and memoized:

    !scala
    lazy val z = { println("Computing z"); 1+1}
    //z: Int = <lazy>
    z
    // Computing z
    //res4: Int = 2
    z
    //res5: Int = 2

---

`Eval` has three subtypes: `Eval.Now`, `Eval.Later`, and `Eval.Always`:

    !scala
    import cats.Eval
    val now = Eval.now({ println("foo"); 1+1})
    //foo
    //now: cats.Eval[Int] = Now(2)
    val later = Eval.later({ println("foo"); 1+1})
    //later: cats.Eval[Int] = cats.Later@24a2a9b8
    later.value
    //foo
    //res0: Int = 2
    later.value
    //res1: Int = 2
    val always = Eval.always({ println("foo"); 1+1})
    //always: cats.Eval[Int] = cats.Always@773adb25
    always.value
    //foo
    //res2: Int = 2
    always.value
    //foo
    //res3: Int = 2


---

The three behaviors are summarized below:

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;border-color:#ccc;margin:0px auto;}
.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#ccc;color:#333;background-color:#fff;}
.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#ccc;color:#333;background-color:#f0f0f0;}
.tg .tg-9hbo{font-weight:bold;vertical-align:top}
.tg .tg-yw4l{vertical-align:top}
</style>
<table class="tg">
 <tr>
   <th class="tg-9hbo"></th>
   <th class="tg-9hbo">Eager</th>
   <th class="tg-9hbo">Lazy</th>
 </tr>
 <tr>
   <td class="tg-9hbo">Memoized</td>
   <td class="tg-yw4l">`val, Eval.now`</th>
   <td class="tg-yw4l">`lazy val, Eval.later`</th>
 </tr>
 <tr>
   <td class="tg-9hbo">Not Memoized</td>
   <td class="tg-yw4l">-</th>   
   <td class="tg-yw4l">`def, Eval.always`</th>
 </tr>
</table>

---

`Eval`s `map` and `flatMap` methods add computations to a chain.

This is similar to the `map` and `flatMap` methods on `scala.concurrent.Future`, except that the computations aren’t run until we call value to obtain a result:

    !scala
    val greeting = Eval.always { println("Step 1")
      "Hello"
      }.map { str =>
        println("Step 2")
        str + " world"
      }
    //greeting: cats.Eval[String] = cats.Eval$$anon$8@411b9c5f
    greeting.value
    //Step 1
    //Step 2
    //res0: String = Hello world

---

`Eval` also supports for comprehensions:

    !scala
    val ans = for {
        a <- Eval.now { println("Calculating A") ; 40 }
        b <- Eval.now { println("Calculating B") ; 2 }
      } yield {
        println("Adding A and B"); a+b
      }
    //Calculating A
    //ans: cats.Eval[Int] = cats.Eval$$anon$8@f636c08

---

Note that, while the semantics of the originating `Eval` instances are maintained, mapping functions are always called lazily on demand:

    !scala
    ans.value
    //Calculating B
    //Adding A and B
    //res0: Int = 42
    ans.value
    //Calculating B
    //Adding A and B
    //res1: Int = 42

---

We can use `Eval`'s `memoize` method to memoize a chain of computations.

    !scala
    val saying = Eval.always { println("Step 1") ; "The cat" }
      .map { str => println("Step 2") ; str + " sat on" }.memoize
      .map { str => println("Step 3") ; str + " the mat" }
    //saying: cats.Eval[String] = cats.Eval$$anon$8@24c1a639

---

Calculations before the call to memoize are cached, whereas calculations after the call retain their original semantics:

    !scala
    saying.value
    //Step 1
    //Step 2
    //Step 3
    //res0: String = The cat sat on the mat
    saying.value
    //Step 3
    //res1: String = The cat sat on the mat

---

#Trampolining

One useful property of `Eval` is that its `map` and `flatMap` methods are trampolined.

This means we can nest calls to `map` and `flatMap` arbitrarily without consuming stack frames.

We’ll illustrate this by comparing `Eval` to `Option`.

---

The `loopM` method below creates a loop through a monad’s `flatMap`:

    !scala
    import cats.Monad
    import cats.syntax.flatMap._
    import scala.language.higherKinds
    def stackDepth: Int = Thread.currentThread.getStackTrace.length
    def loopM[M[_] : Monad](m: M[Int], count: Int): M[Int] = {
      println(s"Stack depth $stackDepth")
      count match {
        case 0 => m
        case n => m.flatMap { _ => loopM(m, n - 1) }
      }
    }


---

When we run `loopM` with an `Option` we can see the stack depth slowly increasing:

    !scala
    import cats.std.option._
    import cats.syntax.option._
    loopM(1.some, 3)
    //Stack depth 45
    //Stack depth 52
    //Stack depth 59
    //Stack depth 66
    //res0: Option[Int] = Some(1)

---

Now let’s see the same thing using `Eval`. The trampoline keeps the stack depth constant:

    !scala
    loopM(Eval.now(1), 3).value
    //Stack depth 45
    //Stack depth 49
    //Stack depth 49
    //Stack depth 49
    //res1: Int = 1

---

We can use `Eval` as a mechanism to prevent to prevent stack overflows when working on very large data structures.

However, we should bear in mind that trampolining is not free—it effectively avoids consuming stack by creating a chain of function calls on the heap.

There are still limits on how deeply we can nest computations, but they are bounded by the size of the heap rather than the stack.

---

# Thunks

A ["thunk"](https://en.wikipedia.org/wiki/Thunk#Functional_programming) uses a function to provide *laziness*:

	!scala
	val thunk: () => Int = () => 123

---

`expensive` will not be called until the thunk is called.

  	!scala
    def expensive: Int = 42 //cost incurred here
    //expensive: Int
    val thunk2: () => Long = () => expensive
    //thunk2: () => Long = <function0>
    thunk2
    //res0: () => Long = <function0>
    thunk2()
    //res1: Long = 42


---

The chosen syntax for a call-by-name argument was probably chosen for its resemblance to a thunk.

	!scala
	// Compare
	def bar(x: => Int)
	// to
	def baz(x: () => Int)


Note that Scala does not support call-by-name parameters in case classes.

---

#Streams

Recall the definition of a simple list:

	!scala
	sealed trait List[+A]
	case object Nil
	  extends List[Nothing]
	case class Cons[+A](head: A,
	                    tail: List[A])
	  extends List[A]

---

A `Stream` is nothing other than a lazily evaluated list:

	!scala
	trait Stream[+A]
	case object Empty extends Stream[Nothing]
	case class Cons[+A](h: () => A,
	                    t: () => Stream[A])
	extends Stream[A]

---

The most important difference between the two:

* the tail of `Cons` of `List` is *eager*
* the tail of `Cons` of `Stream` is *lazy*

Non-empty streams are also commonly used in comonadic computations (e.g. zippers). More on this later.  

---

Note the type has been inferred as `Cons[Int]` rather than `Stream[Int]`:

  !scala
  val s = Cons(() => 1, () => Cons(() => 2, () => Empty))
	//res0 = Cons(<function0>,<function0>)
  s.h
  //res1: () => Int = <function0>
  s.h()
  //res2: Int = 1

---

The `cons` smart constructor memoizes and hides the thunks as well as assisting with type inference.  

	!scala
  def cons[A](hd: => A, tl: => Stream[A]):
    Stream[A] = {
      lazy val head = hd
      lazy val tail = tl
      Cons(() => head, () => tail)
      }
  def empty[A]: Stream[A] = Empty

---

Note that the call-by-name syntax in the smart constructor removes need for thunks:

  !scala
  val s = cons(1, cons(2, empty))
  //s: Stream[Int] = Cons(<function0>,<function0>)


---
#[Tying the Knot](https://github.com/fpinscala/fpinscala/wiki/Chapter-5:-Strictness-and-laziness#tying-the-knot)

With strict evaluation, `fibonacciHelper` would be an endless loop and lead to stack overflow.

	!scala
def fibonacciHelper(a: Int, b: Int): Stream[Int] = Stream.cons(a, fibonacciHelper(b, a+b))
val fibonacci: Stream[Int] = fibonacciHelper(0, 1)
fibonacci.print(22)

<br />

output

	0, 1, 1, 2, 3, 5, 8, 13, 21, 34 ...

<br />

[Source](http://philwantsfish.github.io/scala/streamsandprimes)

.notes: It will still lead to stack overflow, but the lazy list gives us more control over this.  We still need to limit our consumption of the Fibonacci stream.  `print` will be explained later

---
# Challenge question

Use the `cons` smart constructor to implement a sine wave.  Implement the sine wave for multiples of π/3 radian, or multiples of π/2 radian.
<br />
<br />
Utilize `math.sqrt(Double): Double`
<br />
<br />


	!scala
	def sinePositive: Stream[Double] = ???


	def sineNegative: Stream[Double] =
      sinePositive.map { d => -1*d }

---
`sinePositive` corresponds to the top half of the unit circle, including `(1, 0)` and excluding `(-1, 0)`
![](images/lecture5/unit_circle.png)


---
# Answer

	!scala
	def sinePositive: Stream[Double] =
      Stream.cons(0,
        Stream.cons(1.0/2,
          Stream.cons(math.sqrt(3)/2,
            Stream.cons(1.0,
              Stream.cons(math.sqrt(3)/2,
                Stream.cons(1.0/2, sineNegative)
	  )))))

	def sineNegative: Stream[Double] =
      sinePositive.map { d => -1*d }

	sinePositive.print(32)

---

	0.0
	0.5
	0.8660254037844386
	1.0
	0.8660254037844386
	0.5
	-0.0
	-0.5
	-0.8660254037844386
	-1.0
	-0.8660254037844386
	-0.5
	0.0
	0.5
	...


---
# `from`

	!scala
	def from(i: Int): Stream[Int] =
	  cons(i, from(i + 1))

<br />
<br />
<br />
<br />

example

	scala> Stream.from(0).print(8)
	0
	1
	2
	3
	4
	5
	6
	7


---

# Challenge question

We can turn an infinite `Stream` into a finite `Stream` with `take.`  `take` is a method that exists inside the `Stream` trait.

`countFromZero.take(6)` will insert an `Empty` after the sixth element of the infinite `Stream`.

Complete the implementation of `take`:

	!scala
	trait Stream[+A] {
	  ...
      def take(n: Int): Stream[A] = this match {
	    case cons(head, lazyTail) if ??? => ???
		case cons(head, lazyTail) if ??? => ???
		case Empty => empty[A]
	  }
	  ...
	}
<br />
<br />

`print` relies upon `take`

---

---
# Answer

	!scala
	trait Stream[+A] {
	  ...
      def take(n: Int): Stream[A] = this match {
	    case cons(head, lazyTail) if n>0 =>
		  cons(h, lazyTail.take(n-1))
		case cons(head, lazyTail) if n<=0 =>
		  empty[A]
		case Empty =>
		  empty[A]
	  }
	  ...
	}

---
Trace

	from(0).take(4)
	cons(0, from(1).take(3))
	cons(0, cons(1, from(2).take(2)))
	cons(0, cons(1, cons(2, from(3).take(1))))
	cons(0, cons(1, cons(2, cons(3, from(4).take(0)))))
	cons(0, cons(1, cons(2, cons(3, Empty))))

.notes: Evaluation is explained later.  `take` is not evaluation.  If only two elements are evaluated, then this trace will be shortened.

---

#foldRight

`foldRight` on `List`

	!scala
	trait List[+A] {
	  ...
	  def foldRight[B](z: B)
	                  (f: (A, B) => B): B =
        as match {
          case Nil => z
          case Cons(a, tail) =>
		    f(a, tail.foldRight(z)(f))
        }
	  ...
    }

---


`foldRight` on `Stream`

	!scala
	trait Stream[+A] {
	  ...
      def foldRight[B](z: => B)
		              (f: (A, => B) => B): B =
	    this match {
		  case Empty => z
		  case cons(head, lazyTail) =>
		    f(head, lazyTail.foldRight(z)(f))
		}
	  ...
    }

.notes: Remind them lazy tail in Stream.cons; case class Cons[+C](h: () => C, t: () => Stream[C])

---


`foldRight` is not suitable to be used on an infinite `Stream`.

Furthermore, `foldRight` is not tail-recursive.
<br />
<br />
<br />
<br />

	!scala
	trait Stream[+A] {
	  ...
      def foldRight[B](z: => B)
		              (f: (A, => B) => B): B = ...
	  ...
    }

.notes: Point out which combinators on `Stream` are suitable to be used on an infinite `Stream`, and which are not

---

Limit an infinite `Stream` to a finite length with `take` before calling `foldRight`.  

Despite this precaution, a `Stream` of finite length [may still overflow the stack because of `foldRight`'s tail-calls.](https://groups.google.com/forum/#!topic/scala-functional/MKZ5olHHSwQ)

<br />
<br />

	!scala
    val summed =
	  Stream.from(0).take(100).foldRight(0)(_+_)

	println(s"sum of 0 to 99, inclusive = $summed")

	// sum of 0 to 99, inclusive = 4950


---
#map

	!scala
	trait Stream[+A] {
	  ...
	  def map[B](f: A => B): Stream[B] = {
        def g(a: A, sb: => Stream[B]): Stream[B] =
	      cons(f(a), sb)

        foldRight(empty[B])(g)
      }
	  ...
	}

---

	!scala
	val radians: Stream[Double] =
	  Stream.from(0).map { i =>
	                       i.toDouble * math.Pi / 3 }


	val cosineWave: Stream[Double] =
	  radians.map { d => math.cos(d) }

<br />
<br />
<br />
<br />
<br />
<br />


.notes: An implementation of `map` on an `Array` can process the elements in parallel because an `Array` provides O(1) access to any element.  An implementation of `map` on a `List` cannot process the elements in parallel because a `List` provides O(n) access to any element.  Mapping over a List occurs sequentially.  Understanding this, it is more intuitive how `map` on a `Stream` is sequential and lazy.  In this example, `66.toChar` preceed the lazy evaluation of `67.toChar`, and so on.  Anyone who thinks mapping over a `List` happens "all at once" will be confused by mapping over a Stream.

---

![](images/lecture5/sine_wave.png)



---
#flatMap

	!scala
	trait Stream[+A] {
	  ...
	  def append[B >: A](appended: Stream[B]):
	    Stream[B] = ...

	  def flatMap[B](f: A => Stream[B]):
	    Stream[B] = {
        def g(a: A, sb: => Stream[B]) =
          f(a).append(sb)

        foldRight(empty[B])(g)
      }
	  ...
    }

.notes: It is impossible for `map` to lengthen the Stream.  `flatMap` can do this.  We can interpolate more points in the sine wave with `flatMap`

---

	!scala
	// def flatMap[B](f: A => Stream[B]): Stream[B]

	val interpolatedSineWave: Stream[Double] =
	  sineWave.take(32).flatMap { d =>
        Stream.cons(d, Stream.cons(d, Stream.empty))
      }

<br />
<br />
<br />
<br />
<br />
<br />


in `slideCode.lecture5.SineWave`

---

![](images/lecture5/sine_wave_interpolated.png)


---
# Evaluation

Exiting the `Stream`

	!scala
	trait Stream[+A] {
	  ...
	  def print(upTo: Int): Unit = {
        def f(a: A, remaining: => Int): Int = {
          println(a)
          remaining - 1
        }
        this.take(upTo).foldRight(upTo)(f)
      }
      ...
	}


---

	!scala
	trait Stream[+A] {
	  ...
	  def force(n: Int): List[A] = {
        def f(a: A, la: => List[A]) = a::la

        this.take(n).foldRight(List[A]())(f)
      }
      ...
	}

---

	!scala
    def sum(s: Stream[Int], maxElements: Int): Int = {
      def f(i: Int, s: => Int) = i + s
      s.take(maxElements).foldRight(0)(f)
    }
<br />
<br />

Usage

	scala> from(4)
	res0: fpinscala.laziness.Stream[Int] =
	  Cons(<function0>,<function0>)

	scala> sum(from(4), 10)
	res1: Int = 85


---


#unfold

`unfold` helps to create `Stream`s

<br />
<br />

	!scala
	def unfold[A, S](z: S)(f: S => Option[(A, S)]):
	  Stream[A]

---

Many of the exercises can be re-implemented with `unfold` -- often more cleanly.
<br />
<br />

	!scala
	def from(n: Int): Stream[Int] =
      unfold(n)((n0: Int) => Some(n0, n0+1))

	from(5).print(6)
	// 5
	// 6
	// 7
	// 8
	// 9
	// 10

<br />
<br />

in `common.lecture5.StreamExamples`

---

	!scala
	def fromTo(lowerInclusive: Int,
	           upperExclusive: Int):Stream[Int]=
      unfold(lowerInclusive){(n0: Int) =>
        if (n0 < upperExclusive) Some(n0, n0+1)
        else None
      }

	fromTo(5,8).print(6)
	// 5
	// 6
	// 7
	// Stream ends before 6 elements can be printed

<br />

in `common.lecture5.StreamExamples`

---

#Homework

Read Chapter 6 of _Functional Programming in Scala_.
