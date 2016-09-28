#Lecture 13a: Interpreters

![](images/lecture3/gang-of-four-monads.png)

---

The interpreter is the über pattern of functional programming.

Intuition:

We treat the interpreter as part of the runtime.

We can talk about programs as values right up until we run them.
---

!haskell
interpretMonoid :: Monoid b => (a -> b) -> ([a] -> b)
interpretMonoid f [] = mempty
interpretMonoid f (a : as) = f a <> interpretMonoid f as

---

Most large programs written in a functional style can be viewed as using this pattern.

Amongst many reasons, interpreters allow us to handle effects and still keep desirable properties such as substitution.

---

Why Do We Use Interpreters?
One important reason for functional programmers liking the interpreter pattern is how it allows us to deal with effects. Effects are problematic, because they break substitution. Substitution allows easy reasoning about code, so functional programmers strive hard to maintain it. At some point you have to have effects—if not, the program will not do anything useful. The secret to allowing effects is to delay them until some point in the program where we don’t care about substitution anymore. For example, in a web service we can delay effects till we’ve constructed the program to create the response, and then run that program, causing our database effects and so on to occur. In Doodle we delay effects—drawing—until we’ve fully described the picture we want to draw. If you’re familiar with computer graphics, Doodle is essentially constructing a scene graph.

---

#Example: Random

The Random monad allows us to separate describing how to generate random data from actually introducing randomness.

Randomness is an effect (a function that generates a random value breaks substitution, as it returns a different value each time it is called) and so we can use the Random monad to control this effect.

---

Let’s look at two different implementation strategies.

The first strategy is to reify the methods to an algebraic data type.


import cats.Monad
sealed trait AlgebraicDataType[A] extends Product with Serializable {
  def run(rng: scala.util.Random = scala.util.Random): A =
    this match {
      case Primitive(sample) => sample(rng)
      case FlatMap(fa, f) => f(fa.run(rng)).run(rng)
    }
  def flatMap[B](f: A => AlgebraicDataType[B]): AlgebraicDataType[B] = FlatMap(this, f)

  def map[B](f: A => B): AlgebraicDataType[B] =
    FlatMap(this, (a: A) => AlgebraicDataType.always(f(a)))
}


object AlgebraicDataType {
  def always[A](a: A): AlgebraicDataType[A] =
    Primitive(rng => a)

  def int: AlgebraicDataType[Int] =
    Primitive(rng => rng.nextInt())

  def double: AlgebraicDataType[Double] =
    Primitive(rng => rng.nextDouble())

  implicit object randomInstance extends Monad[AlgebraicDataType] {
    def flatMap[A, B](fa: AlgebraicDataType[A])(f: (A) ⇒ AlgebraicDataType[B]): AlgebraicDataType[B] =
      fa.flatMap(f)

    def pure[A](x: A): AlgebraicDataType[A] =
      AlgebraicDataType.always(x)
  }
}
final case class FlatMap[A,B](fa: AlgebraicDataType[A], f: A => AlgebraicDataType[B]) extends AlgebraicDataType[B]
final case class Primitive[A](sample: scala.util.Random => A) extends AlgebraicDataType[A]
An alternative implementation strategy is to reify with functions.

import cats.Monad

final case class Lambda[A](get: scala.util.Random => A) {
  def run(rng: scala.util.Random = scala.util.Random): A =
    get(rng)

  def flatMap[B](f: A => Lambda[B]): Lambda[B] =
    Lambda((rng: scala.util.Random) => f(get(rng)).run(rng))

  def map[B](f: A => B): Lambda[B] =
    Lambda((rng: scala.util.Random) => f(get(rng)))
}
object Lambda {
  def always[A](a: A): Lambda[A] =
    Lambda(rng => a)

  def int: Lambda[Int] =
    Lambda(rng => rng.nextInt())

  def double: Lambda[Double] =
    Lambda(rng => rng.nextDouble())

  implicit object randomInstance extends Monad[Lambda] {
    def flatMap[A, B](fa: Lambda[A])(f: (A) ⇒ Lambda[B]): Lambda[B] =
      fa.flatMap(f)

    def pure[A](x: A): Lambda[A] =
      Lambda.always(x)
  }
}
I call the former strategy (AlgebraicDataType) a transparent interpreter, because we can programmatically inspect the AlgebraicDataType data structure, and the later strategy (Lambda) an opaque interpreter, because we can’t look into the anonymous functions. Update: in the literature you’ll find the terms shallow and deep embedding are used for what I call opaque and transparent respectively. Thanks for Gabriel Claramunt for pointing this out.

There are two ways in which the opaque interpreter is superior to the transparent interpreter.

The opaque interpreter doesn’t require we implement an algebraic data type to represent the “language” we want to interpret. This certainly saves on the typing. In some sense functions are universal interpreters. We can represent any language we like in terms of functions, so long as we can accept the semantics we get from Scala.

The other main advantage of the opaque interpreter is that code within a function is just code. The compiler is going to have a much easier time optimising the opaque representation than the transparent one. We can say the opaque representation is more transparent to the compiler.

The transparent interpreter is more modular than the opaque one. With a transparent interpreter we can see the structure of the program we’re interpreting in terms of its algebraic data type representation. This means we can choose to interpret it in different ways. For instance, we could print logging information during interpretation, or run in a distributed environment, or use a “stackless” implementation to avoid overflowing the stack in deeply nested calls. With the opaque representation we can’t make these choices.

In summary, transparent interpreters trade performance for flexibility. I usually find that flexibility is the right choice.

---

#Modular Interpreters

One way we can take advantage of the modularity offered by transparent interpreters is by capturing common patterns in reusable classes. This is exactly what the free monad does. Here’s an example of the Random monad using the free monad implementation in Cats. As you can see, we don’t have to write a great deal of code, and we benefit from an optimised and stack-safe implementation.


  import cats.free.Free
  import cats.{Comonad,Monad}

  type Random[A] = Free[Primitive,A]
  final case class Primitive[A](sample: scala.util.Random => A)

  implicit val randomMonad: Monad[Random] = Free.freeMonad

  implicit def randomInterpreter(implicit rng: scala.util.Random = scala.util.Random): Comonad[Primitive] =
    new Comonad[Primitive] {
      override def coflatMap[A, B](fa: Primitive[A])(f: (Primitive[A]) ⇒ B): Primitive[B] =
        Primitive(rng => f(fa))

      override def extract[A](x: Primitive[A]): A =
        x.sample(rng)

      override def map[A, B](fa: Primitive[A])(f: (A) ⇒ B): Primitive[B] =
        Primitive(rng => f(fa.sample(rng)))
    }


---

Conclusions
The transparency tradeoff is a major design decision in creating an interpreter. Over time I feel that as a community we’re moving towards more transparent representations, particularly as techniques like the free monad become more widely known.

Note that transparency is not a binary choice. Even in the transparent implementation above, we have opaque functions passed to flatMap and friends. The ultimate endpoint of a transparent implementation is implementing all language features—conditionals, binding constructs, and everything else—within the little language we’re defining.

The opaque interpreter reminds of higher order abstract syntax (HOAS), which a technique for representing variable binding in an interpreter by reusing the host language’s implementation. HOAS has the same drawback as our opaque interpreters: since we can’t inspect the structure of the bindings in HOAS we have to use the host language’s semantics. There is some work on removing this restriction. I’m not familiar enough with this work to say if and how it applies to the discussion here.


---

#Links

http://underscore.io/blog/posts/2016/06/27/opaque-transparent-interpreters.html
http://underscore.io/blog/posts/2016/04/21/probabilistic-programming.html
http://blog.scalac.io/2016/06/02/overview-of-free-monad-in-cats.html
http://eed3si9n.com/herding-cats/Free-monads.html
http://okmij.org/ftp/Computation/free-monad.html
http://underscore.io/blog/posts/2015/04/14/free-monads-are-simple.html#fnref:continuation-monad

https://www.youtube.com/watch?v=M5MF6M7FHPo
https://www.youtube.com/watch?v=rK53C-xyPWw
https://www.youtube.com/watch?v=M258zVn4m2M
