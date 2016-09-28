package com.datascience.education.tutorialAnswer.lecture1

object TypeLinearization {

  trait Animal {
    override def toString = "animal"
  }

  trait Amphibian extends Animal {
    override def toString = "amphibian"
  }

  trait Philosophical {
    override def toString = "I think, therefore I am."
  }

  // Task (3a)
  // class Frog

  // Answer (3a)
  class Frog extends Amphibian with Philosophical
  //class Frog extends Philosophical with Amphibian

}

object TypeLinearizationExample extends App {

  import TypeLinearization._

  val kermit = new Frog

  println(kermit)

}

