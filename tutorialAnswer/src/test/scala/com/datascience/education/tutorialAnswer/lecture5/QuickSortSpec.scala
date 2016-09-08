package com.datascience.education.tutorialAnswer.lecture5

import org.scalatest._
import prop._
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalatest.prop.Checkers.check


class QuickSortSpec extends PropSpec
    with GeneratorDrivenPropertyChecks with Matchers {
  import QuickSort._

  property("QuickSort on List sorts 16 integers") {
    forAll(
      Gen.listOfN(16, Gen.chooseNum(Integer.MIN_VALUE, Integer.MAX_VALUE))
    ) { unsorted =>
      val correctSorted = unsorted.sorted
      val sorted = quickSort(unsorted).toListFinite(20)
      // http://www.scalatest.org/user_guide/using_matchers#logicalExpressions
      // (
      //   correctSorted should be (unsorted)
      //     and sorted should be (unsorted)
      // ) or (
      //   sorted should be (correctSorted)
      //     and sorted should not be (unsorted)
      // )

      sorted should be (correctSorted)
    }

  }

}
