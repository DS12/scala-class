package com.datascience.education.tutorial.lecture4

import org.scalatest.{FunSuite, Matchers}

class FPOptionSpec extends FunSuite with Matchers {
  import FPOption._

  // val optionHello: FPOption[String] = FPSome("hello")
  // val option65: FPOption[Int] = FPSome(65)
  // val noInt: FPOption[Int] = FPNone
  // val option64: FPOption[Int] = unit(64) // alternative construction

  // def capitalLetter(i: Int): FPOption[Char] =
  //   if (i >= 65 && i <= 90) FPSome(i.toChar) // use unit here
  //   else FPNone
  

  // test(s"The inner type of $option65 .getOrElse($optionHello) is raised to common ancestor `Any`") {
  //   "option65.orElse(optionHello): FPOption[Any]" should compile
  // }

  // // http://www.scalatest.org/user_guide/using_matchers#checkingThatCodeDoesNotCompile
  // test(s"The inner type of $option65 .getOrElse($optionHello) should not be Int") {
  //   "val foo: FPOption[Int] = option65.orElse(optionHello)" shouldNot typeCheck
  // }

  // test(s"The inner type of $option65 .getOrElse($optionHello) should not be String") {
  //   "val foo: FPOption[String] = option65.orElse(optionHello)" shouldNot typeCheck
  // }

  // test(s"The ASCII capital character for code 65 is FPSome('A')") {
  //   capitalLetter(65) shouldBe (FPSome('A'))
  // }

  // test(s"The ASCII capital character for code 64 is FPNone") {
  //   capitalLetter(64) shouldBe (FPNone)
  // }
  
  // test(s"option65.flatMap(capitalLetter) is FPSome('A')") {
  //   option65.flatMap(capitalLetter) shouldBe (FPSome('A'))
  // }

  // test(s"noInt.flatMap(capitalLetter) is FPNone") {
  //   noInt.flatMap(capitalLetter) shouldBe (FPNone)
  // }

  // test(s"The sum of FPSome(64) and FPSome(65) is FPSome(129)") {
  //   option64.map2(option65)(_+_) shouldBe (FPSome(129))
  // }

  // test(s"The sum of FPSome(64) and FPSome(65) should not be FPNone") {
  //   option64.map2(option65)(_+_) should not be (FPNone)
  // }

}
