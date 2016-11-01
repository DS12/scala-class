package com.datascience.education.tutorialCommon.lecture4

import org.scalatest.{FunSuite, Matchers}

class CommonEmptySetSpec[EmptySet](implicit EmptySet: EmptySetTypeclass) extends FunSuite with Matchers {

  import EmptySet._
  test("Sum of 1 through 10 inclusive should be 55; method sum") {
    sum((1 to 10).toList) should be (Some(55))
  }

  val dec = (1 to 10).map(_.toDouble).toList
  val prod = 3628800.0

  test(s"Product of $dec should be $prod ; prodList") {
    prodList(dec) should be (prod)
  }
  test(s"Product of $dec should be $prod ; prodList2") {
    prodList2(dec) should be (prod)
  }

  test(s"Sum of empty List should be None ; sum2") {
    sum2(List[Int]()) should be (None)
  }

  val oneTen = (1 to 10).toList
  val sm = oneTen.sum

  test(s"Sum of $oneTen should be Some($sm) ; sum2") {
    sum2(oneTen) should be (Some(sm))
  }
  

  test(s"Product of empty List should be None ; product2") {
    product2(List[Double]()) should be (None)
  }

  test(s"Product of $dec should be Some($prod) ; product2") {
    product2(dec) should be (Some(prod))
  }
  



}


