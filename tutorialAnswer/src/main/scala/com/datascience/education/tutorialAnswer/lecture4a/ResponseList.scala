package com.datascience.education.tutorialAnswer.lecture4a

import scala.util.Random

import cats.data.Xor
import cats.data.Xor.Left
import cats.data.Xor.Right

object ResponseList {

  import RequestResponse._
  import TraverseXor._

  /*
   Given a List of possibly-corrupt Payloads,
   transform these into a single Xor containing a list of 
   intact Payloads.

   Then combine these payloads with the provided function.
   */

  val rand = new Random

  val listRequests = (1 to 10).map((i: Int) => Request(i.toString)).toList
  val listCorrupt = Request("foo") :: listRequests ::: List(Request("bar"), Request("5"))

  // Task 4a
  // answer
  def parsePayload(payload: Payload): Xor[NumberFormatException, Int] =
    Xor.catchOnly[NumberFormatException] {
      Integer.parseInt(payload.pay)
    }


  // Task 4b
  def pipeline(request: Request): Xor[Exception, Int] =
    client(request).flatMap(parsePayload)

  // Task 4c
  def sum(lr: List[Request]): XorException[Int] =
    traverse(lr)(pipeline).map { list =>
      list.foldLeft(0)(_ + _)
    }

}

object ResponseListExample extends App {
  import ResponseList._

  println("List of valid Requests:")
  println(listRequests)


  val sumValid = sum(listRequests)

  println(sumValid)

  println("----------------------")
  println("List of possibly corrupt Payloads:")
  println(listCorrupt)

  val sumCorrupt = sum(listCorrupt)

  println(sumCorrupt)

}
