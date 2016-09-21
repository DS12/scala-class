package com.datascience.education.tutorial.lecture5

import com.twitter.algebird.AveragedValue
import com.twitter.algebird.AveragedGroup

import com.datascience.education.common.lecture5.Stream



object ScanRightExample extends App {

  // 2a
  val sum10 = Stream.from(0).take(6).scanRight(0)(_+_)

  sum10.print(10)


}

object Average {

  val incrementingNumbers: Stream[Int] = Stream.from(0)

  // Task 2b

  val average: Stream[AveragedValue] = ???
    // incrementingNumbers.take(32).???

}

object AverageExample extends App {

  import Average._

  println("average of incrementing numbers")

  average.print(32)

}
