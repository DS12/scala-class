package com.datascience.education.tutorial.lecture3

import org.scalatest._
import prop._

import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop.forAllNoShrink

import org.scalatest.prop.Checkers.check

import cats.data.Reader
import cats.syntax.applicative._

class DatabaseSpec extends PropSpec with
    GeneratorDrivenPropertyChecks with Matchers {


  import DatabaseQueriesAndUpdates._

  // Task 3a
//  property("User does not exist in database") {
//
//    val testDB = TestDatabase()
//
//    forAll(Gen.alphaStr) {
//      case (username: String) =>
//      userExists(username).run(testDB) should be (false)
//    }
//
//  }


  // val nonEmptyString = Gen.nonEmptyContainerOf(Gen.alphaStr)

  // val nonEmptyString = Gen.chooseNum(8, 32).flatMap { length =>
  //  Gen.listOfN(length, Gen.alphaChar).map(_.mkString)
  // }


  // Task 3b
//  property("create user") {
//    ???
//  }
  

  // Task 3c
//  property("User exists in database") {
//
//    ???
//  }

  // Task 3d
//  property("Password works") {
//    ???
//
//  }


  // Task 3e
//  property("Bad password fails") {
//
//    ???
//  }

}


