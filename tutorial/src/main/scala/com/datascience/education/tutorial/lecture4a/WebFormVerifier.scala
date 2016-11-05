package com.datascience.education.tutorial.lecture4a

trait WebFormVerifier[+P[_,_] <: Product] {

  private case class VerifiedWebFormImpl(
    firstName: String, lastName: String,
    phoneNumber: Long, email: String) extends VerifiedWebForm

  protected def constructVerifiedWebForm(
    firstName: String, lastName: String,
    phoneNumber: Long, email: String): VerifiedWebForm =
    VerifiedWebFormImpl(firstName, lastName, phoneNumber, email)

  def isAlpha(string: String): P[String, String]
  
  def minimumLength(minLength: Int)(string: String): P[String, String]

  def verifyName(name: String): P[String,String]

  def numDigits(num: Long, length: Int): P[String, Long]

  def verifyPhoneNumber(phoneNumber: Long): P[String, Long]

  def verifyEmailAddress(emailAddress: String): P[String, String]


  def verify(unverifiedWebForm: UnverifiedWebForm): P[String, VerifiedWebForm]

}
