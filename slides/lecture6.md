
#Lecture 6: Purely Functional State

<img src="images/lecture6/von-neumann.jpg"  height="300" >
<br>
<br>

"Anyone who attempts to generate random numbers by deterministic means is, of course, living in a state of sin."

-- [John von Neumann](https://en.wikipedia.org/wiki/John_von_Neumann)

---

Just as a mathematical function always calculates the same output for a given input, so does a *referentially transparent* function in functional programming.
<br />
In this lecture we will build an API for pseudo-random number generators that obeys this rule.
<br />
Furthermore, the combinators in our API will prevent the re-generation of the same "random" number.  This particular error will not be a concern of our library's user.

---

We start with a [linear congruential generator](https://en.wikipedia.org/wiki/Linear_congruential_generator):
<br />

	!scala
    def generateSeed(seed: Long): Long =
      (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL

    def generateInt(seed: Long): Int =
      (seed >>> 16).toInt

<br />
<br />
<br />
<br />
<br />
<br />
Defined in `slideCode.lecture6.PseudoRandomNumberGenerator`


---

`RNG` generates a "random" Integer, and another `RNG`, in a tuple.  RNG => (Int, RNG)
<br />

	!scala
    case class RNG(seed0: Long) {
      def nextInt: (Int, RNG) = {
        val seed1: Long = generateSeed(seed0)
        val int1: Int = generateInt(seed1)
        val rng1: RNG = RNG(seed1)
        (int1, rng1)
      }
    }

<br />
<br />
<br />
<br />
Defined in `lectureCode.lecture6.PseudoRandomNumberGenerator`

.notes: `RNG` generates a "random" Integer, and another `RNG`, in a tuple.  RNG => (Int, RNG)

---

	val simple = RNG(123)
	val tup: (Int, RNG) = simple.nextInt
	println(tup)
	// (47324114,RNG(3101433181802))



---

#Exercise

What does `iterate` do?

	!scala
	val rng0 = RNG(123)

	def iterate(iterations: Int)(rng0: RNG):
	  (Int, RNG) =
      (0 to (iterations - 1)).foldLeft((0, rng0)) {
      case ((randIntI: Int, rngI: RNG), iter: Int) =>
        println(s".. $iter .. $randIntI .. $rngI")
        rngI.nextInt
      }

	println(iterate(32)(rng0))


<br />
<br />
<br />

Defined in `slideCode.lecture6.RNGPrimitiveExamples`

---

	[info] Running slideCode.lecture6.
	         RNGPrimitiveExamples
	iter: 0 randInt: 0 rngI: RNG(123)
	iter: 1 randInt: 47324114 rngI: RNG(31014..802)
	iter: 2 randInt: -386449838 rngI: RNG(25614..869)
	iter: 3 randInt: 806037626 rngI: RNG(52824..4818)
	iter: 4 randInt: -1537559018 rngI: RNG(180..38287)
	...
	iter: 28 randInt: 1770503168 rngI: RNG(11316..223)
	iter: 29 randInt: 265482177 rngI: RNG(1736..462)
	iter: 30 randInt: -1627823703 rngI: RNG(179..50073)
	iter: 31 randInt: -683498645 rngI: RNG(236..41456)
	(-1680880615,RNG(171316784789787))




---

We can begin to manipulate `RNG` to generate different random types:

	!scala
	def nonNegativeInt(rng: RNG): (Int, RNG) = {
		val (i, r) = rng.nextInt
		(if (i < 0) -(i + 1) else i, r)
	}
	//Uniform on [0,1]
	def double(rng: RNG): (Double, RNG) = {
		val (i, r) = nonNegativeInt(rng)
		(i / (Int.MaxValue.toDouble + 1), r)
	}

---

... or pairs of types.

	!scala
	def intDouble(rng: RNG): ((Int, Double), RNG) = {
		val (i, r1) = rng.nextInt
		val (d, r2) = double(r1)
		((i, d), r2)
	}

However this is tedious and error-prone.

---

#Exercise

What common pattern is shared between these type signatures?

	!scala
	def iterate(iters: Int)(rng0: RNG): (Int, RNG) = ???
	def nonNegativeInt(rng: RNG): (Int, RNG) = ???
	def double(rng: RNG): (Double, RNG) = ???
	def intDouble(rng: RNG): ((Int, Double), RNG) = ???


---

---

We factor this commonality out into a type:

	!scala
	type Rand[A] = RNG => (A, RNG)

<br>
This means we can create a `Rand[Int]` directly from an `RNG` for example:

	!scala
	val int: Rand[Int] = _.nextInt

---

# Combinators on `Rand`

Note that `Rand` is a type, not a class or trait, so we define our combinators in a companion object.

	!scala
	object Rand {
	  // primitive
	  def unit[A](a: A): Rand[A] = { ... }
	  def flatMap[A, B](ra: Rand[A])(g: A => Rand[B]):
	    Rand[B] = { ... }

	  // derived  
	  def map[A,B](ra: Rand[A])(f: A => B):
	  Rand[B] = ???
	  def map2[A,B,C](ra: Rand[A], rb: Rand[B])
	  (f: (A, B) => C): Rand[C] = ???
	  def sequence[A](fs: List[Rand[A]]):
	  Rand[List[A]] = ???
	}

---

#Exercise

How would you implement `map` for `Rand[A]`?
<br>

Given `int`, re-implement `double` using `map`. Ignore the edge case.

---

---

	!scala
	def map[A,B](ra: Rand[A])(f: A => B): Rand[B] =
		rng => {
			val (a, rng1) = ra(rng0)
			(f(a), rng1)
		}

---


---

	!scala
	def double: Rand[Double] =
	  map(int) {
	    (i: Int) => i.toDouble / Int.MaxValue
	  }

Note that no explicit `RNG` value is necessary anywhere in this implementation of `double`.

---

	!scala
	def flatMap[A, B](ra: Rand[A])(g: A => Rand[B]):
	   Rand[B] =
	   rng0 => {
	      val (a, rng1) = ra(rng0)
	      g(a)(rng1) //pass the new state along
	    }


---

	!scala
	def map2[A, B, C](ra: Rand[A], rb: Rand[B])
	  (f: (A, B) => C): Rand[C] =
      flatMap(ra) { a =>
        map(rb) { b => f(a, b) }
      }

Note that `map2` is a *non-primitive* combinator so we don't have to handle any `RNG` values explicitly.

---

# Passing `RNG` implicitly

`Rand` passes the `RNG` values for us.

	!scala
	val simple = RNG(123)
	def double: Rand[Double] = { ... }
	def both[A,B](ra: Rand[A], rb: Rand[B]): Rand[(A,B)] =
		map2(ra, rb)((_, _))

	println(map2(double, double)(addDoubles)(simple))
	// ((0.022037,-0.179954),RNG(256148600186669))


If `RNG` were not passed through `map2` correctly, `x` and `y` would be the same value.

---

#Exercise

How many steps are there from `RNG(123)` to `RNG(256148600186669)`?

---

---

	scala> val a = RNG(123)
	a: RNG = RNG(123)
	scala> a.nextInt
	res0: (Int, RNG) = (47324114,RNG(3101433181802))
	scala> res0._2.nextInt
	res1: (Int, RNG) = (-386449838,RNG(256148600186669))

---

#Exercise

What does `unit` do?

	!scala
	def unit[A](a: A): Rand[A] =
      rng => (a, rng)

---


---

	scala> val u = unit(1)
	u: Rand[Int] = <function1>
	scala> u(a)
	res2: (Int, RNG) = (1,RNG(123))

<br>
What is the use of a random number generator that always returns the same number?

---

---

This implementation of `map` makes it a *non-primitive* combinator.

	!scala
	def map[A, B](ra: Rand[A])(f: A => B): Rand[B] =
      flatMap(ra)((a: A) => unit(f(a)))

`unit` is one of the primitive combinators required for the Monad typeclass.

---

	!scala
	def flatMap[A, B](ra: Rand[A])(g: A => Rand[B]):
		Rand[B] =
	    rng => {
	    	val (a, rng1) = ra(rng0)
	    	g(a)(rng1)
	    }
	def map[A,B](ra: Rand[A])(g: A => B):
	    Rand[B] =
	    rng => {
			val (a, rng1) = ra(rng0)
			(g(a), rng1)
	    }	    

---

Because `flatMap` can exit its context it can be used to implement recursive methods such as [rejection sampling](https://en.wikipedia.org/wiki/Rejection_sampling):

	!scala
	def rejectionSampler[A](ra: Rand[A])
		(p: A => Boolean): Rand[A] =
		flatMap(ra) { a =>
		  if (p(a)) unit(a)
		  else rejectionSampler(ra)(p)
		}

---

	scala> rejectionSampler(int)(_ % 5 ==0)(a)
	res3: (Int, RNG) = (936386220,RNG(61367007330318))

---

Recall that "nesting" is synonymous with `flatMap`, i.e. nested `Rand`s are `flatMapped` together.

We will make use of `rejectionSampler` extensively in Friday's lab.

---

	!scala
	def sequence[A](fs: List[Rand[A]]): Rand[List[A]] =
		fs.foldRight(unit(List[A]()))
			((f, acc) => map2(f, acc)(_ :: _))


---

	scala> sequence(List(int))(a)
	res4: (List(47324114),RNG(3101433181802))
	scala> sequence(List(int,int))(a)
	res5: (List(47324114, -386449838),RNG(256148600186669))


---

# Next steps

`Rand` generalizes to the `State` monad

With `Rand`, we transformed the transitions between `RNG`s.
<br />
With `State`, we will transform the transitions between generic states
<br />

Today's "state" was a `RNG`.

	!scala
	type Rand[A] = RNG => (A, RNG)

	type State[S,A] = S => (A, S)

---

The meaning of `join` for example in the state monad is to give the outer action an opportunity to get and put the state, then do the same for the inner action, making sure any subsequent actions see the changes made by previous ones.

    !scala
    case class State[S,A](run: S => (A, S))
    def join[S,A](v1: State[S,State[S,A]]): State[S,A] =
      State(s1 => {
        val (v2, s2) = v1.run(s1)
        v2.run(s2)
      })

---

#Homework

Have a look at `State` in [*Cats*](https://github.com/typelevel/cats).
