# Subtyping, Variance, Typeclasses Tutorial - Answers

## Part 0

In this tutorial we aim to review, define or explain

* inheritance
* Multiple inheritance and the "diamond problem"
* composition / mix-ins
* implicit conversions
* type classes, and
* implicit parameters

ordered by level of abstraction.

----------------------------

## Part 1: [Simple Inheritance](https://en.wikipedia.org/wiki/Inheritance_(object-oriented_programming))

`code/tutorialAnswers/src/main/scala/tutorialAnswers/lecture3/Hierarchy.scala`

Inheritance is the simplest and earliest-developed pattern of object-oriented programming.

We will look inside the library [Json4s](https://github.com/json4s/json4s) to demonstrate inheritance.

The JSON format is specified by a grammar.  Here is a [simplified version](http://pythonhosted.org/pyrser/tutorial1.html#basic-json-parser) of its grammar:

```
json = [ object eof ]

object = [ '{' members? '}' ]

members = [ pair [',' pair]* ]

pair = [ string ':' value ]

value = [ string
        | number
        | object
        | array
        | "true"
        | "false"
        | "null"
     ]

array = [ '[' elements? ']' ]

elements = [ value [',' value]* ]
```

An example of a valid sentence of this grammar ([an example of JSON](https://en.wikipedia.org/wiki/JSON#Example)):

```
{
  "firstName": "John",
  "lastName": "Smith",
  "isAlive": true,
  "age": 25,
  "address": {
    "streetAddress": "21 2nd Street",
    "city": "New York",
    "state": "NY",
    "postalCode": "10021-3100"
  },
  "phoneNumbers": [
    {
      "type": "home",
      "number": "212 555-1234"
    },
    {
      "type": "office",
      "number": "646 555-4567"
    }
  ],
  "children": [],
  "spouse": null
}
```

With the grammar usage pointed out:

```
object
{
  pair = string : string
  "firstName": "John",

  pair = string : "true"
  "isAlive": true,
  
  pair = string : number
  "age": 25,
  
  pair = string : object
  "address": {

	pair = string : string
    "streetAddress": "21 2nd Street",
  
	pair = string : string
    "city": "New York",

	pair = string : string!
    "postalCode": "10021-3100"
  },
  
  pair = string : array
  "phoneNumbers": [
  
    value = object
    {
	
	  pair = string : string
      "type": "home",
	  
	  pair = string : string
      "number": "212 555-1234"
    },
    {
	 
	  pair = string : string
      "type": "office",
	  
	  pair = string : string
      "number": "646 555-4567"
    }
  ],
  
  pair = string : array
  "children": [],
  
  pair = string : null
  "spouse": null
}
```


The grammar allows for *recursion*.  We can construct some unwieldly - but valid - JSON:

```
{
  "name" : "Bert",
  "children" : [
    {
	  "name" : "Alice",
	  "children" : []
	},
	{ 
	  "name": "Bob",
	  "children" : [
	    {
		  "name" : "Bill",
		  "children" : []
	    },
		{
		  "name" : "Zoot",
		  "children" : []
	    }
	  ]
	}
  ]
}
```

Grammars and parsing will be explained in lecture 6.

JSON is a great fit for a hierarchical, recursive data structure.  Json4s [provides this](https://github.com/json4s/json4s#guide) ([source](https://github.com/json4s/json4s/blob/3.4/ast/src/main/scala/org/json4s/JsonAST.scala)):

```
sealed abstract class JValue
case object JNothing extends JValue // 'zero' for JValue
case object JNull extends JValue
case class JString(s: String) extends JValue
case class JDouble(num: Double) extends JValue
case class JDecimal(num: BigDecimal) extends JValue
case class JInt(num: BigInt) extends JValue
case class JLong(num: Long) extends JValue
case class JBool(value: Boolean) extends JValue
case class JObject(obj: List[JField]) extends JValue
case class JArray(arr: List[JValue]) extends JValue

type JField = (String, JValue)
```

This Part doubles as a preview of Lecture 9.

### Task (1a): Parsing with json4s

The two JSON examples above are stored in values `tutorials.lecture3.Hierarchy.json1` and `tutorials.lecture3.Hierarchy.json2`.

Use [this example](https://github.com/json4s/json4s#parsing-json) to parse `json1` and `json2`:


```
scala> import org.json4s._
scala> import org.json4s.native.JsonMethods._

scala> parse(""" { "numbers" : [1, 2, 3, 4] } """)
res0: org.json4s.JsonAST.JValue =
      JObject(List((numbers,JArray(List(JInt(1), JInt(2), JInt(3), JInt(4))))))

scala> parse("""{"name":"Toy","price":35.35}""", useBigDecimalForDouble = true)
res1: org.json4s.package.JValue = 
      JObject(List((name,JString(Toy)), (price,JDecimal(35.35))))
```

#### Answer (1a)

```
  val json1JValue: JValue = parse(json1)
  val json2JValue: JValue = parse(json2)
```

```


scala> import tutorialAnswers.lecture3.Hierarchy._
import tutorialAnswers.lecture3.Hierarchy._

scala> json1JValue
res1: org.json4s.JValue = JObject(List((firstName,JString(John)), (lastName,JString(Smith)), (isAlive,JBool(true)), (age,JInt(25)), (address,JObject(List((streetAddress,JString(21 2nd Street)), (city,JString(New York)), (state,JString(NY)), (postalCode,JString(10021-3100))))), (phoneNumbers,JArray(List(JObject(List((type,JString(home)), (number,JString(212 555-1234)))), JObject(List((type,JString(office)), (number,JString(646 555-4567))))))), (children,JArray(List())), (spouse,JNull)))

scala> json2JValue
res2: org.json4s.JValue = JObject(List((name,JString(Bert)), (children,JArray(List(JObject(List((name,JString(Alice)), (children,JArray(List())))), JObject(List((name,JString(Bob)), (children,JArray(List(JObject(List((name,JString(Bill)), (children,JArray(List())))), JObject(List((name,JString(Zoot)), (children,JArray(List()))))))))))))))

scala> 
```

-----------

## Part 2: Covariance and Contravariance

We are familiar with the rules below:

Given `T' <: T`,

```
	            Meaning	                        Scala notation
covariant	    C[T’] is a subclass of C[T]	    [+T]
contravariant	C[T] is a subclass of C[T’]	    [-T]
invariant	    C[T] and C[T’] are not related	[T]
```

[Twitter Scala School: Type & Polymorphism Basics](https://twitter.github.io/scala_school/type-basics.html#variance)

The slides gave an example:

```
abstract class OutputChannel[-A] {
  def write(x: A): Unit
}
```

"With that annotation, we have that OutputChannel[AnyRef] conforms to OutputChannel[String]. That is, a channel on which one can write any object can substitute for a channel on which one can write only strings."

Let's assume for the sake of argument that `Int` is a subtype of `Double` -- `Int <: Double`.

An `OutputChannel[Double]` should be able to substitute for an `OutputChannel[Int]` -- `OutputChannel[Double] <: OutputChannel[Int]`.

It turns out this is just part of a larger pattern, concerning inputs and outputs.  Equivalently, sinks and sources.

[Covariance and contravariance: Function types](https://en.wikipedia.org/wiki/Covariance_and_contravariance_(computer_science)#Function_types):

> Languages [like Scala] with first-class functions have function types like "a function expecting a Cat and returning an Animal" [written `Cat => Animal` or `Function1[Cat, Animal]` in Scala syntax].

>Those languages also need to specify when one function type is a subtype of another—that is, when it is safe to use a function of one type in a context that expects a function of a different type. It is safe to substitute a function `f` for a function `g` if `f` accepts a more general type of arguments and returns a more specific type than `g`. For example, a function of type `Cat=>Cat` can safely be used wherever a `Cat=>Animal` was expected, and likewise a function of type `Animal=>Animal` can be used wherever a `Cat=>Animal` was expected. (One can compare this to the [robustness principle of communication](https://en.wikipedia.org/wiki/Robustness_principle): *"be liberal in what you accept and conservative in what you produce"*). 


This explains the variance annotations of [`Function1`](http://www.scala-lang.org/api/2.11.8/#scala.Function1), [`Function2`](http://www.scala-lang.org/api/2.11.8/#scala.Function2) and so on.

The input to a function can be thought of as a *sink*.

The output of a function can be thought of as a *source*.

Is `write` in `OutputChannel[-A]` a source or a sink?

-------------------------------

## Part 3: [Type Linearization](https://www.safaribooksonline.com/library/view/programming-scala/9780596801908/ch07s05.html)

`code/tutorialAnswers/src/main/scala/tutorialAnswers/lecture3/TypeLinearization.scala`

Based off section 12.1 of *Programming in Scala*

Scala [does not allow multiple inheritance](http://stackoverflow.com/questions/9919021/can-a-scala-class-extend-multiple-classes).  Scala allows [mixin](https://en.wikipedia.org/wiki/Mixin)s:

> In object-oriented programming languages, a mixin is a class that contains methods for use by other classes without having to be the parent class of those other classes. How those other classes gain access to the mixin's methods depends on the language. Mixins are sometimes described as being "included" rather than "inherited".

Conventional multiple-inheritance allows for an ambiguity known as the [diamond problem](https://en.wikipedia.org/wiki/Multiple_inheritance#The_diamond_problem).  If multiple parents want to contribute to a descendant the same value or method, which to choose?

The ordering of mixins resolves this ambiguity.

#### Task (3a)

Set up class `Frog` so that it mixes-in traits `Amphibian` and `Philosophical`.  Give precedence to `Philosophical`'s `toString` method.


---------------------

## Part 4: The Liskov Substitution Principle

`code/tutorialAnswers/src/main/scala/tutorialAnswers/lecture3/Heirloom.scala`

The following program demonstrates a *misuse* of subtyping.

Proper subtyping must provide *substitutability*:

> Substitutability is a principle in object-oriented programming that states that, in a computer program, if S is a subtype of T, then objects of type T may be replaced with objects of type S (i.e. objects of type S may substitute objects of type T) without altering any of the desirable properties of that program (correctness, task performed, etc.).

[Liskov substitution principle](https://en.wikipedia.org/wiki/Liskov_substitution_principle)


We have a class heirarchy - a family tree.  `HeirloomTransition` is intended only function in one direction - from an ancestor to a descendant.

```
  class GreatGrandparent
  class Grandparent extends GreatGrandparent
  class Parent extends Grandparent
  class Child extends Parent

  case class HeirloomTransition[A, D <: A](ancestor: A, descendant: D)

  val grandParentToChild =
    new HeirloomTransition(new Grandparent, new Child)
```

The program works as intended up until this point.  Let's look inside `grandParentToChild` `HeirloomTransition`:

```

scala> grandParentToChild
res3: tutorialAnswers.lecture3.Heirloom.HeirloomTransition[tutorialAnswers.lecture3.Heirloom.Grandparent,tutorialAnswers.lecture3.Heirloom.Child] = HeirloomTransition(tutorialAnswers.lecture3.Heirloom$Grandparent@60d0e6b8,tutorialAnswers.lecture3.Heirloom$Child@3c9f3050)

scala> grandParentToChild.ancestor
res4: tutorialAnswers.lecture3.Heirloom.Grandparent = tutorialAnswers.lecture3.Heirloom$Grandparent@60d0e6b8

scala> grandParentToChild.descendant 
res5: tutorialAnswers.lecture3.Heirloom.Child = tutorialAnswers.lecture3.Heirloom$Child@3c9f3050

```

The type of `ancestor` is `Grandparent` and the type of `descendant` is `Child`.

### Task (4a): `childToGrandparent`

Run this code in your REPL:

```
  import Heirloom._

  val childToGrandparent =
    new HeirloomTransition(new Child, new Grandparent)
```

Investigate the inner types of `childToGrandparent` and explain why the code is allowed to compile.

Note that the sub- and supertype relationships are inclusive; `A <: A` and `B >: B` are true.  This affects the answer.


#### Answer (4a)

Let's look inside the `childToGrandparent` `HeirloomTransition`:

```
scala> childToGrandparent
res0: tutorialAnswers.lecture3.Heirloom.HeirloomTransition[tutorialAnswers.lecture3.Heirloom.Grandparent,tutorialAnswers.lecture3.Heirloom.Grandparent] = HeirloomTransition(tutorialAnswers.lecture3.Heirloom$Child@344e6879,tutorialAnswers.lecture3.Heirloom$Grandparent@5d026a2f)

scala> childToGrandparent.ancestor 
res1: tutorialAnswers.lecture3.Heirloom.Grandparent = tutorialAnswers.lecture3.Heirloom$Child@344e6879

scala> childToGrandparent.descendant 
res2: tutorialAnswers.lecture3.Heirloom.Grandparent = tutorialAnswers.lecture3.Heirloom$Grandparent@5d026a2f
```

The `Child` was cast to type `Grandparent`!  The compiler "raised" the type of `A` until the type invariant `D <: A` was satisfied.  Why would it do this?



This subtype relationship requires that each descendant can take the place of an ancestor if necessary.  This explains why the `Child` was cast to a `Grandparent`.

---------------------

## Part 5: Implicit Conversions

`code/tutorialAnswers/src/main/scala/tutorialAnswers/lecture3/ImplicitConversions.scala`

In Spark, `Vectors.dense` only accepts `Double`s as arguments.

```
def dense(firstValue: Double, otherValues: Double*): Vector

Creates a dense vector from its values.
```

[`Vectors` ScalaDoc](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.mllib.linalg.Vectors$)


`Int` can be given to `Vectors.dense` with the use of an implicit `intToDouble` conversion.  Just as [primitive instances of `Ordering` are provided by the Scala Library](http://www.scala-lang.org/api/current/index.html#scala.math.Ordering$), so are primitive implicit conversions.

An unrelated error of method ambiguity breaks this code, but the implicit `intToDouble` conversion has done its job.  Scala is *not* complaining, "`Found: Int, Required: Double`":

```
val data = rawData.map { line =>
    val values = line.split(',').map(_.toInt)
    val featureVector = Vectors.dense(values.init)
    val label = values.last - 1
    LabeledPoint(label, featureVector)
}


<console>:51: error: overloaded method value dense with alternatives:
  (values: Array[Double])org.apache.spark.mllib.linalg.Vector <and>
  (firstValue: Double,otherValues: Double*)org.apache.spark.mllib.linalg.Vector
 cannot be applied to (Array[Int])
           val featureVector = Vectors.dense(values.init)
```

Here is a type that does not have an implicit conversion provided by the Scala Library, but similar to `Vectors` from Spark:

```
  type ComplexNumber = (Double, Double)

  trait ComplexVector {
    def complexVector: List[ComplexNumber]
    override def toString = s"Vector contains $complexVector"
  }


  object ComplexVectors {
    def dense(firstValue: ComplexNumber, otherValues: ComplexNumber*): ComplexVector =
      new ComplexVector {
        val complexVector = firstValue :: otherValues.toList
      }
  }
```

We want to store *real* Integers in `ComplexVector`.

`ComplexNumber` isn't so complicated a type -- a tuple of `Double`s.  Both `Tuple2` and `Double` are primitive Scala types.

One of you has asked if Scala can make these two hops to resolve our problem, as it made the one hop to convert our `Int` to a `Double`.  It cannot -- see the "One-at-a-time" rule in [Rules for implicits](http://www.artima.com/pins1ed/implicit-conversions-and-parameters.html#21.2)


### Task (5a): implicit conversion

Define an implicit conversion so that this call compiles:

```
  val denseInts = ComplexVectors.dense(4, 2, 6, 9)
```

*Discover* where the implicit conversion should be placed.  It is a question of scope.  Consider: implicit conversions are primarily used for compatibility with an unmodifiable library.  The canonical example is a Java library that accepts Java types as arguments.  Modifying the library would be an anti-pattern -- modify the call to the library with an implicit conversion.  Implicit conversions help to convert to and from Java types: [`JavaConversions`](http://www.scala-lang.org/api/2.11.8/#scala.collection.JavaConversions$)

Do not change any given method signatures -- only add code.

Test your implicit conversion by compiling and running `ImplicitConversionsExample`.


#### Answer (5a)

```
  implicit def int2Complex(i: Int): ComplexNumber = (i.toDouble, 0.0)
```



--------------------------

## Part 6: Implicit Parameters and the Typeclass Pattern

`code/tutorialAnswers/src/main/scala/tutorialAnswers/lecture3/TypeClassProblem.scala`

Read this example: [Label-maker typeclass example](http://debasishg.blogspot.com/2010/06/scala-implicits-type-classes-here-i.html)


We have a class `Model`:

```
  class Model[Domain, Range](val pdf: Domain => Range) {

  }
```

We want to add the functionality of plotting (by printing to console) to `Model` without modifying `Model`.  We will use the Typeclass Pattern.

`Plottable` is the counterpart to `LabelMaker`.

While `LabelMaker` is generic on a single type `T`, `Plottable` must handle generic type `T`, generic type `Domain` and generic type `Range`.

`Plottable` must be usable by any type with two generics [(a higher-kinded type)](http://stackoverflow.com/questions/6246719/what-is-a-higher-kinded-type-in-scala).  That is, do not specialize `Plottable` to only plot `Model`s.

### Task (6a): `points`

Complete the implementation of typeclass `Plottable`.

`points` is an *abstract* method that will be implemented by each concrete instance of `Plottable`.  You need to complete its signature.

#### Answer (6a)

```
  trait Plottable[Domain, Range, T[Domain, Range]] {
    def points(t: T[Domain, Range], ld: List[Domain]): List[Range]

    import breeze.plot._

    def plot(name: String, t: T[Domain, Range], input: List[Domain]): Unit = {
      val y = points(t, input)
      println(s"$name: $y")
    }

  }
```

### Task (6b): `PlotDoubleDoubleModel`

Complete the implementation of `PlotDoubleDoubleModel`.

This is a concrete instance of `Plottable`.

#### Answer (6b)

```
  implicit object PlotDoubleDoubleModel extends Plottable[Double, Double, Model] {

    def points(mod: Model[Double, Double], ld: List[Double]): List[Double] =
      ld.map(mod.pdf)

  }
```

### Task (6c): `PlotDoubleDoubleFunction`

Complete the implementation of `PlotDoubleDoubleFunction`.

This is a concrete instance of `Plottable`.

#### Answer (6c)

```
  implicit object PlotDoubleDoubleFunction extends Plottable[Double, Double, Function1] {

    def points(func: Double => Double, ld: List[Double]): List[Double] =
      ld.map(func)

  }
```

### Task (6d): `plotter`

`plotter` is the counterpart to `printLabel`.  `plotter` will work with any class `T` of domain `D` and range `R` for which there is a `Plottable` implemented.

Implement `plotter`.

Test your implementations by running `PlotExample`.


Your output will look like this:

```
[info] Running tutorialAnswers.lecture3.PlotExample 
gaussianpdf.png: List(0.08568429602390368, 0.09132454269451096, 0.09709302749160648,
0.10296813435998739, 0.10892608851627526, 0.11494107034211652, 0.12098536225957168,
0.1270295282345945, 0.1330426249493774, 0.13899244306549824, 0.1448457763807414,
0.15056871607740221, 0.15612696668338066, 0.16148617983395713, 0.16661230144589984,
0.17147192750969195, 0.17603266338214976, 0.18026348123082397, 0.18413507015166167,
0.18762017345846896, 0.19069390773026207, 0.19333405840142462, 0.19552134698772794,
0.19723966545394447, 0.1984762737385059, 0.19922195704738202, 0.19947114020071635,
0.19922195704738202, 0.1984762737385059, 0.19723966545394447, 0.19552134698772797,
0.19333405840142462, 0.19069390773026204)
```
#### Answer (6d)

```
  def plotter[D,R,T[D,R]](t: T[D,R], ld: List[D], name: String)(implicit plottable: Plottable[D,R,T]): Unit =
    plottable.plot(name, t, ld)
```

---------------------

## Resources

[Composition over inheritance](https://en.wikipedia.org/wiki/Composition_over_inheritance)

[Polymorphism](https://en.wikipedia.org/wiki/Polymorphism_(computer_science))

[Inheritance versus Subtyping](https://en.wikipedia.org/wiki/Inheritance_(object-oriented_programming)#Inheritance_vs_subtyping)

[How do Traits in Scala avoid the “diamond error”?](http://programmers.stackexchange.com/questions/237115/how-do-traits-in-scala-avoid-the-diamond-error)

[How Scala Tames Multiple Inheritance](https://www.safaribooksonline.com/blog/2013/05/30/traits-how-scala-tames-multiple-inheritance/)

[When to use val or def in Scala traits?](http://stackoverflow.com/questions/19642053/when-to-use-val-or-def-in-scala-traits)

[Programming in Scala, 1st Edition: Contravariance](http://www.artima.com/pins1ed/type-parameterization.html#19.6)

[Why are Arrays invariant, but Lists covariant? tldr: Mutability](http://stackoverflow.com/questions/6684493/why-are-arrays-invariant-but-lists-covariant)

[Programming in Scala, 1st ed: How Scala checks variance annotations](http://www.artima.com/pins1ed/type-parameterization.html#19.4)

[Programming in Scala, 1st ed: Abstract Members](http://www.artima.com/pins1ed/abstract-members.html)

[Covariance and Contravariance in generic types of a Function](https://en.wikipedia.org/wiki/Covariance_and_contravariance_(computer_science)#Function_types)


[How are co- and contra-variance used in designing business applications?](http://stackoverflow.com/questions/5277526/how-are-co-and-contra-variance-used-in-designing-business-applications/5279436#5279436)

[How to Use Covariance and Contravariance to Build Flexible and Robust Programs](https://github.com/lambdaconf/lambdaconf-2016-usa/blob/master/How%20to%20Use%20Covariance%20and%20Contravariance%20to%20Build%20Flexible%20and%20Robust%20Programs/CovarianceContravarianceTalk.pdf)

[Ad Hoc Polymorphism in Scala With Type Classes](http://blog.jaceklaskowski.pl/2015/05/15/ad-hoc-polymorphism-in-scala-with-type-classes.html)

[Java's Interface and Haskell's type class: differences and similarities?](http://stackoverflow.com/questions/6948166/javas-interface-and-haskells-type-class-differences-and-similarities)

[Label-maker typeclass example](http://debasishg.blogspot.com/2010/06/scala-implicits-type-classes-here-i.html)

[Comments about "sub-typing" in Haskell](https://news.ycombinator.com/item?id=4784116)

[Polymorphism in Haskell; Another ad-hoc versus parametric polymorphism definition](https://wiki.haskell.org/Polymorphism)

[Advanced Typing](https://www.safaribooksonline.com/library/view/learning-scala/9781449368814/ch10.html)
