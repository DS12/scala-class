package com.datascience.education.tutorialCommon.lecture4


trait EmptySetTypeclass {
  def sumList(l: List[Int]): Int
  def prodList(ds: List[Double]): Double
  
  def sumList2(l: List[Int]): Int
  def prodList2(l: List[Double]): Double

  def sum(l: List[Int]): Option[Int]

  def product(l: List[Double]): Option[Double]

  def sum2(l: List[Int]): Option[Int]

  def product2(l: List[Double]): Option[Double]

}
