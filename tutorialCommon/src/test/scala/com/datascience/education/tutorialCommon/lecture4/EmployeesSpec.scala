package com.datascience.education.tutorialCommon.lecture4

import org.scalatest._
import org.scalatest.{FunSuite, Matchers}

import scala.Option
import scala.Some
import scala.None

import java.util.UUID

// http://www.scalatest.org/user_guide/using_matchers

import EmployeesTypeclass._

class EmployeesSpec[Emp, ES, Email](
  implicit et: Emp => EmployeeTypeclass[Email], ES: EmployeesTypeclass[Emp, Email]) extends FunSuite with Matchers {

  import ES._

  test("Peter's email should be wrapped in a Some") {
    employeeEmail(peter.id) should be (Some(peter.email))
  }

  test("Chris's email should be a None") {
    employeeEmail(chrisId) should be (None)
  }



}


