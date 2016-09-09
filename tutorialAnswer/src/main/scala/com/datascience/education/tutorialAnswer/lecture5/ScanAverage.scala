package com.datascience.education.tutorialAnswer.lecture5


import com.datascience.education.common.lecture5.Stream
import com.twitter.algebird.AveragedValue
import com.twitter.algebird.AveragedGroup

object ScanRightExample extends App {

  val sum10 = Stream.from(0).take(6).scanRight(0)(_+_)

  sum10.print(10)


}

object Average {

  val incrementingNumbers: Stream[Int] = Stream.from(0)

  // Task 2a
  // Answer 2a
  def f(i: Int, priorAverage: => AveragedValue) =
    AveragedGroup.plus(AveragedValue(1, i.toDouble), priorAverage)

  // Answer 2a
  val average: Stream[AveragedValue] =
    incrementingNumbers.take(32).scanRight(AveragedGroup.zero)(f)

}

object AverageExample extends App {

  import Average._

  println("average of incrementing numbers")

  average.print(32)

}
