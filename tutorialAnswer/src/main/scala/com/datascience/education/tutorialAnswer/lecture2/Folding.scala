package com.datascience.education.tutorialAnswer.lecture2
import scala.language.postfixOps

object Folding {


  // PART 1
  // Part 1a
  def myToString[A](list: List[A]): String = list match {
    case Nil => "Nil"
    case head :: tail => "Cons(" + head + ", " + myToString(tail) + ")"
  }
  myToString(List(1,2,3,4,5))

  // Part 1b
  def foo(head: String, tail: String): String =
    "Cons(" + head + ", " + tail + ")"

  def myToString2(list: List[String]) = foldRight(list,"Nil")(foo)

  myToString2(List("1","2","3","4","5"))

  // Part 1c
  def foo2[A](head: A, tail: String): String =
    "Cons(" + head + ", " + tail + ")"

  def myToString3[A](list: List[A]) = foldRight[A,String](list,"Nil")(foo2)

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

  // verbose
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
          (next+sum, count+1)
      }

    tuple._1 / tuple._2
  }

  val r = scala.util.Random
  r.nextDouble //returns a value between 0.0 and 1.0
  val l2 = average((for {i <- (0 to 400)} yield r.nextDouble).toList)


  // TASK 3b

  def contains[A](list: List[A], item: A): Boolean =
    list.foldLeft(false){(detected: Boolean, next: A) => 
	   detected || item.equals(next)
    }

  def contains2[A](list: List[A], item: A): Boolean =
    list.foldLeft(false)(_ || _ == item)


  /*We choose an initial value of false. That is, we’ll assume the item is not in the list until we can prove otherwise. We use each of the two parameters exactly once and in the proper order, so we can use the ‘_’ shorthand in our function literal. That function literal returns the result so far (a Boolean) ORed with a comparison of the current item and the target value. If the target is ever found, the accumulator becomes true and stays true as foldLeft continues.*/


  contains(List(1,2,3,4),4)


  // TASK 3c
  def last[A](list: List[A]): A = list.foldLeft[A](list.head)((_, c) => c)

  //When it gets to the end of the list, the accumulator holds the la0st item. We don’t use the accumulator value in the function literal, so it gets parameter name ‘_’


  last(List(1,2,3,4))
  //List(1,2,3,4).head // hint- use head


  // TASK 3d
  def penultimate[A](list: List[A]): A =
    list.foldLeft( (list.head, list.tail.head) )((r, c) => (r._2, c) )._1

  /*This one is like the function ‘last’, but instead of keeping just the current item it keeps a Pair containing the previous and current items. When foldLeft completes, its result is a Pair containing the next-to-last and last items. The “_1” method returns just the penultimate item. You can use a similar idea to implement a more efficient average.*/


  // List(1,2,3,4).tail.head // hint- use (list.head, list.tail.head)  as the initial value
  // val a = (2,3)
  // a._1
  penultimate(List(1,2,3,4))


  // TASK 3e
  def average3(list: List[Double]): Double =
    list match {
      case Nil => Double.NaN
      case head :: tail => tail.foldLeft((head,1)){ (avg, nxt) =>
        ( (avg._1+(nxt/avg._2)) * avg._2/(avg._2+1), avg._2+1)
      }._1
    }
  
  average3(List(1,2,3,4))


  // TASK 3f
  def kthLast[A](l: List[A], k: Int) = {
    
    def getK[A](elt: A, cache: List[A]): List[A] =
      if (cache.length == k) cache
      else elt :: cache
    
    l.foldRight(List[A]())(getK) head
  }

  kthLast(l,2)

  // TASK 3g
  def passThrough[A](list: List[A]): List[A] =
    list.foldLeft(List[A]()) { (xs: List[A], x: A) => x :: xs }.reverse

  // TASK 3h
  //curly braces are ok for function literals
  def mapViaFoldLeft[A,B](list: List[A], f: A => B): List[B] =
    list.foldLeft(List[B]()) { (xs: List[B], x: A) =>  f(x) :: xs }.reverse

  val l3 = (0 to 5).toList
  mapViaFoldLeft(l3,(a: Int) => a*2)

  // TASK 3i
  def unique[A](list: List[A]): List[A] =
    list.foldLeft(List[A]()) { (accumulator: List[A], next: A) =>
      if ( accumulator.contains(next) ) accumulator
      else next :: accumulator
    }.reverse

  def unique2[A](list: List[A]): List[A] =
    list.foldLeft(List[A]()) { (r,c) =>
      if (r.contains(c)) r else c :: r
    }.reverse
  
  /*As usual, we start with an empty list. foldLeft looks at each list item and if it’s already contained in the accumulator then then it stays as it is. If it’s not in the accumulator then it’s appended. This code bears a striking similarity to the ‘reverse’ function we wrote earlier except for the “if (r.contains(c)) r” part. Because of this, the foldLeft result is actually the original list with duplicates removed, but in reverse order. To keep the output in the same order as the input, we add the call to reverse.*/


  unique(List(2,3,2,3,2,3,1,4))
  List(1,2,3).contains(2) //hint- use contains

  // 3j
  def double[A](list: List[A]): List[A] =
    list.foldLeft(List[A]())((r,c) => c :: c :: r).reverse
  
  /*See a pattern? When you use foldLeft to transform one list into another, you usually end up with the reverse of what you really want.

   Alternately, you could have used the foldRight method instead. This does the same thing as foldLeft, except it accumulates its result from back to front instead of front to back.*/

  def double2[A](list: List[A]): List[A] =
    list.foldRight(List[A]())((c,r) => c :: c :: r)


  val l4 = List(1,2,3) ::: List(4,5,6)
  l4.partition(_ > 2)

  // 3k
  def stackSort[A : Ordering](list: List[A]): List[A] =
    list.foldLeft(List[A]()) { (ordered: List[A], next: A) =>
      val (front, back) = ordered.partition{(a: A) =>
        implicitly[Ordering[A]].lt(next, a)
      }

      front ::: next :: back
    }


  val l5 = List(9,3,4,2,6,4,8,1,5,63)
  println(l5.mkString(","))
  println(stackSort(l5).mkString(","))

  /*This can be done as an insertion sort using the foldLeft API. First, the type parameter ensures that we have elements that can be arranged in order. We start, predictably, with an empty list as our initial accumulator. Then, for each item we assume the accumulator is in order (which it always will be), and use span to split it into two sub-lists: all already-sorted items less than the current item, and all already-sorted items greater than or equal to the current item. We put the current item in between these two and the accumulator remains sorted. This is, of course, not the fastest way to sort a list. But it’s a neat foldLeft trick.*/


  // 3l
  // def updateDiffs(tup: (Int, Int, Int), x: Int): (Int, Int, Int) =
  //   tup match {
  //     case (mn, mx, diff) if x < mn => (x, mx, (mx-x) max diff) // a new low
  //     case (mn, mx, diff) if x > mx => (mn, x, (x-mn) max diff) // a new high
  //     case _ => tup
  //   }


}

object MaxDifference {

  def updateDiffs(tup: (Int, Int, Int), x: Int): (Int, Int, Int) =
    tup match {
      case (mn, mx, diff) if x < mn => (x, mx, mx-x) // a new low
      case (mn, mx, diff) if x > mx => (mn, x, x-mn) // a new high
      case _ => tup
    }
  

  def maxDifference(list: List[Int]): Int = list match {
    case Nil => -1
    case head :: tail => tail.foldLeft((head, head, -1)) {(tup, x) =>
      updateDiffs(tup,x)}._3
  }



}

object MaxDifferenceExamples extends App {
  import MaxDifference._

  // maxDifference(List(2,3,10,2,4,8,1))
  // maxDifference(List(7,9,5,6,3,2))

  val l1 = List(2,3,10,2,4,8,1)
  println(l1)
  println(maxDifference(l1))

  println("------------------")

  val l2 = List(7,9,5,6,3,2)
  println(l2)
  println(maxDifference(l2))


}
