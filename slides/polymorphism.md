

#Parametric Polymorphism
<!-- http://eed3si9n.com/learning-scalaz/polymorphism.html -->

Method-level parametric polymorphism

The best way to get to grips with method-level parametric polymorphism is to compare the definitions and uses of a monomorphic and a polymorphic method. At the point of definition the constrast is simply between those methods with arguments of fixed types and those with arguments which have a type that is bound by a method-level type parameter. So, for example,

// Monomorphic methods have type parameter-free signatures
def monomorphic(s: String): Int = s.length

monomorphic("foo")

// Polymorphic methods have type parameters in their signatures
def polymorphic[T](l: List[T]): Int = l.length

polymorphic(List(1, 2, 3))
polymorphic(List("foo", "bar", "baz"))
Monomorphic methods can only be applied to arguments of the fixed types specified in their signatures (and their subtypes, I’ll come back to this in a moment), whereas polymorphic methods can be applied to arguments of any types which correspond to acceptable choices for their type parameters — in the example just given we can apply monomorphic() to values of type String only, but we can apply polymorphic() to values of type List[Int] or List[String] or … List[T] for any type T.

---

#Subtype Polymorphism


Of course, Scala is both an object-oriented and a functional programming language, so as well as parametric polymorphism (ie. polymorphism captured by type parameters) it also exhibits subtype polymorphism. That means that the methods that I’ve been calling monomorphic are only monomorphic in the sense of parametric polymorphism and they can in fact be polymorphic in the traditional object-oriented way. For instance,

trait Base { def foo: Int }
class Derived1 extends Base { def foo = 1 }
class Derived2 extends Base { def foo = 2 }

def subtypePolymorphic(b: Base) = b.foo

subtypePolymorphic(new Derived1) // OK: Derived1 <: Base
subtypePolymorphic(new Derived2) // OK: Derived2 <: Base

Here the method subtypePolymorphic() has no type parameters, so it’s parametrically monomorphic. Nevertheless, it can be applied to values of more than one type as long as those types stand in a subtype relationship to the fixed Base type which is specified in the method signature — in other words, this method is both parametrically monomorphic and subtype polymorphic.


    scala> def head[A](xs: List[A]): A = xs(0)
    head: [A](xs: List[A])A
    scala> head(1 :: 2 :: Nil)
    res0: Int = 1
    scala> case class Car(make: String)
    defined class Car
    scala> head(Car("Civic") :: Car("CR-V") :: Nil)
    res1: Car = Car(Civic)

---

!scala
trait Plus[A] {
  def plus(a2: A): A
}

---

scala> def plus[A <: Plus[A]](a1: A, a2: A): A = a1.plus(a2)
plus: [A <: Plus[A]](a1: A, a2: A)A

---

We can now provide different definitions of plus for A.

But, this is not flexible since trait Plus needs to be mixed in at the time of defining the datatype. So it can’t work for Int and String.

We will see how to extend this w/ type classes.

---

#Ad-hoc Polymorphism [link](https://en.wikipedia.org/wiki/Ad_hoc_polymorphism)

The term ad hoc in this context is not intended to be pejorative; it refers simply to the fact that this type of polymorphism is not a fundamental feature of the type system.

This is in contrast to parametric polymorphism, in which polymorphic functions are written without mention of any specific type, and can thus apply a single abstract implementation to any number of types in a transparent way.

---

The third approach in Scala is to provide an implicit conversion or implicit parameters for the trait.

    scala> trait Plus[A] {
             def plus(a1: A, a2: A): A
           }
    defined trait Plus
    scala> def plus[A: Plus](a1: A, a2: A): A = implicitly[Plus[A]].plus(a1, a2)
    plus: [A](a1: A, a2: A)(implicit evidence$1: Plus[A])A

---

This is truely ad-hoc in the sense that

* we can provide separate function definitions for different types of A
* we can provide function definitions to types (like Int) without access to its source code
* the function definitions can be enabled or disabled in different scopes

---

#View and Context Bounds

<!--- http://stackoverflow.com/questions/4465948/what-are-scala-context-and-view-bounds -->

---

    !scala
    def stackSort[A <% Ordered[A]](list: List[A]): List[A] =
      list.foldLeft(List[A]()) { (r,c) =>
        val (front, back) = r.partition(_ > c)
        front ::: c :: back
      }

---

Context bounds are mainly used in what has become known as the `Typeclass` pattern, as a reference to Haskell's type classes.
$$
$$
The `Typeclass` pattern implements an alternative to inheritance by making functionality available through an implicit adapter pattern.

---

Anatomy of a Type Class

There are three important components to the type class pa ern:

the type class itself,
instances for par cular types,
and the interface methods that we expose to users.

---

The type class itself is a generic type that represents the func onality we want to implement.

For example, we can represent generic “serialize to JSON” behaviour as a generic trait.

// Define a very simple JSON AST
sealed trait Json
// defined trait Json
final case class JsObject(get: Map[String,Json]) extends Json // defined class JsObject
final case class JsString(get: String) extends Json // defined class JsString
final case class JsNumber(get: Double) extends Json // defined class JsNumber
// The "serialize to JSON" behavior is encoded in this trait
trait JsonWriter[A] {
def write(value: A): Json
 }

---

The instances of a type class provide implementa ons for the types we care about, including standard Scala types and types from our domain model.

We define instances by crea ng concrete implementa ons of the type class and tagging them with the im- plicit keyword:

 final case class Person(name: String, email: String) // defined class Person
object DefaultJsonWriters {
implicit val stringJsonWriter = new JsonWriter[String] {
def write(value: String): Json = JsString(value)
}
implicit val personJsonWriter = new JsonWriter[Person] {
def write(value: Person): Json =
JsObject(Map("name" -> JsString(value.name), "email" -> JsString(value.email)))
}
// etc...
}

---

An interface is any func onality we expose to users. Interfaces to type classes are generic methods that accept instances of the type class as implicit parameters.

There are two common ways of specifying an interface: Interface Objects and Interface Syntax.

---
Interface Objects
The simplest way of crea ng an interface is to place the interface methods in a singleton object.
 object Json {
def toJson[A](value: A)(implicit writer: JsonWriter[A]): Json =
    writer.write(value)
}


 import DefaultJsonWriters._
// import DefaultJsonWriters._
val json: Json = Json.toJson(Person("Dave", "dave@example.com"))
// json: Json = JsObject(Map(name -> JsString(Dave), email -> JsString(dave@example.com)))

---

Interface Syntax

As an alterna ve, we can use type enrichment to extend exis ng types with interface methods1. Cats refers to this as “syntax” for the type class:

object JsonSyntax {
implicit class JsonWriterOps[A](value: A) {
def toJson(implicit writer: JsonWriter[A]): Json = { writer.write(value)
} }
}

---

We use interface syntax by impor ng it along-side the instances for the types we need:
import DefaultJsonWriters._
import JsonSyntax._

val json: Json = Person("Dave", "dave@example.com").toJson
 JsString(dave@example.com)))

---

#Application: Insertion Sort

    !scala
    def insertionSort[A : Ordering](list: List[A]): List[A] =
      list.foldLeft(List[A]()) { (r,c) =>
        val (front, back) = r.partition(implicitly[Ordering[A]].lt(c, _))
        front ::: c :: back
      }

---


#Application: `sum`

<!--- http://eed3si9n.com/learning-scalaz/sum+function.html -->

---


#Application: Thrush Combinator

---

In his classic book [To Mock a Mockingbird](http://www.amazon.com/Mock-Mockingbird-Other-Logic-Puzzles/dp/0192801422), Raymond Smullyan teaches combinatory logic using the analogy of songbirds in a forest.

More than the implementation, the bird names have gone a long way in establishing a common vocabulary of programming idioms and techniques.

---

The Thrush combinator is defined by the following condition: `Txy = yx`.

.notes: ie Thrush reverses the order of evaluation.

---

The following code is correct but difficult to read:

    !scala
    ((x: Int) => (x * x))((1 to 100).filter(_ % 2 != 0).foldLeft(0)(_+_))

---

    !scala
    case class Thrush[A](x: A) {
      def into[B](g: A => B): B = {
        g(x)
      }
    }

---

    !scala
    Thrush((1 to 100)
      .filter(_ % 2 != 0)
      .foldLeft(0)(_ + _))
      .into((x: Int) => x * x)

---

    !scala
    implicit def int2Thrush(x: Int) = Thrush(x)
    (1 to 100)
      .filter(_ % 2 != 0)
      .foldLeft(0)(_ + _)
      .into((x: Int) => x * x)

---

This comes in handy for designing expressive domain APIs and data pipelines:

    !scala
    accounts.filter(_ belongsTo "John S.")
            .map(_.calculateInterest)
            .filter(_ > threshold)
            .foldLeft(0)(_ + _)
            .into {x: Int =>
              updateBooks journalize(Ledger.INTEREST, x)
            }
