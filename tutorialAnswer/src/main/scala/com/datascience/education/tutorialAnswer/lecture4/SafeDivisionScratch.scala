
package com.datascience.education.tutorialAnswer.lecture4


object SafeDivisionScratch {

  // http://alvinalexander.com/scala/scala-try-catch-finally-syntax-examples-exceptions-wildcard

  def safeDivCatchArithmeticException(x: Int, y: Int): Option[Double] =
    try {
      throw new Exception("vague exception")
    } catch {
      case e: Exception => None
    }

  def safeDivCatchAll(x: Int, y: Int): Option[Double] =
    try {
      throw new Exception("vague exception")
    } catch {
      case ae: java.lang.ArithmeticException => None
    }
  

}
