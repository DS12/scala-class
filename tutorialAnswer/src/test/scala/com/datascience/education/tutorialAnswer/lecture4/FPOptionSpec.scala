package com.datascience.education.tutorialAnswer.lecture4

import org.scalatest._
import org.scalatest.{FunSuite, Matchers}


import FPOption._

class FPOptionSpec extends FunSuite with Matchers {


  val optionHello: FPOption[String] = FPSome("hello")
  val option65: FPOption[Int] = FPSome(65)
  val noInt: FPOption[Int] = FPNone
  val option64: FPOption[Int] = unit(64) // alternative construction

  def capitalLetter(i: Int): FPOption[Char] =
    if (i >= 65 && i <= 90) FPSome(i.toChar) // use unit here
    else FPNone

  // test(s"The inner type of $option65 .getOrElse($optionHello) is raised to common ancestor `Any`") {
  //   option65.orElse(optionHello) shouldBe an (FPOption[Any])
  // }


}
