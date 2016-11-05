package com.datascience.education.tutorialAnswer.lecture4a

import cats.data.Xor
import cats.data.Xor.Left
import cats.data.Xor.Right
import cats.data.NonEmptyList
import org.apache.commons.validator.routines.EmailValidator
import XorErrors._

import scala.language.higherKinds

object XorWebFormVerifier
    extends WebFormVerifier[XorErrors] {

  //http://stackoverflow.com/a/19754142/1007926
  val alpha = (('a' to 'z') ++ ('A' to 'Z')).toSet

  def isAlpha(string: String): XorErrors[String, String] =
    if(string.forall(alpha.contains(_))) Right(string)
    else Left(NonEmptyList(s"String $string contains non-alpha characters"))

  def minimumLength(minLength: Int)(string: String): XorErrors[String, String] =
    if(string.length() >= minLength) Right(string)
    else Left(NonEmptyList(s"String $string shorter than minimum length $minLength"))

  /*
   This version of `verifyName` runs two checks on `name: String` --
   the string must only contain alphabetic characters,
   and the string must be at least 3 characters long.

   Here, "failing fast" means if `isAlpha` fails, 
   

  def verifyName(name: String): XorErrors[String, String] =
    isAlpha(name).flatMap { alphaName =>
      minimumLength(3)(alphaName)
    }
   */

  /*
   For first and last name
   */
  def verifyName(name: String): XorErrors[String, String] = {
    val alpha: XorErrors[String, String] = isAlpha(name)
    val isLength2: XorErrors[String, String] = minimumLength(2)(name)
    val combined: XorErrors[String, String] =
      map2(alpha, isLength2)((name1: String, name2: String) => name1)

    combined
  }


  // http://stackoverflow.com/a/19590634/1007926
  def numDigits(num: Long, length: Int): XorErrors[String, Long] =
    if(num.toString.length() == length) Right(num)
    else Left(NonEmptyList(s"Number $num does not have $length digits"))

  def verifyPhoneNumber(phoneNumber: Long): XorErrors[String, Long] = {
    val sevenDigits: XorErrors[String, Long] = numDigits(phoneNumber, 7)
    val tenDigits: XorErrors[String, Long] = numDigits(phoneNumber, 10)

    or(sevenDigits, tenDigits): XorErrors[String, Long]
  }

  /*
   Email address validation
   " What is the best Java email address validation method? "
   http://stackoverflow.com/a/624590/1007926
   https://commons.apache.org/proper/commons-validator/apidocs/org/apache/commons/validator/routines/EmailValidator.html
   */

  val validator: EmailValidator = EmailValidator.getInstance()

  def apacheEmailValidator(emailAddress: String): XorErrors[String, String] =
    if(validator.isValid(emailAddress))
      Right(emailAddress)
    else
      Left(NonEmptyList("Apache Commons EmailValidator validity test fails"))

  def emailHasPrefix(emailAddress: String): XorErrors[String, String] = 
    if(emailAddress.split('@').length == 2)
      Right(emailAddress)
    else
      Left(NonEmptyList("Email does not contain a single prefix."))

  // def verifyEmailAddress(emailAddress: String): XorErrors[String, String] = {
  //   if(validEmailAddress(emailAddress)) Right(emailAddress)
  //   else Left(NonEmptyList(s"Not a valid e-mail address: $emailAddress"))
  // }

  def verifyEmailAddress(emailAddress: String): XorErrors[String, String] =
    and(apacheEmailValidator(emailAddress), emailHasPrefix(emailAddress))




  // Task 5b
  def verify(unverifiedWebForm: UnverifiedWebForm):
      XorErrors[String, VerifiedWebForm] = {

    val unverifiedFirstName: String = unverifiedWebForm.firstName
    val unverifiedLastName: String = unverifiedWebForm.lastName
    val unverifiedPhoneNumber: Long = unverifiedWebForm.phoneNumber
    val unverifiedEmail: String = unverifiedWebForm.email

    val xorVerifiedFirstName: XorErrors[String, String] =
      verifyName(unverifiedFirstName)
    val xorVerifiedLastName: XorErrors[String, String] =
      verifyName(unverifiedLastName)
    val xorVerifiedPhoneNumber: XorErrors[String, Long] =
      verifyPhoneNumber(unverifiedPhoneNumber)
    val xorVerifiedEmail: XorErrors[String, String] =
      verifyEmailAddress(unverifiedEmail)



    // Note the use of the case class constructor
    val xorVerifiedWebForm: XorErrors[String, VerifiedWebForm] =
      map4(xorVerifiedFirstName,
        xorVerifiedLastName,
        xorVerifiedPhoneNumber,
        xorVerifiedEmail)(constructVerifiedWebForm)

    xorVerifiedWebForm

  }
}



object XorWebFormVerifierExample extends App {

  import XorWebFormVerifier._

  val unverified = UnverifiedWebForm("P3t3r", "Bec1ch", 1234, "no@suffix")

  val verified = verify(unverified)

  println("unverified: ")
  println(unverified)

  println("result of verification: ")
  println(verified)

  println("--------------------")

  val unverified2 = UnverifiedWebForm("P3t3r", "Bec1ch", 1234567890, "no@suffix")

  val verified2 = verify(unverified2)

  println("unverified: ")
  println(unverified2)

  println("result of verification: ")
  println(verified2)

  println("--------------------")

  val unverified3 = UnverifiedWebForm("Peter", "Becich", 1234567890, "peter@datascience.com")
  println("unverified: ")
  println(unverified3)

  val verified3 = verify(unverified3)

  println("result of verification: ")
  println(verified3)




}


