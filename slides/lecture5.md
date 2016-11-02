
#Lecture 5: Laziness and Streams

---

In this course we will explore a series of methods for separating program description from program evaluation.
<br />
<br />
This line of inquiry leads ultimately to the `Free` monad in lecture 13.

---


#Eval Monad

`cats.Eval` is a monad that allows us to abstract over different models of evaluation.

It is somewhat similar to `scala.concurrent.Future` from the standard library and `fs2.Task` from the [Functional Streams for Scala library](https://github.com/functional-streams-for-scala/fs2).

---

We typically hear of two such evaluation models: eager and lazy.

Eager computations happen immediately, whereas lazy computations happen only upon access.

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

---

    !scala
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

`Eval`s `map` and `flatMap` methods add computations to a chain:

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


#Streams

Recall the definition of a simple list:

	!scala
	sealed trait List[+A] //base trait
	case object Nil extends List[Nothing]
	case class Cons[+A](head: A,
	                    tail: List[A])
	  extends List[A]

---

A `Stream` is nothing other than a lazily evaluated list:

	!scala
	trait Stream[+A] //base trait
	case object Empty extends Stream[Nothing]
	case class Cons[+A](h: () => A,
	                    t: () => Stream[A])
	  extends Stream[A]

---

The most important difference between the two:

* the tail of `Cons` of `List` is *eager*
* the tail of `Cons` of `Stream` is *lazy*

Like lists, streams are monadic. Non-empty streams (e.g. zippers) are also commonly used comonads. More on this later.  

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

The smart constructor memoizes and hides the thunks as well as assisting with type inference:

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

# Exercise

Use the `cons` smart constructor to implement a sine wave sampled at multiples of π/3 radians.

---


---

Here's one approach. Create the top half of the circle 'by hand', then use it to create the bottom half :

	!scala
	def sinePos: Stream[Double] = ???
	def sineNeg: Stream[Double] = sinePos.map { d => -1*d }

---

	!scala
	def sine: Stream[Double] =
      Stream.cons(0,
        Stream.cons(1.0/2,
          Stream.cons(math.sqrt(3)/2,
            Stream.cons(1.0,
              Stream.cons(math.sqrt(3)/2,
                Stream.cons(1.0/2, sineNeg)
	  )))))
	sine.take(32)

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

# Exercise

We can turn an infinite `Stream` into a finite `Stream` with `take`.  

`foo.take(6)` will insert an `Empty` after the sixth element of `foo`.

  	!scala
  	trait Stream[+A] {
      def take(n: Int): Stream[A] = this match {
  	    case cons(head, lazyTail) if ??? => ???
  		  case cons(head, lazyTail) if ??? => ???
  		  case Empty => empty[A]
  	  }
  	}


---

---

  	!scala
  	trait Stream[+A] {
      def take(n: Int): Stream[A] = this match {
  	    case cons(head, lazyTail) if n>0 =>
  		    cons(head, lazyTail.take(n-1))
  		  case cons(head, lazyTail) if n<=0 => empty[A]
  		  case Empty => empty[A]
  	  }
  	}

---
#Example: `from`

  	!scala
  	def from(i: Int): Stream[Int] = cons(i, from(i + 1))
    Stream.from(0).take(4)
    //???


---

    from(0).take(4)
    cons(0, from(1).take(3))
    cons(0, cons(1, from(2).take(2)))
    cons(0, cons(1, cons(2, from(3).take(1))))
    cons(0, cons(1, cons(2, cons(3, from(4).take(0)))))
    cons(0, cons(1, cons(2, cons(3, Empty))))


---

#Example: Transposition


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
    Stream(1,2,3).map(foo).filter(bar).take(1)
    //1
    //11
    //2
    //12
    //3
    //13
    //res1: Stream[Int] = Stream(12)


---

#foldRight

Recall from our folds tutorial that `foldRight` on a list looks like this:

  	!scala
  	trait List[+A] {
  	  def foldRight[B](z: B)
  	    (f: (A, B) => B): B =
        this match {
          case Nil => z
          case Cons(a, tail) =>
		        f(a, tail.foldRight(z)(f))
        }
      }

---

We can use the same pattern to create an (unsafe) `foldRight` for our `Stream`:

  	!scala
  	trait Stream[+A] {
      def foldRight[B](z: => B)
  		  (f: (A, => B) => B): B =
  	    this match {
  		    case Empty => z
  		    case cons(head, lazyTail) =>
  		      f(head, lazyTail.foldRight(z)(f))
  	 	  }
    }

---

Here is the stack-safe trampolined version from [Cats](https://github.com/typelevel/cats/blob/master/core/src/main/scala/cats/instances/stream.scala):

    !scala
    def foldRight[A, B](lb: Eval[B])
      (f: (A, Eval[B]) => Eval[B]): Eval[B] =
      Now(this).flatMap { s =>
        if (s.isEmpty) lb
        else f(s.head, Eval.defer(s.tail.foldRight(lb)(f)))
      }

Note that we don't use pattern matching to deconstruct the stream, since that would needlessly force evaluation of the tail.

---

#Homework

Read Chapter 6 of _Functional Programming in Scala_.
