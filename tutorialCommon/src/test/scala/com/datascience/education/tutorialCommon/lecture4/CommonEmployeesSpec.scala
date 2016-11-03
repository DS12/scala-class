package com.datascience.education.tutorialCommon.lecture4

import org.scalatest.{FunSuite, Matchers}

/*
 The typeclass pattern may be useful to unify test implementations 
 between exercise and answer SBT sub-projects.

 The test is defined on a typeclass, which both 
 exercise and answer code implement -- see
 com.datascience.education.tutorialAnswer.lecture4.EmptySetSpec and
 com.datascience.education.tutorial.lecture4.EmptySetSpec
 in the test folder for a simpler example than `Employees`.

 `EmptySet` is too simple to demonstrate the complication of this attempt
 at test unification.
 `Employees` features a couple of custom types:
 the `Email` type, and the `Employee` case class.
 These are duplicately defined in the exercise and answer code,
 to make them immediately visible to the student.  We should not
 hide these simple types away in this `tutorialCommon` sub-project.
 While testing boilerplate may expand, I attempt to keep the exercises
 in `main` as compact as possible.  
 `test` code is generally my responsibility and a "given" for the student.

 Both this attempt and the monomorphism attempt at test unification 
 (see com.datascience.education.tutorialCommon.lecture4.FPOption)
 will be shelved for now.  Test implementations will simply be duplicated
 between the `tutorial` and `tutorialAnswer` sub-projects.

 */

class CommonEmployeesSpec[Employee, Employees, Email](
  implicit
    et: Employee => EmployeeTypeclass[Email], Employees: EmployeesTypeclass[Employee, Email]
) extends FunSuite with Matchers {

  import Employees._

  // http://www.scalatest.org/user_guide/using_matchers
  test("Peter's email should be wrapped in a Some") {
    employeeEmail(peter.id) should be (Some(peter.email))
  }

  test("Chris's email should be a None") {
    employeeEmail(chrisId) should be (None)
  }

}


