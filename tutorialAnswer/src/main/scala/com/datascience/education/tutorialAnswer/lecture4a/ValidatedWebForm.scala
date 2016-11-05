package com.datascience.education.tutorialAnswer.lecture4a

import cats._
import cats.std._
import cats.std.all._
import cats.implicits._
import cats.syntax.all._
import cats.std.list._
import cats.SemigroupK

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.data.NonEmptyList
//  http://typelevel.org/cats/api/index.html#cats.data.package@ValidatedNel[E,A]=cats.data.Validated[cats.data.package.NonEmptyList[E],A]
import cats.data.ValidatedNel

import org.apache.commons.validator.routines.EmailValidator

import scala.language.higherKinds

object ValidatedWebFormVerifier
    extends WebFormVerifier[ValidatedNel] {

  // http://typelevel.org/cats/tut/validated.html

  // implicit val semigroupNel = SemigroupK[NonEmptyList].algebra[String]

  // implicit val nelSemigroup: Semigroup[NonEmptyList[String]] =
  //   SemigroupK[NonEmptyList].algebra[String]

  val alpha = (('a' to 'z') ++ ('A' to 'Z')).toSet

  def isAlpha(string: String): ValidatedNel[String, String] =
    if(string.forall(alpha.contains(_)))
      Valid(string)
    else
      Invalid(NonEmptyList(s"String $string contains non-alpha characters"))

  def minimumLength(minLength: Int)(string: String): ValidatedNel[String, String] =
    if(string.length() >= minLength)
      Valid(string)
    else
      Invalid(NonEmptyList(s"String $string shorter than minimum length $minLength"))


  // http://eed3si9n.com/herding-cats/Validated.html#Using+NonEmptyList+to+accumulate+failures



  def verifyName(name: String): ValidatedNel[String, String] = {
    val alpha: ValidatedNel[String, String] = isAlpha(name)
    val isLength2: ValidatedNel[String, String] = minimumLength(2)(name)

    //alpha.toValidatedNel.combine(isLength2.toValidatedNel)

    ???

  }
  


  def numDigits(num: Long, length: Int): ValidatedNel[String, Long] =
    if(num.toString.length() == length) Valid(num)
    else Invalid(NonEmptyList(s"Number $num does not have $length digits"))

  def verifyPhoneNumber(phoneNumber: Long):
      ValidatedNel[String, Long] = {
    val sevenDigits: ValidatedNel[String, Long] = numDigits(phoneNumber, 7)
    val tenDigits: ValidatedNel[String, Long] = numDigits(phoneNumber, 10)

    // until `ValidatedExtra.or` is fixed, `orElse` will be used.
    // Invalid("foo").toValidatedNel.orElse(Invalid("bar").toValidatedNel) will *loose*
    // "foo", unfortunately.  A proper `or` method would combine "foo" and "bar" given
    // a Semigroup[NonEmptyList]

    sevenDigits.orElse(tenDigits)
  }

  val validator: EmailValidator = EmailValidator.getInstance()
  def validEmailAddress(emailAddress: String): Boolean = validator.isValid(emailAddress)

  def verifyEmailAddress(emailAddress: String): ValidatedNel[String, String] = {
    if(validEmailAddress(emailAddress)) Valid(emailAddress)
    else Invalid(NonEmptyList("Not a valid e-mail address: $emailAddress"))
  }


  import cats.syntax.applicative._
  import cats.syntax.validated._

  type ValNelString[A] = ValidatedNel[String, A]
  implicit val appValNel: Apply[ValNelString] =
    new Apply[ValNelString] {
      def flatMap[A, B](fa: ValidatedNel[String, A])(
        f: A => ValidatedNel[String, B]): ValidatedNel[String, B] =
        fa match {
          case Valid(a)     => f(a)
          case i@Invalid(_) => i
        }

      def ap[A, B](vf: ValidatedNel[String, A => B])(va: ValidatedNel[String, A]):
          ValidatedNel[String, B] =
        flatMap(vf) { f =>
          va.map { a => f(a) }
        }
      def map[A,B](va: ValidatedNel[String, A])(f: A => B): ValidatedNel[String,B] =
        va.map(f)
    }


  def verify(unverifiedWebForm: UnverifiedWebForm):
      ValidatedNel[String, VerifiedWebForm] = {

    val unverifiedFirstName: String = unverifiedWebForm.firstName
    val unverifiedLastName: String = unverifiedWebForm.lastName
    val unverifiedPhoneNumber: Long = unverifiedWebForm.phoneNumber
    val unverifiedEmail: String = unverifiedWebForm.email

    val verifiedFirstName: ValidatedNel[String, String] =
      verifyName(unverifiedFirstName)
    val verifiedLastName: ValidatedNel[String, String] =
      verifyName(unverifiedLastName)
    val verifiedPhoneNumber: ValidatedNel[String, Long] =
      verifyPhoneNumber(unverifiedPhoneNumber)
    val verifiedEmail: ValidatedNel[String, String] =
      verifyEmailAddress(unverifiedEmail)
    

    //???
    // (verifiedFirstName |+| verifiedLastName |+| verifiedPhoneNumber |+| verifiedEmail)


    Apply[ValNelString].map4[String,String,Long,String,VerifiedWebForm](
      verifiedFirstName, verifiedLastName, verifiedPhoneNumber, verifiedEmail)(
    constructVerifiedWebForm)
  }




}
