
package com.datascience.education.tutorialAnswer.lecture5


import com.datascience.education.common.lecture5.Stream
import com.datascience.education.common.lecture5.Empty
import com.datascience.education.common.lecture5.Cons
import com.datascience.education.common.lecture5.Stream._



import scala.math.BigInt

/*

 https://en.wikipedia.org/wiki/Look-and-say_sequence

 To generate a member of the sequence from the previous member, read off the digits of the previous member, counting the number of digits in groups of the same digit. For example:

 1 is read off as "one 1" or 11.
 11 is read off as "two 1s" or 21.
 21 is read off as "one 2, then one 1" or 1211.
 1211 is read off as "one 1, then one 2, then two 1s" or 111221.
 111221 is read off as "three 1s, then two 2s, then one 1" or 312211.


 */

object LookAndSay {

  /*
   https://www.rosettacode.org/wiki/Look-and-say_sequence#Scala
   
   Embed the function `next` into a Stream with `unfold`

   So if a single call to `next` provides the next integer in the Look And Say sequence,
   `unfold` will produce the next value in the Stream with `next`

   */

  // Task 3a
  // Answer 3a

  def next(num: List[BigInt]): List[BigInt] = num match {
    case Nil => Nil
    case head :: Nil => 1 :: head :: Nil
    case head :: tail =>
      val size = (num takeWhile (_ == head)).size
      List(BigInt(size), head) ::: next(num.drop(size))
  }

  def bigIntListToString(lbi: List[BigInt]): String =
    lbi.foldRight("")((bi: BigInt, s: String) => bi.toString + s)

  def f(priorList: List[BigInt]): Option[(String, List[BigInt])] = {
    val nextList: List[BigInt] = next(priorList)

    println("nextList = " + nextList)
    if (nextList.isEmpty) None
    else Some((bigIntListToString(priorList), nextList))
  }

  val initialLookAndSay = List(BigInt(1))

  val lookAndSay: Stream[String] = Stream.unfold(initialLookAndSay)(f)

}

object LookAndSayExample extends App {
  import LookAndSay._


  lookAndSay.print(10)

}

