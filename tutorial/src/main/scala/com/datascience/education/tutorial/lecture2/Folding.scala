
package com.datascience.education.tutorial.lecture2

import scala.language.postfixOps

object Folding {

  // PART 1
  // 1a
  def myToString[A](list: List[A]): String = list match {
    case Nil => "Foo"
    case head :: tail => "Cons(" + head + ", " + myToString(tail) + ")"
  }
  myToString(List(1,2,3,4,5))

  // TASK 1b
  def foo(head: String, tail: String): String =
    "Cons(" + head + ", " + tail + ")"

  def myToString2(list: List[String]) = foldRight(list,"Nil")(foo)

  myToString2(List("1","2","3","4","5"))

  // Part 1c
  def foo2[A](head: A, tail: String): String =
    "Cons(" + head + ", " + tail + ")"

  def myToString3[A](list: List[A]) =
    foldRight[A,String](list,"Nil")(foo2)

  myToString3(List(1,2,3,4,5))

  // verbose
  foldRightPrinter(List(1,2,3,4,5),"Nil")(foo2)

  // PART 2
  // part 2a
  def foldRight[A, B](list: List[A], z: B)(f:(A, B) => B): B =
    list match {
      case Nil => z
      case x :: xs => f(x, foldRight(xs, z)(f))
    }

  def foldRightPrinter[A, B](list: List[A], z: B)(f:(A, B) => B): B = {
    println(s"input: $list")
    val out: B = list match {
      case Nil => z
      case x :: xs => f(x, foldRightPrinter(xs, z)(f))
    }
    println(s"output: $out")
    out
  }


  // part 2b
  @annotation.tailrec
  def foldLeft[A,B](list: List[A], z: B)(f: (B, A) => B): B =
    list match {
      case Nil => z
      case head :: tail => foldLeft(tail,f(z,head) )(f)
    }

  def foldLeftPrinter[A,B](list: List[A], z: B)(f: (B, A) => B): B = {
    println(s"input: $list")
    val out: B = list match {
      case Nil => z
      case head :: tail => {
        val zi: B = f(z, head)
        println(s"f($z, $head) = $zi")
        foldLeftPrinter(tail, zi)(f)
      }
    }
    println(s"output: $out")
    out
  }

  def foo4[A](tail: String, head: A): String = 
    "Cons(" + head + ", " + tail + ")"

  def myToString4[A](list: List[A]): String = foldLeft(list,"Nil")(foo4)

  myToString4(List(1,2,3,4))

  foldLeftPrinter(List(1,2,3,4),"Nil")(foo4)


  @annotation.tailrec
  def tailRecursive(i: Int): Int =
    if (i>100) i
    else tailRecursive(i+1)

  def notTailRecursive(i: Int): Int =
    if (i > 100) i
    else i + notTailRecursive(i+1)


  // part 2c

  def sumFoldRight(list: List[Int]): Int =
    foldRight(list, 0)((next: Int, sum: Int) => next+sum)

  def sumFoldLeft(list: List[Int]): Int =
    foldLeft(list, 0)((sum: Int, next: Int) => next+sum)


  def foldRightViaFoldLeft[A,B](l: List[A], z: B)(f: (A,B) => B): B =
    foldLeft(l.reverse, z)((b,a) => f(a,b))
  
  def myToString5(list: List[String]) =
    foldRightViaFoldLeft(list,"Nil")(foo)

  myToString5(List("1","2","3","4","5"))

  //difference between Reduce and foldLeft
  //http://stackoverflow.com/questions/25158780/difference-between-reduce-and-foldleft-fold-in-functional-programming-particula


  //List also has foldRight and foldLeft as methods

  val l = (0 to 5).toList
  //l.foldLeft(0)(_+_)
  //l.foldLeft(0)((r,c) => r + c)
  //l.foldLeft(0)((s,_) => s + 1)
  l.foldLeft(List[Int]())((r,c) => c :: r)

  // PART 3
  // 3a
  def average(list: List[Double]): Double =
    list.foldLeft(0.0)(_+_) /
  list.foldLeft(0.0)((count, next) => count+1)

  // TASK

  def average2(list: List[Double]): Double = {

    val tuple: (Double, Int) =
      list.foldLeft[(Double,Int)]((0.0, 0)){
        case ((sum: Double, count: Int), next: Double) =>
          ???
      }

    tuple._1 / tuple._2
  }

  val r = scala.util.Random
  r.nextDouble //returns a value between 0.0 and 1.0
  val l2 = average((for {i <- (0 to 400)} yield r.nextDouble).toList)


  // TASK 3b

  def contains[A](list: List[A], item: A): Boolean =
    list.foldLeft(???)(???)


  // contains(List(1,2,3,4),4)


  //  TASK 3c
  def last[A](list: List[A]): A = list.foldLeft[A](???)(???)



  // last(List(1,2,3,4))



  // TASK 3d
  def penultimate[A](list: List[A]): A = ???
    // list.foldLeft( (???, ???) )((???, ???) => ??? )//???

  // List(1,2,3,4).tail.head // hint- use (list.head, list.tail.head)  as the initial value
  // val a = (2,3)
  // a._1
  // penultimate(List(1,2,3,4))


  // TASK 3e
  def average3(list: List[Double]): Double = ???
    // list match {
    //    ???
    // }
  
  // average3(List(1,2,3,4))


  // TASK 3f
  def kthLast[A](l: List[A], k: Int) = {
    ???
    l.foldRight(???)(???)//.???
  }

  // kthLast(l,2)

  // TASK 3g
  def passThrough[A](list: List[A]): List[A] =
    ???

  // TASK 3h
  //curly braces are ok for function literals
  def mapViaFoldLeft[A,B](list: List[A], f: A => B): List[B] =
    ???

  val l3 = (0 to 5).toList

  // mapViaFoldLeft(l3,(a: Int) => a*2)

  // TASK 3i
  def unique[A](list: List[A]): List[A] =
    ???

  // unique(List(2,3,2,3,2,3,1,4))
  List(1,2,3).contains(2) //hint- use contains

  // TASK 3j
  def double[A](list: List[A]): List[A] =
    ???
  

  // TASK 3k

  val l4 = List(1,2,3) ::: List(4,5,6)
  l4.partition(_ > 2)

  def stackSort[A : Ordering](list: List[A]): List[A] =
    ???


  val l5 = List(9,3,4,2,6,4,8,1,5,63)
  // println(l5.mkString(","))
  // println(stackSort(l5).mkString(","))



  // TASK 3l

  def updateDiffs(tup: (Int, Int, Int), x: Int): (Int, Int, Int) =
    tup match {
      case (mn, mx, diff) if ??? => ??? // a new low
      case (mn, mx, diff) if ??? => ??? // a new high
      case _ => ???  // element x was not higher or lower than elements previously encountered.  Maximum difference remains the same.
    }
  

  def maxDifference(list: List[Int]): Int = list match {
    case Nil => -1
    case head :: tail => ???
  }

  // maxDifference(List(2,3,10,2,4,8,1))
  // maxDifference(List(7,9,5,6,3,2))

}
