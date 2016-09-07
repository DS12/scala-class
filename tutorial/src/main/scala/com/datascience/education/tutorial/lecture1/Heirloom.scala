package com.datascience.education.tutorial.lecture1

object Heirloom {

  class GreatGrandparent
  class Grandparent extends GreatGrandparent
  class Parent extends Grandparent
  class Child extends Parent

  //case class HeirloomTransition[A, D <: A](ancestor: A, descendant: D)
  case class HeirloomTransition[D, A >: D](ancestor: A, descendant: D)

  val grandParentToChild =
    new HeirloomTransition(new Grandparent, new Child)

  // Task (4a) -- not an answer, given
  // val childToGrandparent =
  //   new HeirloomTransition(new Child, new Grandparent)


}

