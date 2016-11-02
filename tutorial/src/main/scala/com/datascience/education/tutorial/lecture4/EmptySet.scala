package com.datascience.education.tutorial.lecture4


object EmptySet {
  def sumList(l: List[Int]): Int = l match {
    case Nil => 0
    case x :: xs => x + sumList(xs)
  }
  def prodList(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case x :: xs => x * prodList(xs)
  }
  
  def sumList2(l: List[Int]) = l.foldRight(0)(_ + _)
  def prodList2(l: List[Double]) = l.foldRight(1.0)(_ * _)



  // Task (1a)
  def sum(l: List[Int]): Option[Int] = ???

  // Task (1b)
  def product(l: List[Double]): Option[Double] = ???


  // Task (1c)
  def sum2(l: List[Int]): Option[Int] = ???

  // Task (1d)

  def product2(l: List[Double]): Option[Double] = ???


}


object EmptySetExamples extends App {

  import EmptySet._

  val nums = (1 to 10).toList
  val dec = nums.map(_.toDouble/10.0)

  val s10 = sum(nums)

  println(s"sum of $nums")
  println(s10)

  val emptyInt = List[Int]()

  val sEmpty = sum(emptyInt)

  println(s"sum of $emptyInt")
  println(sEmpty)


  println("--------------------")


  val p10 = product(dec)

  println(s"product of $dec")
  println(p10)

  val emptyDouble = List[Double]()

  val dEmpty = product(emptyDouble)

  println(s"product of $emptyDouble")
  println(dEmpty)
  

  println("--------------------")

  val sFolded10 = sum2(nums)
  println(s"sum of $nums")
  println(sFolded10)

  val sFoldedEmpty = sum2(emptyInt)
  println(s"sum of $emptyInt")
  println(sFoldedEmpty)

  println("--------------------")


  val pFolded10 = product2(dec)

  println(s"product of $dec")
  println(pFolded10)

  val dFoldedEmpty = product2(emptyDouble)

  println(s"product of $emptyDouble")
  println(dFoldedEmpty)



}
