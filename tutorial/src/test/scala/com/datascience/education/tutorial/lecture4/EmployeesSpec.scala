package com.datascience.education.tutorial.lecture4

import org.scalatest._
import org.scalatest.{FunSuite, Matchers}

import scala.Option
import scala.Some
import scala.None

import java.util.UUID

import scala.language.implicitConversions

import Employees._

// http://www.scalatest.org/user_guide/using_matchers

import com.datascience.education.tutorialCommon.lecture4.EmployeesTypeclass
import EmployeesTypeclass._


object EmployeesSpec {

  implicit def employee(e: Employee): EmployeeTypeclass =
    new EmployeeTypeclass {
      val id: UUID = e.id
      val email: Email = e.email
    }

  implicit def employees: EmployeesTypeclass = new EmployeesTypeclass {
    def employeeEmail(id: UUID): Option[Email] =
      Employees.employeeEmail(id)
  }
}

import EmployeesSpec._



