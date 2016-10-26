package com.datascience.education.tutorialAnswer.lecture3

import org.scalatest._
import prop._

import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Prop.forAllNoShrink

import org.scalatest.prop.Checkers.check

import cats.data.Reader
import cats.syntax.applicative._

// class DatabaseSpec(genDb: Gen[Database]) extends PropSpec with
class DatabaseSpec extends PropSpec with
    GeneratorDrivenPropertyChecks with Matchers {


  import DatabaseQueriesAndUpdates._

  // Task 3a
  property("User does not exist in database") {

    val testDB = TestDatabase()

    forAll(Gen.alphaStr) {
      case (username: String) =>
      userExists(username).run(testDB) should be (false)
    }

  }


  // val nonEmptyString = Gen.nonEmptyContainerOf(Gen.alphaStr)

  val nonEmptyString = Gen.chooseNum(8, 32).flatMap { length =>
    Gen.listOfN(length, Gen.alphaChar).map(_.mkString)
  }


  // Task 3b
  property("create user") {
    val testDB = TestDatabase()

    check {
      forAllNoShrink(Gen.zip(nonEmptyString, nonEmptyString)) {
        case (username: String, passwordClear: String) =>
          val out = createUser(username, passwordClear).run(testDB)
          out.isDefined
      }
    }

  }
  

  // Task 3c
  property("User exists in database") {

    val testDB = TestDatabase()

    def createAndCheck(username: String, password: String): DatabaseReader[Boolean] =
      createUser(username, password).flatMap { opUserId =>
        opUserId match {
          case Some(userId) => findUserId(username).map(_.isDefined)
          case _ => false.pure[DatabaseReader]
        }
      }

    check {
      forAllNoShrink(Gen.zip(nonEmptyString, nonEmptyString)) {
        case (username: String, passwordClear: String) =>
          val out = createAndCheck(username, passwordClear).run(testDB)
          out == true
      }
    }

  }

  // Task 3d
  property("Password works") {

    val testDB = TestDatabase()

    def createAndCheckLogin(username: String, password: String): DatabaseReader[Boolean] =
      createUser(username, password).flatMap { opUserId =>
        opUserId match {
          case Some(userId) => checkLogin(userId, password)
          case _ => false.pure[DatabaseReader]
        }
      }

    check {
      forAllNoShrink(Gen.zip(nonEmptyString, nonEmptyString)) {
        case (username: String, passwordClear: String) =>
          val out = createAndCheckLogin(username, passwordClear).run(testDB)
          out == true
      }
    }

  }


  // Task 3e
  property("Bad password fails") {

    val testDB = TestDatabase()

    def createAndCheckLogin(username: String, password: String, badPassword: String): DatabaseReader[Boolean] =
      createUser(username, password).flatMap { opUserId =>
        opUserId match {
          case Some(userId) => checkLogin(userId, badPassword)
          case _ => false.pure[DatabaseReader]
        }
      }

    check {
      forAllNoShrink(Gen.zip(nonEmptyString, nonEmptyString, nonEmptyString)) {
        case (username: String, passwordClear: String, badPassword: String) =>
          val out = createAndCheckLogin(username, passwordClear, badPassword+"z").run(testDB)
          out == false
      }
    }

  }  

}

// class TestDatabaseSpec extends DatabaseSpec(Gen.const(TestDatabase))


