package com.datascience.education.tutorial.lecture5

import com.datascience.education.common.lecture5.Stream
import com.datascience.education.common.lecture5.Empty
import com.datascience.education.common.lecture5.Cons
import com.datascience.education.common.lecture5.Stream._


object Sine {

  def sinePositive: Stream[Double] =
    Stream.cons(0,
      Stream.cons(1.0/2,
        Stream.cons(math.sqrt(3)/2,
          Stream.cons(1.0,
            Stream.cons(math.sqrt(3)/2,
              Stream.cons(1.0/2, sineNegative)
            )
          )
        )
      )
    )

  def sineNegative: Stream[Double] =
    sinePositive.map { d => -1*d }


  val sine = sinePositive


}




object Stepper {
  import Sine._

  val stepperSine = sine.flatMap { d =>
    Stream.cons(d, Stream.cons(d, Stream.empty))
  }


}



object Interpolation {

  import Sine._

  // Task 1a
  val linearInterpolated: Stream[Double] = ???



}
