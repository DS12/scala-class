package com.datascience.education.tutorialAnswer.lecture4a

trait WebForm {
  def firstName: String
  def lastName: String
  def phoneNumber: Long
  def email: String
}

case class UnverifiedWebForm(firstName: String, lastName: String,
  phoneNumber: Long, email: String) extends WebForm

trait VerifiedWebForm extends WebForm


/*
 A new descendant of VerifiedWebForm, like VerifiedWebFormImpl,
 could leak out the privilege of created a VerifiedWebForm.
 */

