
#Generic Programming

---

Generic programming is the art of programming with types that you know nothing about.
<br />
<br />
GP enables abstractions over shapes richer than can be captured with traditional parametric polymorphism, subtype polymorphism, or ad-hoc (typeclass) polymorphism:

* type level functions
* [dependent types](https://en.wikipedia.org/wiki/Functional_dependency)
* [functional dependencies](https://en.wikipedia.org/wiki/Functional_dependency)

---

The main generic programming library in Scala (other than the Reflections API) is Shapeless, which was started by Miles Sabin in 2011 as a research project.
<br />
<br />
It is now an extremely active project w/ many commercial users.
<br />
<br />
Key idea (following SYB): represent polymorphic function values via type classes

---

One of the main use cases for data engineering is to reduce boilerplate and enforce constraints at compile time with the following strategy:

* trasform a domain type into a generic representation.

* write a program that manipulates that generic representation _at the type level_

* apply ('shape') the result to many different types as needed


---

# Example: Singleton Types

The Scala compiler can understand a constant value as a type.

    !scala
    "bar".narrow // <: String
    //"bar".narrow : String("bar")
    42.narrow    // <: Int
    //42.narrow    : Int(42)  

For performance reasons, the JVM runtime uses type erasure to merge these with their superclasses, but at compile time they are understood as distinct elements of Scala's type system.

---

Singleton types bridge the gap between the value level and the type level and hence allow the exploration in Scala of techniques which would typically only be available in languages with support for full-spectrum dependent types.

You've seen at least one example of a 'native' singleton type before:

    !scala
    Nil.narrow
    //res0: scala.collection.immutable.Nil.type = List()


---

# Heterogenous lists

Heterogeneous lists are the core abstraction of Shapeless, most of its features revolve around the `HList` type:

    !scala
    sealed trait HList
    case class ::[+H, +T <: HList](head : H, tail : T) extends HList
    case object HNil extends HList

---

    !scala
    import shapeless.{HList, HNil, ::}
    val hl: String :: Int :: HNil = "foo" :: 42 :: HNil
    type StrInt = String :: Int :: HNil
    hl: ::[String, ::[Int, HNil]] = foo :: 42 :: HNil


---

An `HList` can have elements of arbitrary types, and unlike a `List` it will retain the types of each of its elements.

    !scala
    val l = "foo" :: 42 :: Nil
    l: List[Any] = List(foo, 42)
    type IntStr =  Int :: String :: HNil
    val hl2 : IntStr = "foo" :: 42 :: HNil
    // error: type mismatch

---

We can get the head or tail of an `HList` if and only if it is a cons:

    !scala
    hl.head
    //res0: String = foo
    hl.tail
    //res1: shapeless.::[Int,shapeless.HNil] = 42 :: HNil
    hl.tail.tail
    //res2: shapeless.HNil = HNil

---

Of course if it is `HNil` there is no such method to call :

    !scala
    hl.tail.tail.head
    //error: could not find implicit value for parameter c: //shapeless.ops.hlist.IsHCons[shapeless.HNil]

---

Note that this is a compile error, and not a runtime exception like the one that arises when we try to access the head or tail of an empty List.

    !scala
    List().head
    //java.util.NoSuchElementException: head of empty list

---

Shapeless provides an easy way to encode case classes as `HLists`, again with compile-time type guarantees on structure.
<br />
<br />
This lends itself well to employing Spark `Dataset`s, which express semistructured data as case classes.

---

    !scala
    case class Person(name: String, age: Int)
    val genPerson = Generic[Person]
    val alice = Person("Alice", 25)
    val repr = genPerson.to(alice)
    //genPerson.Repr = Alice :: 25 :: HNil
    val bob = genPerson.from("Bob" :: 22 :: HNil)
    //bob: Person = Person(Bob,22)
    val eve = genPerson.from("Eve" :: "23" :: HNil)
    //error: type mismatch

---

This functionality extends to arbitrary ADTs defined by inheritance:

    !scala
    // Simple recursive case class family
    sealed trait Tree[T]
    case class Leaf[T](t: T) extends Tree[T]
    case class Node[T]
      (left: Tree[T], right: Tree[T]) extends Tree[T]

---

    !scala
    val tree: Tree[Int] =
      Node(
        Node(
          Leaf(1),
          Leaf(2)
        ),
        Node(
          Leaf(3),
          Leaf(4)
        )
      )

---

    !scala
    val treeGen = Generic[Tree[Int]]
    treeGen.to(tree)
    //res0 = Inr(Inl(Node(Node(Leaf(1),Leaf(2)),Node(Leaf(3),Leaf(4)))))

---

We can also map over these structures:

    !scala
    object inc extends Poly1 {
      implicit def caseInt = at[Int](i => i+1)
    }
    everywhere(inc)(tree)
    //res1: Tree[Int] = Node(Node(Leaf(2),Leaf(3)),Node(Leaf(4),Leaf(5)))

More on `Poly1` in a bit.

---

# Implicit Recursion

However there’s not much else we can do with an arbitrary `HList`, because almost all the methods are injected via type classes.
<br />
<br />
Lets work through an example of this, computing the size of an `HList`.

---

    !scala
    case class size[L <: HList](get: Int)
    object size {
      implicit val hnilsize = size[HNil](0)
      implicit def hconssize[H, T <: HList]
        (implicit tailsize: size[T]) =
          size[H :: T]( 1 + tailsize.get)
      def apply[L <: HList](l: L)
        (implicit size: size[L]): Int = size.get
    }
    val hl = "foo" :: true :: HNil
    size(hl)
    //res0: Int = 2

---

How does it work?

* `size(hl)` requires an implicit `size[String :: Boolean :: HNil]`
* ... which we obtain by calling `hconssize[String, Boolean :: HNil]`
* ... which itself requires a `size[Boolean :: HNil]`
* ... which we obtain by calling `hconssize[Boolean, HNil]`
* ... which itself requires a `size[HNil]`
* ... which we obtain by returning `hnilsize`

---

In the end calling `size(hl)` returns:

    !scala
    hconssize[String, Boolean :: HNil](
      hconssize[Boolean, HNil](
        hnilsize))
---

Or equivalently:

    !scala
    size[String :: Boolean :: HNil]( 1 +
      size[Boolean :: HNil]( 1 +
        size[HNil](0).get).get)

---


What about mapping over an `HList`?
<br />
<br />
To map over an `HList` we will need a polymorphic 'function' that can be applied to each of the types in the list:

---

#Polymorphic Functions

A polymorphic function is a function that is defined for various – possibly unrelated – input types:


    !scala
    import poly._
    object choose extends (Set ~> Option) {
      def apply[T](s : Set[T]) = s.headOption
    }
    val sets = Set(1) :: Set("foo") :: HNil
    val opts = sets map choose   
    //opts = Some(1) :: Some(foo) :: HNil

---

Poly functions are convertible to an ordinary monomorphic function and can be mapped across an ordinary Scala List

    !scala
    List(Set(1, 3, 5), Set(2, 4, 6)) map choose
    //res3: List[Option[Int]] = List(Some(1), Some(2))

---

Shapeless provides several traits with methods to make poly function construction more convenient:

    !scala
    object size extends Poly1 {
      implicit def caseInt = at[Int](x => 1)
      implicit def caseString = at[String](_.length)
      implicit def caseTuple[T, U]
        (implicit st : Case.Aux[T, Int], su : Case.Aux[U, Int]) =
          at[(T, U)](t => size(t._1)+size(t._2))
    }

The `at[T]` method, takes a function as a parameter, that represents what `size` does when called with an argument of type `T`.

---


`size` is a polymorphic function of one argument, defined as a bunch of `implicit def`s that mimic pattern-matching on types.
<br />
<br />
Actually `size` is functioning like an entire type class, and the cases are functioning like type class instances for that type class (SYB with class).

---

The `Poly1` trait also gives our `size` object an `apply` method so we can use it as a standard function:

    !scala
    size(23)
    //res4 = ???
    size("foo")
    //res5 = ???
    size((23, "foo"))
    //res6 = ???
    size(((23, "foo"), 13))
    //res7 = ???
    (1 :: "23" :: 4 :: HNil) map size
    //res8 = ???
    size(1 :: "23" :: 4 :: HNil)
    //res9 = ???

---

Let's extend a similar `Poly1` function to `HList`s:

    !scala
    import shapeless._
    object makeBigger extends Poly1 {
      implicit def intCase = at[Int](_ * 100)
      implicit def stringCase = at[String](_.toUpperCase)
    }
    makeBigger(42)
    //res0: Int = 4200
    makeBigger("foo")
    //res1: String = FOO

---

If we attempt to call it with an argument of an unhandled type, we get a compilation error:

    !scala
    makeBigger(true)
    //error: could not find implicit value for parameter

---

We also get the same error if we widen the argument’s type up to an unhandled supertype:

    !scala
    makeBigger(42: Any)
    //error: could not find implicit value for parameter
<br />
<br />
To understand the implicit resolution errors here, we have to take a closer look at the `Poly` trait.

---

First, we can inspect the types of `intCase` and `stringCase`:

    !scala
    :type makeBigger.intCase
    //makeBigger.Case[Int]{type Result = Int}
    :type makeBigger.stringCase
    //makeBigger.Case[String]{type Result = String}

---

The inner type `Case[T]` is inherited from `Poly1`; it represents the part of the definition of a `Poly1` that takes care of an argument of type `T`.
<br />
<br />
Likewise, there is a similar type `Case[T, U]` defined in `Poly2`, a `Case[T,U,V]` in `Poly3` and so on.

---

All these different `Case` types are in fact aliases to the `shapeless.poly.Case` class.
<br />
<br />
`shapeless.poly.Case` takes two type parameters: the type of the `Poly` in which this case is defined, and a `Hlist` type that contains the case’s parameters types:

    !scala
    abstract class Case[P, L <: HList] extends Serializable {
      type Result
      val value : L => Result
      def apply(t : L) = value(t)
    }

---

    !scala
    makeBigger.intCase
              .isInstanceOf[
                shapeless.poly.Case[
                  makeBigger.type, Int::HNil]]
    res3: Boolean = true

<br />
<br />
When we called `makeBigger(true)` above, `scalac` was unsuccessfully trying to instantiate a `Case[Boolean]`.

---


Here is the type signature for `Poly1`s `apply` method:

    !scala
    def apply[A](a:A)
      (implicit cse : poly.Case[this.type, A::HNil]): cse.Result

Note that the return type of `apply` is computed in the process of resolving the relevant `implicit`. This is known as a [functional dependency](https://en.wikipedia.org/wiki/Functional_dependency)

---

We can now see how the `apply` method works for `Poly1`:

* For any `Poly1` subtype `F`, given a argument of type `T`, an instance of `shapeless.poly.Case[F,T::HNil]` is searched in the implicit scope.
* If we have defined a parameterless implicit method in object `F` that returns `at[T](f)` (`f` being a function defined on `T`), this method will be selected since it returns an instance of `shapeless.poly.Case[F, T::HNil]`.
* The result of applying `F` to an argument of type `T` will be the one of applying `f` to that argument.

---

Now imagine we want our `makeBigger` function to operate on `HList`s. Say for example that we make an `HList` bigger by duplicating each of its elements:

    !scala
    makeBigger(true :: 1.2 :: HNil)
    //res9 = true :: true :: 1.2 :: 1.2 :: HNil

How can we define enough cases in `makeBigger` to cover each possible `HList` subtype?

---

We'll take the same approach as we did before, this time using a `Poly1`.
<br>
<br>
If we can handle the empty case (`HNil`) and the composite case `H :: T` (with `T` a `HList`) then we can handle any possible `HList`.
<br>
<br>
First we need to handle the empty case, which as usual is rather easy :

    !scala
    implicit def hnilCase = at[HNil](identity)  

---

We need to somehow call `makeBigger` recursively. We would like to write something equivalent to the following:

    !scala
    implicit def hconsCase[H, T <: HList] =
      at[H :: T]( l => l.head :: l.head :: makeBigger(l.tail))
<br>
<br>
But to call `makeBigger(l.tail)` we need to have an implicit `makeBigger.Case[T]` in scope, since `l.tail` is of type `T`.

---

So lets bring it into the scope by adding an implicit parameter to `hconsCase` for it:

    !scala
    implicit def hconsCase[H, T <: HList]
                          (implicit tailCase: Case[T]) =
      at[H :: T](l => l.head :: l.head :: tailCase(l.tail))
<br>
<br>
Now we are simultaneously recursing at both the type and value levels.

---

There's a problem, this case won’t type check.
<br>
<br>
Since there is nothing in it that constrains the result of `tailCase(l.tail)` to be a `HList`, we cannot pass it as the left hand side of `::`.


---

Recall that the `Case[X]` type has an abstract type member `Result`, we can use that it to express our constraint:

    !scala
    implicit def hconsCase[H, T <: HList]
                          (implicit tailCase:
                makeBigger.Case[T]{type Result <: HList}) = at[H::T]
                (l => l.head :: l.head :: makeBigger(l.tail))

---

Our `makeBigger` function is now able to operate on `HLists`:

    !scala
    object makeBigger extends Poly1 {
      implicit def intCase = at[Int](_ * 100)
      implicit def stringCase = at[String](_.toUpperCase)
      implicit def hnilCase = at[HNil](identity)  
      implicit def hconsCase[H, T <: HList]
        (implicit tailCase: makeBigger.Case[T]{type Result <: HList}) =   
          at[H::T](l => l.head :: l.head :: makeBigger(l.tail))
    }
    makeBigger(true :: 1.2 :: HNil)
    //res27 = true :: true :: 1.2 :: 1.2 :: HNil

---

The syntax we used to constrain `Result` to be a `HList` in the previous example was a bit clunky. Fortunately, there is a nice type alias defined inside `Case`:

    !scala
    object Case {
      type Aux[In, Out] = Case[In] { type Result = Out }
    }

---

Using `Aux`, we can rewrite the previous example like so:

    !scala
    object makeBigger extends Poly1 {
      implicit def intCase = at[Int](_ * 100)
      implicit def stringCase = at[String](_.toUpperCase)
      implicit def hnilCase = at[HNil](identity)  
      implicit def hconsCase[H, T <: HList, R <: HList]
        (implicit tailCase: Case.Aux[T, R]) =
          at[H::T](l => l.head :: l.head :: makeBigger(l.tail))    
    }

---

Again, here is the type signature for `Poly1`s `apply` method:

    !scala
    def apply[A](a:A)
      (implicit cse : poly.Case[this.type, A::HNil]): cse.Result

---

Notice how `apply` is parametrized only on the input type (so that users can call it without knowing the return type), which was perfectly fine until we had to enforce some constraint over that result type (eg in the `hconsCase` above).
<br />
<br />
On the other hand, the aux trick allows us to bind the abstract type member to a type variable that we constrain as we see fit.

---
#Links

[Scrap your boilerplate: a practical approach to generic programming](http://research.microsoft.com/en-us/um/people/simonpj/papers/hmap/hmap.ps), Ralf Laemmel and Simon Peyton Jones, Proc ACM SIGPLAN Workshop on Types in Language Design and Implementation, TLDI 2003
[Scrap more boilerplate: reflection, zips, and generalised casts](http://research.microsoft.com/en-us/um/people/simonpj/papers/hmap/gmap2.ps), Ralf Laemmel and Simon Peyton Jones, ICFP 2004
[Scrap your boilerplate with class: extensible generic functions](http://research.microsoft.com/en-us/um/people/simonpj/papers/hmap/gmap3.pdf), Ralf Laemmel and Simon Peyton Jones, ICFP 2005
[SO post](http://stackoverflow.com/questions/11825129/are-hlists-nothing-more-than-a-convoluted-way-of-writing-tuples) by Miles Sabin.
