package com.datascience.education.tutorial.lecture4

import scala.collection.Map

import java.util.UUID

object Employees  {


  type Email = String

  case class Employee(id: UUID, firstName: String, email: Email, ssnLast4: Short)

  val prianna = Employee(UUID.randomUUID(), "Prianna", "prianna@datascience.com", 8765)
  val peter = Employee(UUID.randomUUID(), "Peter", "peter@datascience.com", 9876)

  val chrisId = UUID.randomUUID()

  type Employees = Map[UUID, Employee]
  val employees: Employees = Map[UUID, Employee](prianna.id -> prianna, peter.id -> peter)

  /*
   Alternative problem description

   Implement the function "employeeEmail(id: UUID): Option[Email]",
   which returns an e-mail address if an employee exists who has the provided ID.

   Use the "get" method on Map.
   http://www.scala-lang.org/api/current/index.html#scala.collection.Map@get(key:A):Option[B]

   */

  // Task (3a)
  def employeeEmail(id: UUID): Option[Email] =
    ???

}

object EmployeesExample extends App {
  import Employees._

  val priannaEmail = employeeEmail(prianna.id)
  println(s"Prianna's email: $priannaEmail")
  val peterEmail = employeeEmail(peter.id)
  println(s"Peter's email: $peterEmail")
  val chrisEmail = employeeEmail(chrisId)
  println(s"Chris' email: $chrisEmail")

}
