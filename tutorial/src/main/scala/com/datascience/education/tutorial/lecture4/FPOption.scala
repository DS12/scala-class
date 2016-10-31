
package com.datascience.education.tutorial.lecture4



// Hide Scala's native Option, Some, and None from this namespace
import scala.{ Option => _ }
import scala.{ Some => _ }
import scala.{ None => _ }

// Part (5b)

sealed trait FPOption[A] {
  import FPOption._

  // def map[B](f: A => B): FPOption[B] = this match {
  //   case FPSome(a) => FPSome(f(a))
  //   case FPNone => FPNone
  // }

  // Part (5b)
  // def getOrElse(default: => A): A = this match {
  //   case FPSome(a) => a
  //   case FPNone => default
  // }




  // Task 5c
  // def orElse[A](opA: FPOption[A]): FPOption[A] =
  //   ???


  // Task 5d
  // def flatMap[B](f: A => FPOption[B]): FPOption[B] =
  //   ???


  // Task 5e
  // def map2[B, C](opB: FPOption[B])(f: (A,B) => C): FPOption[C] =
    // ???


}


object FPOption {

  case class FPSome[A](get: A) extends FPOption[A]
  case object FPNone extends FPOption[Nothing]

  def unit[A](a: A): FPOption[A] = FPSome(a)

}



object FPOptionExamples5b extends App {
  // imports all members of the `FPOption` companion object
  import FPOption._

  // val optionHello: FPOption[String] = FPSome("hello")

  // val option65: FPOption[Int] = FPSome(65)
  // println(s"FPOption[Int] = $option65")
  // val optionChar: FPOption[Char] = option65.map((i: Int) => i.toChar)
  // println(s"mapped to FPOption[Char] = $optionChar")

  // println("---------------------")
  // val noInt: FPOption[Int] = FPNone
  // println(s"FPOption[Int] = $noInt")
  // val noChar: FPOption[Char] = noInt.map((i: Int) => i.toChar)
  // println(s"mapped to FPOption[Char] = $noChar")

  // println("---------------------")

  // def capitalLetter(i: Int): FPOption[Char] =
  //   if (i >= 65 && i <= 90) FPSome(i.toChar) // use unit here
  //   else FPNone

  // println(s"FPOption[Int] = $noInt")
  // val orElseSentinel = noInt.getOrElse(999)
  // println(s"getOrElse; replaced with sentinel value: $orElseSentinel")


  


}


// object FPOptionExamples5c extends App {


//   // imports all members of the `FPOption` companion object
//   import FPOption._

//   val optionHello: FPOption[String] = FPSome("hello")
//   val option65: FPOption[Int] = FPSome(65)
//   val noInt: FPOption[Int] = FPNone
//   val option64: FPOption[Int] = unit(64) // alternative construction

//   def capitalLetter(i: Int): FPOption[Char] =
//     if (i >= 65 && i <= 90) FPSome(i.toChar) // use unit here
//     else FPNone


//   println(s"FPOption[Int] = $option65")
//   val opAny1: FPOption[Any] = option65.orElse(optionHello)
//   println(s"orElse; String as a supertype of Int; inferred to Any: "+opAny1)


//   println("---------------------")

//   println(s"FPOption[Int] = $noInt")
//   val opAny2 = noInt.orElse(optionHello)
//   println(s"orElse; String as a supertype of Int; inferred to Any: "+opAny2)

//   println("---------------------")
//   println(s"FPOption[Int] = $noInt")
//   val orElse64 = noInt.orElse(option64)
//   println(s"orElse; replaced with 64: $orElse64")

  

// }




// object FPOptionExamples5d extends App {


//   // imports all members of the `FPOption` companion object
//   import FPOption._

//   val optionHello: FPOption[String] = FPSome("hello")
//   val option65: FPOption[Int] = FPSome(65)
//   val noInt: FPOption[Int] = FPNone

//   def capitalLetter(i: Int): FPOption[Char] =
//     if (i >= 65 && i <= 90) FPSome(i.toChar) // use unit here
//     else FPNone



//   println(s"FPOption[Int] = $option65")
//   val optionChar2: FPOption[Char] = option65.flatMap(capitalLetter)
//   println(s"flatMapped to FPOption[Char] = $optionChar2")

//   println("---------------------")
//   val option64: FPOption[Int] = unit(64) // alternative construction
//   println(s"FPOption[Int] = $option64")

//   val optionChar3: FPOption[Char] = option64.flatMap(capitalLetter)
//   println(s"flatMapped to FPOption[Char] = $optionChar3")

//   println("---------------------")
//   println(s"FPOption[Int] = $noInt")
//   val optionChar4: FPOption[Char] = noInt.flatMap(capitalLetter)
//   println(s"flatMapped to FPOption[Char] = $optionChar4")



// }


// object FPOptionExamples5e extends App {


//   // imports all members of the `FPOption` companion object
//   import FPOption._

//   val option64: FPOption[Int] = FPSome(64)
//   val option65: FPOption[Int] = FPSome(65)
//   val noInt: FPOption[Int] = FPNone

//   val s = option64.map2(option65)(_+_)
//   println("the sum is "+s)


//   val noSum = option64.map2(noInt)(_+_)
//   println("no sum: "+noSum)



// }


