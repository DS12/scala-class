
#IO Monad

---

#Links

case class IO[A](unsafePerformIO: () => A) {
  def map[B](ab: A => B): IO[B] = IO(() => ab(unsafePerformIO()))
  def flatMap[B](afb: A => IO[B]): IO[B] =IO(() => afb(unsafePerformIO()).unsafePerformIO())
  def tryIO(ta: Throwable => A): IO[A] =
    IO(() => IO.tryIO(unsafePerformIO()).unsafePerformIO() match {
      case Left(t) => ta(t)
      case Right(a) => a
    })
}
object IO {
  def point[A](a: => A): IO[A] = IO(() => a)
  def tryIO[A](a: => A): IO[Either[Throwable, A]] =
    IO(() => try { Right(a) } catch { case t : Throwable => Left(t) })
}


liftM f m1 = do { x1 <- m1; return (f x1) }
           = m1 >>= \x1 -> return (f x1)

do { x1 <- m1; x2 <- m2; return (f x1 x2) }
m1 >>= (\x1 -> m2 >>= (\x2 -> return (f x1 x2) ))

Lecture 13a: Free Monads

---

A `Monoid[A]` essentially models a function of type `List[A] => A`.
<br />
<br />
We saw how the unit and associativity laws for monoids model the fact that we can fold left and right, and that the empty list doesn’t count.
<br />
<br />
We also defined a monoid as a category with one object.

---

Before, we represented `Monoid` as having a binary function and an identity element for that function.
<br />
<br />
By our categorical definition, we could just restrict the Scala category (where types are the objects and functions are the arrows) and define `Monoid` in terms of that:

		!scala
		class Endo[M] {
		  def compose(m1: M => M, m2: M => M): M => M
				= m1 compose m2
		  def identity: M => M = m => m
		}

---

Recall that a function from some type to itself is called an endomorphism.
<br />
<br />
`Endo[M]` for any type `M`, is a monoid, by our previous definition.
<br />
<br />
It is a category with one object M whose only arrows are endomorphisms on `M`.

---

We know already that lists and monoids are intimately connected. Now notice that `M => M` is exactly the form of the right-hand side of foldRight:

	!scala
	def foldRight[A,B]:
		(List[A], A => (B => B)) => (B => B)

`foldRight`, then, is saying that a `List[A]` together with a function from a value of type `A` to an endomorphism on `B` allows us to collapse all those `A`s into a single endomorphism on `B`.

---

Here’s a possible implementation:

    !scala
    def foldRight[A,B] =
      (as: List[A], f: A => B => B) => {
        val m = new Endo[B]
        as match {
          case Nil => m.identity
          case x :: xs => m.compose(f(x),
                                    foldRight(as, f))
        }

---

#List Monoid Composition

If we had a `List[List[A]]` such that A is a monoid, so we know we can go from `List[A]` to `A`, then we could fold all the inner lists and end up with `List[A]` which we can then fold.
<br />
<br />
Another way of saying the same thing is that we can pass the list constructor `::` to `foldRight`.

---

`::` has the correct type for the `f` argument, namely `A => List[A] => List[A]`.
<br />
<br />
That in turn gives us another function of type `List[A] => List[A] => List[A]`, which appends one list to another.
<br />
<br />
If we pass that to `foldRight`, we get a `List[List[A]] => List[A] => List[A]`.
<br />
<br />
That again has the proper type for `foldRight`, and so on.

---

The pattern here is that we can keep doing this to turn an arbitrarily nested list of lists into an endofunction on lists, and we can always pass an empty list to one of those endofunctions to get an identity.
<br />
<br />
So the types of arbitrarily nested lists are monoids. Our composition for such a monoid appends one list to another, and the identity for that function is the empty list.


---

The Free Monad (data structure) is to the Monad (class) like the List (data structure) to the Monoid (class): It is the trivial implementation, where you can decide afterwards how the content will be combined.
<br />
<br />
You probably know what a Monad is and that each Monad needs a specific (Monad-law abiding) implementation of either fmap + join + return or bind + return.

---

Let us assume you have a Functor (an implementation of fmap) but the rest depends on values and choices made at run-time, which means that you want to be able to use the Monad properties but want to choose the Monad-functions afterwards.
<br />
<br />
That can be done using the Free Monad (data structure), which wraps the Functor (type) in such a way so that the join is rather a stacking of those functors than a reduction.

---

The real return and join you want to use, can now be given as parameters to the reduction function foldFree:

foldFree :: Functor f => (a -> b) -> (f b -> b) -> Free f a -> b
foldFree return join :: Monad m => Free m a -> m a

To explain the types, we can replace Functor f with Monad m and b with (m a):

foldFree :: Monad m => (a -> (m a)) -> (m (m a) -> (m a)) -> Free m a -> (m a)

---

Free monads are just a general way of turning functors into monads. That is, given any functor f Free f is a monad. This would not be very useful, except you get a pair of functions

liftFree :: Functor f => f a -> Free f a
foldFree :: Functor f => (f r -> r) -> Free f r -> r

the first of these lets you "get into" your monad, and the second one gives you a way to "get out" of it.

---

More generally, if X is a Y with some extra stuff P, then a "free X" is a a way of getting from a Y to an X without gaining anything extra.
<br />
<br />
Examples: a monoid (X) is a set (Y) with extra structure (P) that basically says it has an operations (you can think of addition) and some identity (like zero).

---

Given any type t we know that [t] is a monoid

instance Monoid [t] where
  mempty   = []
  mappend = (++)

So lists are the "free monoid" over sets (or in Haskell types).

---

Free monads are the same idea. We take a functor, and give back a monad. In fact, since monads can be seen as monoids in the category of endo functors, the definition of a list

data [a] = [] | a : [a]
looks a lot like the definition of free monads

data Free f a = Pure a | Roll (f (Free f a))

---

#Links

[Free Monads and Free Monoids](http://blog.higher-order.com/blog/2013/08/20/free-monads-and-free-monoids/)

http://blog.scalac.io/2016/06/02/overview-of-free-monad-in-cats.html
http://eed3si9n.com/herding-cats/Free-monads.html
http://okmij.org/ftp/Computation/free-monad.html
http://underscore.io/blog/posts/2015/04/14/free-monads-are-simple.html#fnref:continuation-monad

https://www.youtube.com/watch?v=M5MF6M7FHPo
https://www.youtube.com/watch?v=rK53C-xyPWw
https://www.youtube.com/watch?v=M258zVn4m2M
