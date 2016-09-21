package com.datascience.education.tutorial.lecture5


import scala.math.BigInt

import com.datascience.education.common.lecture5.Stream
import com.datascience.education.common.lecture5.Empty
import com.datascience.education.common.lecture5.Cons
import com.datascience.education.common.lecture5.Stream._

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

  val lookAndSay: Stream[String] = ???
  //   Stream.unfold???

}

object LookAndSayExample extends App {
  import LookAndSay._


  lookAndSay.print(10)

}

