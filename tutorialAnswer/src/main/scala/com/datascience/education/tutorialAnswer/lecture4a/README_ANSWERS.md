# [`Xor`](http://typelevel.org/cats/api/index.html#cats.data.Xor) Lab - Answers


-----------------------------------

## Part 1: Introducing `Xor`

`Xor`, also known as `Either`, is an error-handling datatype like `Option`.  Its primary difference is that it can carry information other than `None` in its failure case.  `Option` has one generic type; `Xor` has two.  The type of this failure information fills generic `A` in `Left` and in `Xor`, below.

```
sealed abstract class Xor[+A, +B] extends Product with Serializable

final case class Left[+A](a: A) extends Xor[A, Nothing] with Product with Serializable

final case class Right[+B](b: B) extends Xor[Nothing, B] with Product with Serializable

```

Compare with `Option`

```
trait Option[+A] //base trait

case class Some[+A](get: A) extends Option[A]

case object None extends Option[Nothing]
```


[Read the official Cats `Xor` documentation](http://typelevel.org/cats/tut/xor.html)


The left side of `Xor` is *typically* an `Exception` of some form, and is usually intended to be the "end of the chain."  To prioritize typical usage, combinators on `Xor` are "right-biased."

Nevertheless, sometimes we *do* want to operate on the "left" type.  [`leftMap` exists for this purpose.](http://typelevel.org/cats/api/index.html#cats.data.Xor@leftMap[C](f:A=>C):cats.data.Xor[C,B])

```
def leftMap[C](f: (A) ⇒ C): Xor[C, B]
```

The implementation of `Xor` in the Scala STL, [named `Either`](http://www.scala-lang.org/api/current/index.html#scala.util.Either), is not left or right biased.  Call `left` or `right` on this `Either` to produce a `LeftProjection` or `RightProjection`.  This implementation adds boilerplate for the majority of use cases, most operatings being on the "right" type.


`Xor` is best suited to *sequential* dependencies.


```
	val xorA: Xor[Exception, A] = ...
	def produceB(a: A): Xor[Exception, B] = ...
	def produceC(b: B): Xor[Exception, C] = ...

	val xorD: Xor[Exception, C] = 
	  xorA.flatMap { a =>
	    produceB(a).map { b =>
		  produceC(b)
	    }
      }

```


[`Xor` ScalaDoc](http://typelevel.org/cats/api/index.html#cats.data.Xor)

[`Left` ScalaDoc](http://typelevel.org/cats/api/index.html#cats.data.Xor$@Left[+A]extendsXor[A,Nothing]withProductwithSerializable)

[`Right` ScalaDoc](http://typelevel.org/cats/api/index.html#cats.data.Xor$@Right[+B]extendsXor[Nothing,B]withProductwithSerializable)


```

sealed abstract class Xor[+A, +B] extends Product with Serializable {
  ...
  def flatMap[AA >: A, D](f: B => Xor[AA, D]): Xor[AA, D] = this match {
    case l @ Xor.Left(_) => l:  Left[A]   =:=    Xor[ A , Nothing ]
    case Xor.Right(b) => f(b): Xor[AA, D]
  }
  ...

}


```

-----------------------------------

## Part 2: Safe Division with `Xor`

`code/labAnswers/src/main/scala/labAnswers/lecture4a/SafeDivision.scala`

We re-implement our safe division exercises from Tutorial 4, replacing `Option` with `Xor`.


```
  def safeDivInt(numerator: Int, denominator: Int): Option[Int] =
    try {
      Some(numerator / denominator)
    } catch {
      case ae: java.lang.ArithmeticException => None
    }

  def squareRootFrac(numerator: Int, denominator: Int): Option[Double] =
    safeDivInt(numerator, denominator).flatMap { _ =>
      val squareRoot: Double =
        math.sqrt(numerator.toDouble / denominator)
      if (squareRoot.isNaN)
        None
      else
        Some(squareRoot)
    }


  def squareRootFrac(tup: (Int, Int)): Option[Double] =
    squareRootFrac(tup._1, tup._2)
```

`squareRootFrac` is intended to take the square root of a fraction.

`safeDivInt` will catch a divide-by-zero error (`java.lang.ArithmeticException`), and `squareRootFrac` will catch any undefined number (`Double.NaN`) produced by `math.sqrt` -- usually by a negative argument.


Which of these errors occurs is lost information -- `Option`'s `None` is not informative.

`Xor` will be more informative.

### Task (2a): `SafeDivisionXor.safeDivInt`

Implement

```
  def safeDivInt(numerator: Int, denominator: Int): Xor[Exception, Int] = ...
```

Test your implementation with `SafeDivIntXorExamples`.

#### Answer
```
  def safeDivInt(numerator: Int, denominator: Int): Xor[Exception, Int] =
    try {
      Right(numerator / denominator)
    } catch {
      case ae: java.lang.ArithmeticException =>
        Left(ae)
    }
	
  // alternative answer
  def safeDivInt2(numerator: Int, denominator: Int): Xor[Exception, Int] =
    Xor.catchOnly[Exception](numerator / denominator)
	
```
### Task (2b): `SafeDivisionXor.squareRootFrac`

Implement 

```
  def squareRootFrac(numerator: Int, denominator: Int): Xor[Exception, Double] = ...
```

Test your implementation with `SquareRootFracXorExamples`.

#### Answer 
```
  def squareRootFrac(numerator: Int, denominator: Int):
      Xor[Exception, Double] =
    safeDivInt(numerator, denominator).flatMap { _ =>
      val squareRoot: Double =
        math.sqrt(numerator.toDouble / denominator)
      if (squareRoot.isNaN)
        Left(new Exception("Square root is undefined"))
      else
        Right(squareRoot)
    }
```

-----------------------------------

## Part 3: Mock Client

`code/labAnswers/src/main/scala/labAnswers/lecture4a/RequestResponse.scala`


`RequestResponse` contains side-effecting code that resembles poor Java usage.  In Java and Scala, it is permitted to throw `Exception`s or `Throwable`s and leave them unhandled ([in Java-speak, unchecked](http://crunchify.com/better-understanding-on-checked-vs-unchecked-exceptions-how-to-handle-exception-better-way-in-java/)).  Lacking programmer directives on how to handle these unchecked exceptions, even trivial exceptions may crash the program in a run-time error -- the one-size-fits-all solution to exception handling.

[Equally dangerous is catching the *wrong* `Exception`s -- too wide of a net.](http://stackoverflow.com/questions/6083248/is-it-a-bad-practice-to-catch-throwable)  The rule of thumb is "Don't catch fatal exceptions -- let the JVM deal with them."  This explains method [`Xor.catchNonFatal`.](http://typelevel.org/cats/api/index.html#cats.data.Xor$@catchNonFatal[A](f:=>A):cats.data.Xor[Throwable,A])



### Task (3a): implement `sendRequest`
Use [`Xor.catchOnly`](http://typelevel.org/cats/api/index.html#cats.data.Xor$@catchOnly[T>:Null<:Throwable]:XorFunctions.this.CatchOnlyPartiallyApplied[T]) to "catch only" `BadRequestExceptions` in `sendRequestUnsafe`.

### Task (3b): implement `unpackResponse`

Use [`Xor.catchOnly`](http://typelevel.org/cats/api/index.html#cats.data.Xor$@catchOnly[T>:Null<:Throwable]:XorFunctions.this.CatchOnlyPartiallyApplied[T]) to "catch only" `CorruptPayloadException`s in `unpackResponseUnsafe`.

### Task (3c): implement `client`

Use `flatMap` on `Xor` to chain together `sendRequest` and `unpackResponse`.


```
sealed abstract class Xor[+A, +B] extends Product with Serializable {
  ...
  def flatMap[AA >: A, D](f: (B) ⇒ Xor[AA, D]): Xor[AA, D]
  ...
}
```

Test the catching of these exceptions with `RequestResponseExample`.  The testing code relies on a random number generator, so run it a few times to see both `BadRequestException` and `CorruptPayloadException` occur.

Tweak the thresholds in `sendRequestUnsafe` and `unpackResponseUnsafe` to test your error handling.

```
    if (rand.nextDouble() < 0.02) ...
```

#### Subtask
In your comments, name the concrete types of generics `A`, `B`, `AA`, and `D` in your usage of `flatMap`.



#### Answer

```
  def sendRequest(request: Request):
      Xor[BadRequestException, Response] =
    Xor.catchOnly[BadRequestException](sendRequestUnsafe(request))

  // Task
  def unpackResponse(response: Response)(outOfMemory: Boolean):
      Xor[CorruptPayloadException, Payload] =
    Xor.catchOnly[CorruptPayloadException](
      unpackResponseUnsafe(response, outOfMemory)
    )

  // Task
  def client(request: Request, outOfMemory: Boolean = false):
      Xor[ClientException, Payload] =
    sendRequest(request).flatMap(unpackResponse(_)(outOfMemory))

```
```
A = BadRequestException

B = Response

AA = ClientException

D = Payload
```
```
[info] Running labAnswers.lecture4a.RequestResponseExample 
------------------
sending request: Request(K8bTe8KR)
Right(Payload(Request for K8bTe8KR received and fulfilled.))
------------------
sending request: Request(5YPLQYeD)
Right(Payload(Request for 5YPLQYeD received and fulfilled.))
------------------
sending request: Request(6IurMOw4)
Right(Payload(Request for 6IurMOw4 received and fulfilled.))
------------------
sending request: Request(KaulKwxF)
Right(Payload(Request for KaulKwxF received and fulfilled.))
------------------
sending request: Request(97LbgjRN)
Right(Payload(Request for 97LbgjRN received and fulfilled.))
------------------
sending request: Request(DnWPGkfO)
Left(labAnswers.lecture4a.RequestResponse$CorruptPayloadException)
------------------
sending request: Request(GFbqA29Q)
Left(labAnswers.lecture4a.RequestResponse$BadRequestException)
------------------
sending request: Request(XnlpjgvI)
Right(Payload(Request for XnlpjgvI received and fulfilled.))


```


###  Task (3d): Test out-of-memory error

Uncomment the block of code under "Catastrophic exception should not be caught".

Run `RequestResponseExample` again, and ensure that a run-time exception is thrown.  This verifies that `client` is not a "catch-all" exception handler.

[`new Exception("out of memory")` is a mock of the real `OutOfMemoryError` in Java.](http://stackoverflow.com/questions/511013/how-to-handle-outofmemoryerror-in-java)


Read section "Xor in the small, Xor in the large" of [the official Cats Xor tutorial](http://typelevel.org/cats/tut/xor.html) to see what happens when two `Xor`s of unrelated `Left` types are `flatMap`ped together.  tldr; the compiler reverts to the most recent common ancestor.



Comment:

The terms *synchronous* and *asynchronous* are commonly referred to in discussions of networked processes.  

The earliest HTTP frameworks were sychronous.  The most primitive server of a request-response cycle is a simple for-loop -- each iteration of the loop handles one request and one response.  A web application developer using this primitive HTTP framework would think of their server process at this level.

Modern HTTP frameworks [support asynchrony between a request and its response, in one form or another.](http://stackoverflow.com/questions/5971301/determining-synchronous-vs-asynchronous-in-web-applications)

The distinction between synchronous and asynchronous actually depends on perspective; to the perspective of the code implementing any HTTP framework, a request-response cycle is synchronous.  To the perspective of a web application built on top an asynchronous HTTP framework, a request-response cycle is asynchronous.

Analogously, a threading library for a single-core machine is single-threaded from the perspective of its implementation.

`sendRequest` is not a mock server, but a mock client.  A client may be implemented to be synchronous or asynchronous to the perspective of its user.

`sendRequest` is a mock synchronous client.


#### Answer
```
Catastrophic exception should not be caught
[error] (run-main-5) java.lang.Exception: out of memory
java.lang.Exception: out of memory
	at labAnswers.lecture4a.RequestResponse$.unpackResponseUnsafe(RequestResponse.scala:50)
	at labAnswers.lecture4a.RequestResponse$$anonfun$unpackResponse$1.apply(RequestResponse.scala:74)
	at labAnswers.lecture4a.RequestResponse$$anonfun$unpackResponse$1.apply(RequestResponse.scala:74)
	at cats.data.XorFunctions$CatchOnlyPartiallyApplied.apply(Xor.scala:301)
```

-----------------------------------

## Part 4: Combining Payloads

`code/labAnswers/src/main/scala/labAnswers/lecture4a/ResponseList.scala`

### Task (4a): `parsePayload`

Implement ...  Complete its signature.

Read the [JavaDoc for `Integer.parseInt`](https://docs.oracle.com/javase/7/docs/api/java/lang/Integer.html#parseInt(java.lang.String)) and determine the type of `Exception` that `parseInt` throws.

### Task (4b): `pipeline`

Implement `pipeline`, which chains together `client` from the previous <b>Part</b> and `parsePayload`.  Once again, use `flatMap` on `Xor`.  Complete `pipeline`s signature.

```
sealed abstract class Xor[+A, +B] extends Product with Serializable {
  ...
  def flatMap[AA >: A, D](f: (B) ⇒ Xor[AA, D]): Xor[AA, D]
  ...
}
```

#### Subtask

Once again, name the concrete types of generics `A`, `B`, `AA`, and `D`.  This will be useful for completing the signature of `pipeline`.


#### Answer

```
  def parsePayload(payload: Payload): Xor[NumberFormatException, Int] =
    Xor.catchOnly[NumberFormatException] {
      Integer.parseInt(payload.pay)
    }


  // Task
  def pipeline(request: Request): Xor[Exception, Int] =
    client(request).flatMap(parsePayload)


A = ClientException
B = Payload
AA = Exception
D = Int
```


### Task (4c): `sum`

Implement `sum` with method `TraverseXor.traverse`, `map`, and `pipeline`.

```
	def sum(lr: List[Request]): XorException[Int] = ???
```

`sum` should add the Integers parsed by `pipeline`.  If any Integer/Response is corrupted, the summation fails.

Test your implementation with `ResponseListExample`.

Tweak the thresholds in `sendRequestUnsafe` and `unpackResponseUnsafe` to test your error handling.

```
    if (rand.nextDouble() < 0.02) ...
```


#### Answer 
```
  // Task
  def sum(lr: List[Request]): XorException[Int] =
    traverse(lr)(pipeline).map { list =>
      list.foldLeft(0)(_ + _)
    }
```

```
[info] Running labAnswers.lecture4a.ResponseListExample 
List of valid Requests:
List(Request(1), Request(2), Request(3), Request(4), Request(5), Request(6), Request(7), Request(8), Request(9), Request(10))
Right(55)
----------------------
List of possibly corrupt Payloads:
List(Request(foo), Request(1), Request(2), Request(3), Request(4), Request(5), Request(6), Request(7), Request(8), Request(9), Request(10), Request(bar), Request(5))
Left(java.lang.NumberFormatException: For input string: "foo")


```



-----------------------------------

## Part 5: `WebFormVerifier` with `Xor`

`code/labAnswers/src/main/scala/labAnswers/lecture4a/WebForm.scala`

`code/labAnswers/src/main/scala/labAnswers/lecture4a/WebFormVerifier.scala`

`code/labAnswers/src/main/scala/labAnswers/lecture4a/XorWebForm.scala`

`code/labAnswers/src/main/scala/labAnswers/lecture4a/TraverseXor.scala`


The defining difference between `Xor` and `Validated` is *failing-fast* versus *accumulation*.

`Xor` is intended for *chaining* a sequence of problematic operations -- any break of a link will prematurely terminate the sequence of operations.

`Validated` is intended to accumulate errors.


### (5a): Layout

An attempt is made to prevent `VerifiedWebForm` from being created in any place.  This would make it too easy for another programmer to skip the verification step, and convert `UnverifiedWebForm` directly to `VerifiedWebForm`.

```
trait WebForm {
  def firstName: String
  def lastName: String
  def phoneNumber: Long
  def email: String
}

case class UnverifiedWebForm(firstName: String, lastName: String,
  phoneNumber: Long, email: String) extends WebForm

trait VerifiedWebForm extends WebForm
```

The constructor for `VerifiedWebForm` resides in trait `WebFormVerifier`.

```
trait WebFormVerifier[P <: Product] {

  private case class VerifiedWebFormImpl(
    firstName: String, lastName: String,
    phoneNumber: Long, email: String) extends VerifiedWebForm

  protected def constructVerifiedWebForm(
    firstName: String, lastName: String,
    phoneNumber: Long, email: String): VerifiedWebForm =
    VerifiedWebFormImpl(firstName, lastName, phoneNumber, email)

  def verify(unverifiedWebForm: UnverifiedWebForm): P

}
```

The verifier implemented in `Xor` is 

```
object XorWebFormVerifier
    extends WebFormVerifier[XorErrors[String,VerifiedWebForm]] {
	...
	}
```

### Task (5b): `verify`

Review the given verification code in `XorWebFormVerifier`.

Implement `verify`, which ties these simpler verifiers together.

```
  def verify(unverifiedWebForm: UnverifiedWebForm):
      XorErrors[String, VerifiedWebForm] = ...
```

Test your implementation with `XorWebFormVerifierExample`.

#### Answer
```
  def verify(unverifiedWebForm: UnverifiedWebForm):
      XorErrors[String, VerifiedWebForm] = {

    val unverifiedFirstName: String = unverifiedWebForm.firstName
    val unverifiedLastName: String = unverifiedWebForm.lastName
    val unverifiedPhoneNumber: Long = unverifiedWebForm.phoneNumber
    val unverifiedEmail: String = unverifiedWebForm.email

    val xorVerifiedFirstName: XorErrors[String, String] =
      validateName(unverifiedFirstName)
    val xorVerifiedLastName: XorErrors[String, String] =
      validateName(unverifiedLastName)
    val xorVerifiedPhoneNumber: XorErrors[String, Long] =
      validatePhoneNumber(unverifiedPhoneNumber)
    val xorVerifiedEmail: XorErrors[String, String] =
      validateEmailAddress(unverifiedEmail)



    // Note the use of the case class constructor
    val xorVerifiedWebForm: XorErrors[String, VerifiedWebForm] =
      map4(xorVerifiedFirstName,
        xorVerifiedLastName,
        xorVerifiedPhoneNumber,
        xorVerifiedEmail)(constructVerifiedWebForm)

    xorVerifiedWebForm

  }
```

-----------------------------------

## Part 6: Introducing `Validated`

`Validated` is another error-handling datatype in Cats.  

A different description relates `Xor` to sequentiality and `Validated` to parallelism; and "Monad" to sequentiality and "Applicative Functor" to parallelism.

```
sealed abstract class Validated[+E, +A] extends Product with Serializable

final case class Invalid[+E](e: E) extends Validated[E, Nothing] with Product with Serializable

final case class Valid[+A](a: A) extends Validated[Nothing, A] with Product with Serializable

type ValidatedNel[E, A] = Validated[NonEmptyList[E], A]

type NonEmptyList[A] = OneAnd[List, A]

```

[`Validated` ScalaDoc](http://typelevel.org/cats/api/index.html#cats.data.Validated)

[`Invalid` ScalaDoc](http://typelevel.org/cats/api/index.html#cats.data.Validated$@Invalid[+E]extendsValidated[E,Nothing]withProductwithSerializable)

[`Valid` ScalaDoc](http://typelevel.org/cats/api/index.html#cats.data.Validated$@Valid[+A]extendsValidated[Nothing,A]withProductwithSerializable)

[`ValidatedNel` ScalaDoc](http://typelevel.org/cats/api/index.html#cats.data.package@ValidatedNel[E,A]=cats.data.Validated[cats.data.package.NonEmptyList[E],A])

[`NonEmptyList` ScalaDoc](http://typelevel.org/cats/api/index.html#cats.data.package@NonEmptyList[A]=cats.data.OneAnd[List,A])

[Cats Validation documentation](http://typelevel.org/cats/tut/validated.html), section "Of `flatMap`s and `Xor`s": 

>Sometimes the task at hand requires error-accumulation. However, sometimes we want a monadic structure that we can use for sequential validation (such as in a for-comprehension). This leaves us in a bit of a conundrum.

>Cats has decided to solve this problem by using separate data structures for error-accumulation (Validated) and short-circuiting monadic behavior (Xor).

><b>If you are trying to decide whether you want to use Validated or Xor, a simple heuristic is to use Validated if you want error-accumulation and to otherwise use Xor.</b>


-------------------------------

## Resources

[`Xor` in Cats](http://typelevel.org/cats/tut/xor.html)

[`Validated` in Cats](http://typelevel.org/cats/tut/validated.html)

[Easing Into Functional Error Handling in Scala](http://longcao.org/2015/06/15/easing-into-functional-error-handling-in-scala)

[Herding Cats: Xor](http://eed3si9n.com/herding-cats/Xor.html)

[Herding Cats: Validated](http://eed3si9n.com/herding-cats/Validated.html#Using+NonEmptyList+to+accumulate+failures)
