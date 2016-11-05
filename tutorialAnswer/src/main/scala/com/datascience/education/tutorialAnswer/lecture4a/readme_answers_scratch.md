## Part X: `WebFormVerifier` with [`Validated`](http://typelevel.org/cats/tut/validated.html)

`code/labAnswers/src/main/scala/labAnswers/lecture4a/WebForm.scala`

`code/labAnswers/src/main/scala/labAnswers/lecture4a/WebFormVerifier.scala`

`code/labAnswers/src/main/scala/labAnswers/lecture4a/XorWebForm.scala`

`code/labAnswers/src/main/scala/labAnswers/lecture4a/ValidatedWebForm.scala`


We will analyze a web form verifier implemented with `Xor`, and then write an improved version with `Validated`, which is more suitable for this task.

Much wrapper code is necessary to fit `Xor` into this use case.

The defining difference between `Xor` and `Validated` is *failing-fast* versus *accumulation*.

`Xor` is intended for *chaining* a sequence of problematic operations -- any break of a link will prematurely terminate the sequence of operations.

`Validated` is intended to accumulate errors.


### (): Layout

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

and the verified implemented with `Validated` is 

```
object ValidatedWebFormVerifier
    extends WebFormVerifier[ValidatedNel[String, VerifiedWebForm]] {
	...
	}
```



```
def combine[EE >: E, AA >: A](that: Validated[EE, AA])
  (implicit EE: Semigroup[EE], AA: Semigroup[AA]): Validated[EE, AA]


Combine this Validated with another Validated, using the Semigroup instances of the underlying E and A instances. The resultant Validated will be Valid, if, and only if, both this Validated instance and the supplied Validated instance are also Valid.
```
[ScalaDoc](http://typelevel.org/cats/api/index.html#cats.data.Validated@combine[EE>:E,AA>:A](that:cats.data.Validated[EE,AA])(implicitEE:cats.Semigroup[EE],implicitAA:cats.Semigroup[AA]):cats.data.Validated[EE,AA])





[`Writer` in Scalaz Stream 0.8; Usage of `\/`](https://github.com/functional-streams-for-scala/fs2/blob/series/0.8/src/main/scala/scalaz/stream/package.scala#L48-L58)

[`\/` in Scalaz; implementation of `Either`](https://oss.sonatype.org/service/local/repositories/releases/archive/org/scalaz/scalaz_2.11/7.2.4/scalaz_2.11-7.2.4-javadoc.jar/!/index.html#scalaz.$bslash$div)


