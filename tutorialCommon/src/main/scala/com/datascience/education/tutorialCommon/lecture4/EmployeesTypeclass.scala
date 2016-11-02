package com.datascience.education.tutorialCommon.lecture4

import java.util.UUID

/*
 With duplicate definitions of Email, Employee, 
 and the object that contains the exercises - Employees, 
 all three of these must be abstractly referenced in these test definitions.
 Employee and Employees both necessitate their own typeclass.

 */

trait EmployeeTypeclass[Email] {
  val id: UUID
  val email: Email
}

abstract class EmployeesTypeclass[Employee, Email](
  implicit et: Employee => EmployeeTypeclass[Email]) {
  
  val prianna: Employee
  val peter: Employee
  val chrisId: UUID

  def employeeEmail(id: UUID): Option[Email]

}
