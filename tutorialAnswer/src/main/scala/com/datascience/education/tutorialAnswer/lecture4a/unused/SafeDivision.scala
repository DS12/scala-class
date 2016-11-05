// package labAnswers.lecture4a

// import cats.data.Xor
// import cats.data.Xor.Left
// import cats.data.Xor.Right

// import cats.Applicative
// import cats.std.list._

// import TraverseXor._

// object SafeDivision extends App {
//   /*
//    `safeDiv` will catch a java.lang.ArithmeticException
   
//    `fracs` is a list of tuples, where the first member of each tuple is the numerator, and the second member is the denominator.
   
//    Attempt to convert each member of this list to a Double: List[Tuple2[Int,Int]] => List[Xor[Exception, Double]]
//    Then convert 
//   `List[Xor[Exception, Double]] => Xor[Exception, List[Double]]`
   
//    */
//   val a = (10 to 30).toList
//   val b = (-10 to 10).toList
//   val fracsFailing: List[Tuple2[Int, Int]] = a.zip(b)

//   def safeDiv(x: Int, y: Int): XorException[Double] =
//     try {
//       val d: Double = x.toDouble / y
//       if (d.isNaN || d.isPosInfinity || d.isNegInfinity)
//         Left(new ArithmeticException(s"$x/$y incalculable"))
//       else
//         Right(d)
//     } catch { case e: Exception => Left(e) }

//   def divTuple(tup: (Int, Int)): XorException[Double] =
//     safeDiv(tup._1, tup._2)

//   val xorDoubles: List[XorException[Double]] =
//     fracsFailing.map(divTuple)

//   println("List[XorException[Double]]: ")
//   println(xorDoubles)

//   val xorList: XorException[List[Double]] =
//     sequence(xorDoubles)

//   println("XorException[List[Double]]: ")
//   println(xorList)

//   /*
//    Complete the same exercise in one step using `traverse`

//    listInstance.traverse[G[_], A, B](List[A])(A=>G[B])(Applicative[G])

//    Here, G = XorException, and Applicative[G] = Applicative[XorException]
//    */

//   val xorList2: XorException[List[Double]] =
//     traverse(fracsFailing)(divTuple)

//   println("XorException[List[Double]] in one step, using `traverse`: ")
//   println(xorList2)

//   println("These fractions do not include an undefined number")
//   val c = (6 to 11).toList
//   val d = (2 to 7).toList
//   val fracsSuccessful: List[Tuple2[Int, Int]] = c.zip(d)
 
//   val xorList3: XorException[List[Double]] =
//     traverse(fracsSuccessful)(divTuple)

//   println("XorException[List[Double]] in one step, using `traverse`: ")
//   println(xorList3)

// }
