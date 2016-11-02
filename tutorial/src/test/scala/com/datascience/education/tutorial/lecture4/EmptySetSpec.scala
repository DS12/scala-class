package com.datascience.education.tutorial.lecture4

import com.datascience.education.tutorialCommon.lecture4.EmptySetTypeclass
import com.datascience.education.tutorialCommon.lecture4.CommonEmptySetSpec

object EmptySetSpec {

  implicit val emptySetTypeclass = new EmptySetTypeclass {

    def sumList(l: List[Int]): Int = EmptySet.sumList(l)
    def prodList(ds: List[Double]): Double = EmptySet.prodList(ds)
    
    def sumList2(l: List[Int]): Int = EmptySet.sumList2(l)
    def prodList2(l: List[Double]): Double = EmptySet.prodList2(l)

    def sum(l: List[Int]): Option[Int] = EmptySet.sum(l)

    def product(l: List[Double]): Option[Double] = EmptySet.product(l)

    def sum2(l: List[Int]): Option[Int] = EmptySet.sum2(l)

    def product2(l: List[Double]): Option[Double] = EmptySet.product2(l)


  }

}

import EmptySetSpec._

// class EmptySetSpec
//     extends CommonEmptySetSpec()(emptySetTypeclass)
