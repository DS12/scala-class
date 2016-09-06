
#Lecture 7: Purely Functional Parallelism

---

By now, we have implemented `unit`, `flatMap`, `map`, `map2` and other combinators on each of these:

	!scala
	trait List[+A] // chapter 3

	trait Option[+A] // chapter 4

	trait Stream[+A]  // chapter 5

	type Rand[A] = RNG => (A, RNG)  // chapter 6


Implementing combinators on type `Rand` rather than a trait was a significant leap!

---

Chapter 6 demonstrates *both* styles (trait and type), and why the latter style is necessary.

<br />
<br />

We will develop several versions of a parallelism library, and demonstrate why each version is an improvement on the one prior.

<br />
<br />

Chapter 6 demonstrates not only how to *use* a functional parallelism library, but how to to design one.


---

# An early version of `Par`

A trait for "containing" a parallel computation which produces `A`:

	!scala
	trait Par[+A] {
	 ???
	}

	object Par {
	  // entering the container
	  def unit[A](a: => A): Par[A] = ???
	  // exiting the container
	  def get[A](parA: Par[A]): A = ???
    }

---

	!scala
	// listing 7.2
	def sum(ints: IndexedSeq[Int]): Int =
	  if (ints.size <= 1)
	    ints headOption getOrElse 0
	  else {
	    val (l: IndexedSeq[Int], r: IndexedSeq[Int]) =
		  ints.splitAt(ints.length/2)
	    val sumL: Par[Int] = Par.unit(sum(l))
		val sumR: Par[Int] = Par.unit(sum(r))
		val leftPlusRight: Int =
		  Par.get(sumL) + Par.get(sumR)
	    leftPlusRight
      }

---

We need to make design decisions about `unit` and `get`

	!scala
	// entering the container
	def unit[A](a: => A): Par[A] = ???
	// exiting the container
	def get[A](parA: Par[A]): A = ???


We will return to `sum` momentarily.

---

# Non-blocking

A non-blocking function call *does not stop* the progression of the program from top to bottom.  

Even if `expensiveNonblocking` does a lot of work, these three calls are made in quick succession because there is no *wait* for `expensiveNonblocking` to return.

	!scala
	def expensiveNonblocking(f: Foo): Unit = ???

	expensiveNonblocking(foo1)
	expensiveNonblocking(foo2)
	expensiveNonblocking(foo3)

Registering a call-back is a great example of a non-blocking function call.



---

# Separation of description from evaluation

<br />
<br />

In the context of our parallelism library, *description* means:

* when the computation is performed
* on which thread the computation is performed

Some of this description will be "locked in" by our parallelism library.

Other parts of this description will be decided by the user of our parallelism library.  

We will strike a balance.

---
# Design decisions about `get` and `unit`

	!scala
	val sumL: Par[Int] = Par.unit(sum(l))
	val sumR: Par[Int] = Par.unit(sum(r))
	val leftPlusRight: Int =
	  Par.get(sumL) + Par.get(sumR)

If `unit` is non-blocking and begins work:

* `sumL` and `sumR` will be called in quick succession -- good
* Some of the work of the left job and the right job will *already* be completed when we arrive at `leftPlusRight`

---
# Design decisions about `get` and `unit`

	!scala
	val sumL: Par[Int] = Par.unit(sum(l))
	val sumR: Par[Int] = Par.unit(sum(r))
	val leftPlusRight: Int =
	  Par.get(sumL) + Par.get(sumR)

If `get` is blocking and begins work:

* `Par.get(sumL)` will begin and finish its work *before* `Par.get(sumR)` is called -- no parallelism

---

`sum` exposes the first weakness of our primitive parallelism library --

*`get` has to block.*

There is no other way than blocking to return a hard value `A`.

	def get[A](parA: Par[A]): A = ???

---
# `get`, `unit` and `Par` must change

We did not waste any time on the internal implementations of `get` and `unit` before deciding their external signatures must change.  This is good design practice.

<br />
<br />

Our new version of `get` is *non-blocking*, by virtue of a Java `Future`.  

We can retrieve a `Future[A]` without blocking.

	!scala
	def get[A](parA: Par[A]): Future[A] = ???

---

# `get`, `unit` and `Par` must change

Other parts must change to suite Java `Future`.

We need an `ExecutorService` to produce a `Future`.

	!scala
	type Par[A] = ExecutorService => Future[A]

<br />
<br />

It is analogous to needing an `RNG` to produce a random value.

	!scala
	type Rand[A] = RNG => (A, RNG)

---

	!scala
	type Par[A] = ExecutorService => Future[A]

	object Par {
	  // entering the container
	  def unit[A](a: => A): Par[A] = ???
	  // exiting the container
	  def get[A](parA: Par[A]): Future[A] = ???
    }

---

# `fork`

Earlier we said we would "strike a balance" between library control of parallelism and library user control of parallelism.

`fork` is a powerful method for the library's user.

`fork` places a job `parA` on another thread.

	!scala
	type Par[A] = ExecutorService => Future[A]

	def fork[A](parA: => Par[A]): Par[A] =
      executorService =>
	    executorService.submit(new Callable[A] {
	      def call = parA(executorService).get
	    })

Note that `fork` matches up with type `Par`.		

---

# Implementing more of the usual combinators

	!scala
	type Par[A] = ExecutorService => Future[A]

	object Par {
	  ...
	  def map2[A,B,C](parA: Par[A], parB: Par[B])
	    (f: (A,B) => C): Par[C] =



---

# scratch

In Lectures X, 4, 4a, and 5, we implemented combinators on traits:
In Lecture 6: "Random" Number Generators, we implemented combinators on a type:

	!scala

---

#Homework 

Read Chapter 8 of _Functional Programming in Scala_.
