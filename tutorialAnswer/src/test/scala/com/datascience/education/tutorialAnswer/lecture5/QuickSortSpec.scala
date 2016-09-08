package com.datascience.education.tutorialAnswer.lecture5

import org.scalatest._
import prop._
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalatest.prop.Checkers.check


class QuickSortSpec extends PropSpec
    with GeneratorDrivenPropertyChecks with Matchers {
  import QuickSort._

  // property("QuickSort on List sorts 16 integers") {
  //   forAll(
  //     Gen.listOfN(16, Gen.chooseNum(Integer.MIN_VALUE, Integer.MAX_VALUE))
  //   ) { nums =>
  //     val correctSorted = nums.sorted
  //     val sorted = quickSort(nums).toListFinite(20)
  //     sorted should be (correctSorted)
  //   }

  // }

}
