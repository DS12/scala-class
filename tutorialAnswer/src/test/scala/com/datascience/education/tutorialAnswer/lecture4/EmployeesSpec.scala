package com.datascience.education.tutorialAnswer.lecture4

import org.scalatest._
import org.scalatest.{FunSuite, Matchers}

import Employees._

import scala.Option
import scala.Some
import scala.None

// http://www.scalatest.org/user_guide/using_matchers

class EmployeesSpec extends FunSuite with Matchers {

//   type application is not allowed for postfix operators
//   test("Peter's email should return a Some") {
//     employeeEmail(peter) should be a [Some]
//   }


  test("Peter's email should be wrapped in a Some") {
    employeeEmail(peter.id) should be (Some(peter.email))
  }

  test("Chris's email should be a None") {
    employeeEmail(chrisId) should be (None)
  }



}


