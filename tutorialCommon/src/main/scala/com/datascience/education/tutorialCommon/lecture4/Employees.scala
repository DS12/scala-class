package com.datascience.education.tutorialCommon.lecture4

import java.util.UUID

object EmployeesTypeclass {

  //type Email = String

  //case class Employee(id: UUID, firstName: String, email: Email, ssnLast4: Short)

}

trait EmployeeTypeclass[Eml] {
  val id: UUID
  val email: Eml
}

abstract class EmployeesTypeclass[Emp, Email](
  implicit et: Emp => EmployeeTypeclass[Email]) {
  import EmployeesTypeclass._
  
  val prianna: Emp
  val peter: Emp
  val chrisId: UUID

  def employeeEmail(id: UUID): Option[Email]

}
