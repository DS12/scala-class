package com.datascience.education.tutorial.lecture4a

import scala.util.Random

import cats.data.Xor
import cats.data.Xor.Left
import cats.data.Xor.Right

import java.lang.{ String => JString }

object RequestResponse {

  val rand = new Random

  def randString: String =
    rand.alphanumeric.take(8).toList.foldLeft("")(_ + _)


  trait ClientException extends Exception {
    def message: String
    def cause: Throwable
  } 


  class BadRequestException(val message: String = null, val cause: Throwable = null)
      extends ClientException

  class CorruptPayloadException(val message: String = null, val cause: Throwable = null)
      extends ClientException


  case class Request(req: String)
  case class Response(arr: Array[Byte])
  case class Payload(pay: String)


  def sendRequestUnsafe(request: Request): Response =
    if (rand.nextDouble() < 0.02)
      throw new BadRequestException(s"No response received for request: $request")
    else {
      val bytes = { request.req }.getBytes()

      Response(bytes)
    }

  def unpackResponseUnsafe(
    response: Response, outOfMemory: Boolean = false): Payload = {

    if(outOfMemory)
      throw new Exception("out of memory")


    if (rand.nextDouble() < 0.02)
      throw new CorruptPayloadException(s"Payload of response corrupted: $response")
    else {
      val pay = response.arr
      Payload(new JString(response.arr))
    }

  }
  def clientUnsafe(request: Request): Payload =
    unpackResponseUnsafe(sendRequestUnsafe(request))


  // Task 3a
  def sendRequest(request: Request):
      Xor[BadRequestException, Response] =
    ???

  // Task 3b
  def unpackResponse(response: Response)(outOfMemory: Boolean):
      Xor[CorruptPayloadException, Payload] =
    ???

  // Task 3c
  def client(request: Request, outOfMemory: Boolean = false):
      Xor[ClientException, Payload] =
    ???


}

object RequestResponseExample extends App {
  import RequestResponse._

  val requests = (1 to 8).map(_ => Request(randString))

  requests.foreach { request =>
    val payload: Xor[Throwable, Payload] = client(request)
    println("------------------")
    println(s"sending request: $request")
    println(payload)
  }


  println("-------------------------------")
  println("Catastrophic exception should not be caught")

  // Task 3d
  // requests.foreach { request =>
  //   val payload: Xor[Throwable, Payload] = client(request, true)
  //   println("------------------")
  //   println(s"sending request: $request")
  //   println(payload)
  // }


}

