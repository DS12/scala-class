package com.datascience.education.tutorial.lecture4a

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
    ???

  def minimumLength(minLength: Int)(string: String): XorErrors[String, String] =
    ???

  /*
   This version of `validateName` runs two checks on `name: String` --
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
    ???
  }


  // http://stackoverflow.com/a/19590634/1007926
  def numDigits(num: Long, length: Int): XorErrors[String, Long] =
    ???

  def verifyPhoneNumber(phoneNumber: Long): XorErrors[String, Long] = {
    ???
  }

  /*
   Email address validation
   " What is the best Java email address validation method? "
   http://stackoverflow.com/a/624590/1007926
   https://commons.apache.org/proper/commons-validator/apidocs/org/apache/commons/validator/routines/EmailValidator.html
   */

  val validator: EmailValidator = EmailValidator.getInstance()

  def validEmailAddress(emailAddress: String): Boolean = ???

  def verifyEmailAddress(emailAddress: String): XorErrors[String, String] = {
    ???
  }



  def verify(unverifiedWebForm: UnverifiedWebForm):
      XorErrors[String, VerifiedWebForm] = {
    ???
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


