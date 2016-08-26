
#Challenge question

The line between "description" and "evaluation" can be unclear.  Where do you think "evaluation" occurs here?

	!scala
	import scalaz.stream.io
	import scalaz.concurrent.Task

    val illiad = io.linesR("illiad.txt")

	val process: Process[Task, Unit] = 
	  illiad.take(512).to(io.stdOutLines)
	  
	val task: Task[Unit] = process.run
	
	task.run


Using it

	!scala
	
	val finiteList: List[Int] = Cons(1, Cons(2, Cons(3, Nil)))
	
Note that we need to assist type inference.  

Lacking the explicit type annotation on `finiteList`, Scala has inferred its type as `Cons` rather than `List`.

	!scala
	scala> val finiteList = Cons(1, Cons(2, Cons(3, Nil)))
	finiteList: fpinscala.datastructures.Cons[Int] = Cons(1,Cons(2,Cons(3,Nil)))


This fixes it

	!scala
	scala> val finiteList: List[Int] = Cons(1, Cons(2, Cons(3, Nil)))
	finiteList: fpinscala.datastructures.List[Int] = Cons(1,Cons(2,Cons(3,Nil)))

	
Since `=>` does not memoize, lets work around that:

	!scala
	def fooCCMemoized(x: => Int, y: => Int): FooCC = {
      lazy val memoX = x
      lazy val memoY = y

      def genX() = memoX
      def genY() = memoY

      FooCC(genX, genY)
	}
	
I think it is counter-intuitive that `memoX` and `memoY`, sitting outside of the `FooCC` case class, would be storage for `FooCC`.


eager, memoizes

An example where the cost is much higher -- the Ackermann function:

![](ackermann.png)

The cost of time and memory in calculating the Ackermann function for 4 and 3 is paid on the first line.  This cost will only be paid once, ever, for any use of `ack43`.

	!scala
	val ack43 = Ackermann(4,3)
	println(ack43)
	
	
The Ackermann function is used by Runar Bjarnason to [demonstrate the benefits of trampolining](http://blog.higher-order.com/blog/2015/06/18/easy-performance-wins-with-scalaz/).

[Trampolining](https://en.wikipedia.org/wiki/Continuation-passing_style) is a topic of chapter 13.	
---
# Case Classes

Disallowed:

	!scala
	case class FooCC(x: => Int, y: => Int)

`'val' parameters may not be call-by-name`

Disallowed (and `val` is redundant?):

	!scala
	case class FooCC(lazy val x: Int, lazy val y: Int)


`lazy modifier not allowed here. Use call-by-name parameters instead`

These past few slides fall under the topic of [lazy constructors](https://www.google.com/search?q=scala+lazy+constructor&ie=utf-8&oe=utf-8)


---

Ugly work-around to lack of support for `call-by-name` parameters in `case class`.

	!scala
	case class FooCC(x: () => Int, y: () => Int) {
      lazy val z = x() + y()
	}

	def genSix() = 6
	def genFour() = 4
	val fooTen = FooCC(genSix, genFour)

	println("fooTen.z: " + fooTen.z)

A ["thunk"](https://en.wikipedia.org/wiki/Thunk#Functional_programming)*:

	!scala
	val thunk: () => Int = () => 123
	
\* *Wikipedia* says that a "thunk" in Haskell memoizes.  This is not true in Scala.
	
---

That ugly work-around can be hidden by a *"smart constructor"*

	!scala
	def fooCC(x: => Int, y: => Int): FooCC = {
      def genX() = x
      def genY() = y
      FooCC(genX, genY)
	}
	
	val fooTenSmartConstructed: FooCC = fooCC(4, 6)


---

`lazy val` here does not work as intended.  Both of these calls on `thunk` are costly.  
Ackermann(3, 9) is computed twice.

	!scala
	lazy val thunk = () => ack(3, 9)

	println("thunk")
	println(thunk())
	// 4093

	println("thunk again:")
	println(thunk())
	// 4093
	
This example justifies the next slide, and Listing 5.2

---
# => used in a class constructor

	!scala
	class Foo(x: => Int, y: => Int) {
	  val z = x + y // x and y evaluated here, eagerly
	}

	class Bar(x: => Int, y: => Int) {
      def z = x + y // x and y evaluated when z is evaluated; not memoized
	}

	class Baz(x: => Int, y: => Int) {
      lazy val z = x + y // x and y evaluated when z is evaluated; memoized
	}


---

---

#Scalaz Stream example

`illiad` is a source

	!scala
	import scalaz.stream.io
	import scalaz.concurrent.Task

    val illiad: Process[Task, String] = 
	  io.linesR("illiad.txt")

---

`io.stdOutLines` is a sink -- printing to console

The source is limited to 512 lines of the *Illiad*, and then drained into the sink.

	!scala
	val process: Process[Task, Unit] = 
	  illiad.take(512).to(io.stdOutLines)

---

The `Process` is compiled into a `Task`.

	!scala
	val task: Task[Unit] = process.run

<br />
<br />

The `Task` is run to produce `Unit`.

	!scala
	task.run: Unit
	

As it was on `List`, `foldRight` is still not tail-recursive.

`foldRight`'s potential to StackOverflow is now limited by its laziness.

`foldRight` will *not* continue to iterate unless its evaluation demands it.

`foldRight` can be a "description" if its output type is `Stream`.  `foldRight` can be an "evaluator" if its output type is not `Stream`.

`take` should be considered a "description".  `take` also has the ability to prevent stack overflow -- `take` can *end the stream*.  So there is a balance of power between description and evaluation.

Function `f` provided as an argument to `foldRight` can be either a "description" or "evaluation" -- depending on if its output type is `Stream`.  Like `take`, `f` has the ability to *end the stream*.



----


Call-by-name

	!scala
	def quicksort(list: => List[Int]): List[Int] = 
	  list match {
        case Nil => Nil
        case head::tail =>
          quicksort(tail.filter(_ < head)) :::
          List(head) :::
          quicksort(tail.filter(_ >= head))
      }

<br />
<br />
<br />
<br />

in `slideCode.lecture5.LazyArguments`

[Why is lazy evaluation useful?](http://stackoverflow.com/a/284180/1007926)

.notes: Arguments to a function are eager/strict/call-by-value by default in Scala

---

`quicksort` with a call-by-name argument does the minimum work to produce the smallest number of the input

	!scala
	val unsorted = List(4,6,3,1,8)

	val least: Option[Int] = 
	  quicksort(unsorted).headOption

	println(least)
	// Some(1)


Note that we need to assist type inference.  

Lacking the explicit type annotation on `finiteList`, Scala has inferred its type as `Cons` rather than `List`.

	scala> val finiteList = 
	  Cons(1, Cons(2, Cons(3, Nil)))
	finiteList: fpinscala.datastructures.Cons[Int] =
	  Cons(1,Cons(2,Cons(3,Nil)))

<br />
<br />
<br />
<br />

Explicitly specifying the type of `finiteList` fixes the issue

	scala> val finiteList: List[Int] = 
	  Cons(1, Cons(2, Cons(3, Nil)))
	finiteList: fpinscala.datastructures.List[Int] =
	  Cons(1,Cons(2,Cons(3,Nil)))

	
	
---
Like our earlier example with "thunks," we will use a *smart constructor* to 

* provide memoization
* hide the thunks
* assist with type inference
     * preventing problems like when type `Cons` was inferred rather than type `List`
	 
---
*Exiting* the `Stream` type is *evaluation*.

An *evaluator* calls the lazy values in the `Stream`.  The output of the *evaluator* is no longer lazy.

`printer` is the clearest example.  We are evaluating lazy elements in the `Stream`, and producing `Unit` -- text on the console.

`forAll(p: A => Boolean): Boolean` is a method on `Stream[A]` that checks each elements of the `Stream` satisfies the predicate.  It returns Boolean.  Another example of a function that leaves the `Stream` type entirely.

Like other evaluators, calling `forAll` on an infinite `Stream` can easily lead to stack overflow.

.notes: *Exiting* the `Stream` type is *evaluation*.

---

A similar method `forAll2(p: A => Boolean): Stream[A]` could exist -- a sort of "streaming" predicate check.  

`forAll2` passes through its input `Stream` -- until `p` fails.

This would *not* be considered an evaluator.  `forAll2` is lazy.

---

Note the absence of `foldLeft` anywhere in this chapter.

`foldLeft` on `List` reversed its input.  A pass-thru implemented with `foldLeft` required reversal of the output.

How can we depend on "reversal" when we are folding over a potentially infinitely long `Stream`?



It is easier to imagine the termination of a finite `Stream`, than an infinite `Stream` like `countFromZero`.  A perfectly valid, finite `Stream`:

	!scala
	val countToThree: Stream[Int] = 
	  cons(1, cons(2, cons(3, empty[Int])))
	  
It barely resembles something `lazy`, but this compiles and runs as a `Stream[Int]`.


---
---
	!scala
	val ones: Stream[Int] = cons(1, ones)

Note the absence of `foldLeft` anywhere in this chapter.

`foldLeft` on `List` reversed its input.  A pass-thru implemented with `foldLeft` required reversal of the output.

How can we depend on "reversal" when we are folding over a potentially infinitely long `Stream`?

---

Provided this clue, implement `from`, a infinite `Stream` of incrementing numbers

	!scala
	val fromZero: Stream[Int] = from(0)
	
	def from(i: Int): Stream[Int] = ???

Use the `cons` smart constructor

	!scala
	
	def cons[A](hd: => A, tl: => Stream[A]): Stream[A]

---
# Answer

	!scala
	val fromZero: Stream[Int] = from(0)
	
	def from(i: Int): Stream[Int] = cons(i, from(i+1))

---

Alternative solution, reduced to the minimum; 

no smart constructor, no memoization.

	!scala
	val fromZero: Stream[Int] = from(0)
	
	def from(i: Int): Stream[Int] = Cons(() => i, () => from(i+1))
	
.notes: Not using the smart constructor can help to emphasize that the "tail" of `Cons` is *lazy*.  It is not evaluated until it is needed.



# show don't tell, too -- stack trace
#countToThree: Stream[Int]


`countToThree.take(6)` provides an insight into the important of laziness, here.

We are accustomed to writing error-handling, or edge-case handling, logic for situations like this.

"If I hold only three items, and the caller is asking for six items, stop at three items."

Here, there is no such logic like this.

Only three of six possible calls to `take` are evaluated, on `countToThree`:

* `take(6)`
* `take(5)`
* `take(4)`

---

#countToThree: Stream[Int]

Lazy evaluation means the last three calls to `take` are never made -- nor are these last three calls even "set up."

	!scala
    def take(n: Int): Stream[A] = this match {
  	  case cons(head, lazyTail) if n>0 => 
    	cons(h, lazyTail.take(n-1))
      case cons(head, lazyTail) if n<=0 => 
    	empty[A]
      case Empty => 
    	empty[A]
	}

`take(3)`, `take(2)` and `take(1)` *would have* been called in the third line of this pattern match, but the pattern match chose a different case and *changed course*.
