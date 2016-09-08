package com.datascience.education.tutorialAnswer.lecture5


import com.datascience.education.common.lecture5.Stream
import com.datascience.education.common.lecture5.Empty
import com.datascience.education.common.lecture5.Cons
import com.datascience.education.common.lecture5.Stream._


object QuickSort {

  // Task 4c
  // Answer 4c
  def quickSort(si: Stream[Int]): Stream[Int] = {
    println("call")
    // ensure that `Empty` and `Stream` refer to our implementations,
    // not Scala Collections implementations
    si match {
      case Empty => empty
      case Stream.cons(head, tail) =>
        quickSort(tail.filter(_ < head)).
          append(unit(head)).
          append(quickSort(tail.filter(_ >= head)))
    }
  }

  def quickSort(li: List[Int]): Stream[Int] =
    quickSort(listToStream(li))

}



object QuickSortExample extends App {

  import QuickSort._

  import scala.util.Random
  val rand = new Random()

  val unsorted = (for (_ <- 1 to 16) yield rand.nextInt(64)).toList

  println("unsorted")

  println(unsorted)

  println("---------")


  val sortedLazy = quickSort(unsorted)
  println("sort 4 digits")
  sortedLazy.print(4)

  println()

  println("-----------------------")


  val sortedLazy2 = quickSort(unsorted)
  println("sort 8 digits")
  sortedLazy2.print(8)

  println()

  println("-----------------------")

  val sortedLazy3 = quickSort(unsorted)
  println("least element / head")
  println(sortedLazy3.headOption)


  println("---------------------------")

  println("re-use of `sortedLazy`; demonstration of memoization")

  println("sort 4 digits")
  sortedLazy.print(4)

  println("sort 6 digits")
  sortedLazy.print(6)



}

object QuickSortUnevaluated extends App {
    import QuickSort._

  import scala.util.Random
  val rand = new Random()

  val unsorted = (for (_ <- 1 to 16) yield rand.nextInt(64)).toList

  println("---------")


  val sortedLazy = quickSort(unsorted)
  println("sort 4 digits")
  sortedLazy.print(4)

}
