package com.datascience.approximations

import scala.util.Random

object MonteCarlo {

  def norm(x: Double, y: Double): Double = math.sqrt(x*x + y*y)

  def inCircle(n: Int, rand: Random): Int =
    List.fill(n)((rand.nextDouble, rand.nextDouble)).
      map { tup => norm(tup._1, tup._2).toInt }.
      map { radius => if(radius >= 1) 0 else 1 }.
      reduce(_+_)


  def pi(n: Int, rand: Random): Double = 4*inCircle(n, rand).toDouble/n



}


