package com.datascience.education.tutorialAnswer.lecture4a

import cats.data.Xor
import cats.data.Xor.Left
import cats.data.Xor.Right
import cats.data.NonEmptyList


object XorHelpers {

  def concatIntoNonEmptyList[A](nel: NonEmptyList[A], l: List[A]): NonEmptyList[A] =
    NonEmptyList(nel.head, nel.tail:::l)

  def concatIntoList[A](nel: NonEmptyList[A], l: List[A]): List[A] =
    nel.head::nel.tail:::l

  def concat[A](nel1: NonEmptyList[A], nel2: NonEmptyList[A]): NonEmptyList[A] =
    NonEmptyList(nel1.head, nel2.head::nel1.tail:::nel2.tail)


  def toString[A](nel: NonEmptyList[A]): String = {
    val h = nel.head
    val t = nel.tail
    h.toString + " " + t.foldRight("")(_+" "+_)
  }

}
