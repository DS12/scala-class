
#Lecture 13: Free Objects


---

A free foo happens to be the simplest thing that satisfies all of the 'foo' laws. That is to say it satisfies exactly the laws necessary to be a foo and nothing extra.


Let me give a simple example of this separation of structure and meaning. Consider the expression

1 + 2 + 3

When we write this expression we bundle the structure of the computation (two additions) with the meaning given to that computation (Int addition).

We could separate structure and meaning by representing the structure of the computation as data, perhaps as3

Add(1, Add(2, 3))
Now we can write a simple interpreter to give meaning to this structure. Having separated the abstract syntax tree from the interpreter we can choose different interpretations for a given tree.

---

#Free Monoids

Our goal with implementing the free monoid is to represent computations like

    !scala
    1 + 2 + 3

in a generic way without giving them any particular meaning.

---

The free monoid will wrap an arbitrary type and must itself be a monoid.

A monoid for some type A is defined by:

* an operation append with type `(A, A) => A`
* an element zero of type `A`

---

The following laws must also hold for all x, y, z, and a in `A`.

* `append(x, append(y, z)) == append(append(x, y), z)`
* `append(a, zero) == append(zero, a) == a`

---

The monoid operations (append and zero) suggest we want a structure something like

sealed trait FreeMonoid[+A]
final case object Zero extends FreeMonoid[Nothing]
final case class Value[A](a: A) extends FreeMonoid[A]
final case class Append[A](l: FreeMonoid[A], r: FreeMonoid[A]) extends FreeMonoid[A]

---

Now we can represent 1 + 2 + 3 as

Append(Value(1), Append(Value(2), Value(3)))

This is not the simplest representation we can use.

---

With a bit of algebraic manipulation, justified by the monoid laws, we can normalize any monoid expression into a form that allows for a simpler representation.

Let’s illustrate this via algebraic manipulation on 1 + 2 + 3.

---

The identity law means we can insert the addition of zero in any part of the computation without changing the result, and likewise we can remove any zeros (unless the entire expression consists of just zero).

We’re going to decree that any normalized expression must have a single zero at the end of the expression like so:

1 + 2 + 3 + 0

---

The associativity law means we can place brackets wherever we want. We’re going to decide to bracket expressions so traversing the expression from left to right goes from outermost to innermost bracket, like so:

(1 + (2 + (3 + 0)))

Append(1, Append(2, Append(3, Zero)))

---

With these changes – which by the monoid laws make no difference to the meaning of the expression – we can construct the following abstract syntax tree.

sealed trait FreeMonoid[+A]
final case object Zero extends FreeMonoid[Nothing]
final case class Append[A](l: A, r: FreeMonoid[A]) extends FreeMonoid[A]


---

The final step is to recognise that this structure is isomorphic to `List`. So we could just as easily write

1 :: 2 :: 3 :: Nil
or

List(1, 2, 3)

---

Our final step is to make sure that List itself a monoid. It is. The monoid operations on List are:

append is ++, list concatentation;
zero is Nil, the empty list; and
we can “lift” any type into the free monoid using List.apply


---
#Adjunctions

A forgetful functor is one that "forgets" part of the structure as it goes from one category to another.

Given functors F : D -> C, and G : C -> D, we say F -| G, F is left adjoint to G, or G is right adjoint to F whenever forall a, b: F a -> b is isomorphic to a -> G b, where the arrows come from the appropriate categories.

Formally, a free functor is left adjoint to a forgetful functor.

---

You can make a functor U from the category of monoids (where arrows are monoid homomorphisms, that is, they ensure they map unit to unit on the other monoid, and that you can compose before or after mapping to the other monoid without changing meaning) to the category of sets (where arrows are just function arrows) that 'forgets' about the operation and unit, and just gives you the carrier set.

---

The target of F is in the category Mon of monoids, where arrows are monoid homomorphisms, so we need a to show that a monoid homomorphism from [a] -> b can be described precisely by a function from a -> b.

In Haskell, we call the side of this that lives in Set (er, Hask, the category of Haskell types that we pretend is Set), just foldMap, which when specialized from Data.Foldable to Lists has type Monoid m => (a -> m) -> [a] -> m.

---

!haskell
interpretMonoid :: Monoid m => (a -> m) -> ([a] -> m)
interpretMonoid f [] = mempty
interpretMonoid f (a : as) = f a <> interpretMonoid f as

---


A general `Monoid[A]` essentially models a function of type `List[A] => A`.
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

#Example: Bank

This is an example of a catamorphism.

!haskell
data BankOp = Deposit Int | Withdraw Int
program = [Deposit 10, Withdraw 5, Withdraw 2, Deposit 3]

interpretOp :: BankOp -> Int
interpretOp (Deposit d) = d
interpretOp (Withdraw w) = -w

interpret = interpretMonoid interpretOp
interpret program --6


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

#Free Monads

The Free Monad (data structure) is to the Monad (class) like the List (data structure) to the Monoid (class): a 'trivial' implementation, where you can decide afterwards how the content will be combined.
<br />
<br />
You know what a Monad is and that each Monad needs a specific (Monad-law abiding) implementation of either fmap + join + return or bind + return.

Free monads are the same idea. We take a functor, and give back a monad.

---

In fact the definition of a list looks a lot like the definition of a free monad:

  !haskell
  data [a] = [] | a : [a]
  data Free f a = Return a | Suspend (f (Free f a))

---

This makes sense, since free monads can be seen as free monoids in the category of endofunctors.

Unlike List, which stores a list of values, Free stores a list of functors, wrapped around an initial value.

Accordingly, the Functor and Monad instances of Free do nothing other than handing a given function down that list with fmap.

---

---

Example: Toy

Slight variation on an example from [Gabriel Gonzalez](http://www.haskellforall.com/2012/06/you-could-have-invented-free-monads.html):

  !scala
  sealed trait Toy[+Next]
  object Toy {
    case class Output[Next](a: Char, next: Next) extends Toy[Next]
    case class Bell[Next](next: Next) extends Toy[Next]
    case class Done() extends Toy[Nothing]
    def output[Next](a: Char, next: Next): Toy[Next] = Output(a, next)
    def bell[Next](next: Next): Toy[Next] = Bell(next)
    def done: Toy[Nothing] = Done()
  }
  import Toy._
  output('A', done)
  //res0: Toy[Toy[Nothing]] = Output(A,Done())
  bell(output('A', done))
  //res1: Toy[Toy[Toy[Nothing]]] = Bell(Output(A,Done()))

---

Note that the type changes every time a command is added.

case class Fix[F[_]](f: F[Fix[F]])
object Fix {
  def fix(t: Toy[Fix[Toy]]) = Fix[Toy](t)
}

import Fix._
fix(output('A', fix(done)))
//res176: Fix[Toy] = Fix(Output(A,Fix(Done())))
fix(bell(fix(output('A', fix(done)))))
//res177: Fix[Toy] = Fix(Bell(Fix(Output(A,Fix(Done())))))


data Fix f = Fix (f (Fix f))
data ListF a b = Nil | Cons a b
type List a = Fix (ListF a)


There's still a problem. This approach only works if you can use the Done constructor to terminate every chain of functors. Unfortunately, programmers don't often have the luxury of writing the entire program from start to finish. We often just want to write subroutines that can be called from within other programs and our Fix trick doesn't let us write a subroutine without terminating the entire program.


Let us assume you have a `Functor` but the rest depends on values and choices made at run-time.

This means that you want to be able to use the monad properties but want to choose the actual typeclass implementation afterwards.
<br />
<br />
That can be done using the Free Monad (data structure), which wraps the Functor (type) in such a way so that the `join` is rather a stacking of those functors than a reduction.

---

Free monads are just a general way of turning functors into monads. That is, given any functor f Free f is a monad. This would not be very useful, except you get a pair of functions

liftFree :: Functor f => f a -> Free f a
foldFree :: Functor f => (f r -> r) -> Free f r -> r

the first of these lets you "get into" your monad, and the second one gives you a way to "get out" of it.

---

The real `return` and `join` you want to use, can now be given as parameters to the reduction function `foldFree`:

  !haskell
  foldFree :: Functor f => (a -> b) -> (f b -> b) -> Free f a -> b
  foldFree return join :: Monad m => Free m a -> m a

To explain the types, we can replace Functor f with Monad m and b with (m a):

foldFree :: Monad m => (a -> (m a)) -> (m (m a) -> (m a)) -> Free m a -> (m a)

---

We can take the same approach starting with the monad operations `unit` and `flatMap`, but our task will be easier if we reformulate monads in terms of `unit`, `map`, and `join`.

Under this formulation a monad for a type `F[_]` has:

* a unit with type `A => F[A]`;
* a join with type `F[F[A]] => F[A]`
* a map with type `(F[A], A => B) => F[B]`

---

From this list of operations we can start to create an abstract syntax tree. We start with the definition of `Free`.

  !scala
  sealed trait Free[F[_], A]

---

We can directly convert `unit` into a case `Return`:

  !scala
  final case class Return[F[_], A](a: A) extends Free[F, A]

---

We are going to convert `join` into a case `Suspend`, but what type of value should we store?

We might think to store a value of type `F[F[A]]`, but if we did then we wouldn’t be able to store, say, a `Return` inside the outer `F`.

---

We can break it down like this:

The inner F[A] will be represented by an instance of the free monad, and thus has type Free[F, A].
The outer F[_] will be wrapped in the Suspend we’re creating.
Therefore the value we should store has type F[Free[F, A]] giving us

final case class Suspend[F[_], A](f: F[Free[F, A]]) extends Free[F, A]

---

Finally, our free monad data type looks like

sealed trait Free[F[_], A]
final case class Return[F[_], A](a: A) extends Free[F, A]
final case class Suspend[F[_], A](s: F[Free[F, A]]) extends Free[F, A]

---

Let's look at a Haskell implementation:

!haskell
data Free f a = Pure a | Suspend (f (Free f a))

Again, note the similarity to the definition of list:

!haskell
data [a] = [] | a : [a]



---

To show this works, let’s implement the monad operations on this data type.

We’ll use the more familiar flatMap and unit formulation, which is better suited to Scala, than the unit, join, and map formulation above.

---

We can knock out unit easily enough.

object Free {
  def unit[F[_]](a: A): Free[F, A] = Return[F, A](a)
}
Things get a bit trickier with flatMap, however. Since we know Free in an algebraic data type we can easily get the structural recursion skeleton.

---

sealed trait Free[F[_], A] {
  def flatMap[B](f: A => Free[F, B]): Free[F, B] =
    this match {
      case Return(a)  => ???
      case Suspend(s) => ???
    }
}

---

The case for Return just requires us to follow the types.

sealed trait Free[F[_], A] {
  def flatMap[B](f: A => Free[F, B]): Free[F, B] =
    this match {
      case Return(a)  => f(a)
      case Suspend(s) => ???
    }
}

---

The case for Suspend is a bit trickier. The value s has type F[Free[F, A]]. The only operation we (currently) have available is f, which accepts an A. We could flatMap f over the Free[F, A] wrapped in F, but we haven’t yet required any operations on F. If we require F is a functor we can then map over it. Concretely, we can use this code snippet:

s map (free => free flatMap f)

---

A bit of algebra shows the result has type F[Free[F, B]], and we can wrap that in a Suspend to get a result of type Free[F, B]. Our final implementation is thus

sealed trait Free[F[_], A] {
  def flatMap[B](f: A => Free[F, B])(implicit functor: Functor[F]): Free[F, B] =
    this match {
      case Return(a)  => f(a)
      case Suspend(s) => Suspend(s map (_ flatMap f))
    }
}

---

We can write map in terms of flatMap

def map[B](f: A => B)(implicit functor: Functor[F]): Free[F, B] =
  flatMap(a => Return(f(a)))

---

A free monad satisfies all the Monad laws, but does not do any collapsing (i.e., computation). It just builds up a nested series of contexts. The user who creates such a free monadic value is responsible for doing something with those nested contexts, so that the meaning of such a composition can be deferred until after the monadic value has been created.


The free monad is just an abstract syntax tree representation of a monad. It has the advantage that we can define custom interpreters for the computations represented in the free monad, and with some further tricks compose monads and interpreters4.

---

#Links

[Free Monads and Free Monoids](http://blog.higher-order.com/blog/2013/08/20/free-monads-and-free-monoids/)
http://underscore.io/blog/posts/2015/04/23/deriving-the-free-monad.html
http://www.47deg.com/blog/fp-for-the-average-joe-part3-free-monads

http://blog.scalac.io/2016/06/02/overview-of-free-monad-in-cats.html
http://eed3si9n.com/herding-cats/Free-monads.html
http://okmij.org/ftp/Computation/free-monad.html
http://underscore.io/blog/posts/2015/04/14/free-monads-are-simple.html#fnref:continuation-monad

https://www.youtube.com/watch?v=M5MF6M7FHPo
https://www.youtube.com/watch?v=rK53C-xyPWw
https://www.youtube.com/watch?v=M258zVn4m2M







We are now ready to tackle the free monad.

We could do the same thing we did before, we start with a forgetful functor U from the category of monads where arrows are monad homomorphisms to a category of endofunctors where the arrows are natural transformations, and we look for a functor that is left adjoint to that.

---

But how does this relate to the notion of a free monad as it is usually used?

Knowing that something is a free monad, Free f, tells you that giving a monad homomorphism from Free f -> m, is the same thing (isomorphic to) as giving a natural transformation (a functor homomorphism) from f -> m. Remember F a -> b must be isomorphic to a -> U b for F to be left adjoint to U. U here mapped monads to functors.

---


Cofree Comonads

We can construct something similar, by looking at the right adjoint to a forgetful functor assuming it exists. A cofree functor is simply /right adjoint/ to a forgetful functor, and by symmetry, knowing something is a cofree comonad is the same as knowing that giving a comonad homomorphism from w -> Cofree f is the same thing as giving a natural transformation from w -> f.

---
---


---
