package com.datascience.education.tutorialAnswer.lecture3

import cats.data.Reader
import cats.syntax.applicative._

import cats.MonadReader

import scala.collection.mutable.{Set => MutableSet}
import scala.collection.mutable.{HashSet => MutableHashSet}
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.mutable.HashMap

import org.mindrot.jbcrypt.BCrypt

import scala.util.Random
/*
 Note that in Reader, the `S` is static
 In `State`, `S` is intended to change.
 So this example does not "pass along" new copies of an immutable database
 */

sealed trait Database {

  val users: MutableMap[Int, String]
  val passwords: MutableMap[String, (String,String)]

  def genId: Int

  override def toString: String = {
    val userString = users.toList.map(_.toString + '\n').mkString
    val pwString = passwords.toList.map(_.toString + '\n').mkString

    "Users: "+userString+" passwords: "+pwString

  }
}

case class TestDatabase() extends Database {

  val users: MutableMap[Int, String] = MutableMap.empty[Int,String]

  // user name, user password hash
  // http://www.mindrot.org/projects/jBCrypt/

  // username, (salt, hash)
  // val passwords: MutableMap[String, (String, String)] =
  val passwords = MutableMap.empty[String,(String, String)]

  val rand = new Random()

  // unsafe if all ints used...
  @annotation.tailrec
  final def genId: Int = {
    val r = rand.nextInt().abs
    if (users.contains(r))
      genId
    else
      r
  }


}


object DatabaseQueriesAndUpdates {


  // Task 2a
  type DatabaseReader[A] = Reader[Database, A]

  // Task 2b
  def findUsername(userId: Int): DatabaseReader[Option[String]] =
    Reader { (db: Database) =>
      db.users.get(userId)
    }

  // Task 2c
  def findUserId(username: String): DatabaseReader[Option[Int]] =
    Reader { (db: Database) =>
      db.users.find{case (userId: Int, usern: String) =>
        usern == username}.map(_._1)
    }

  // Task 2d
  def userExists(username: String): DatabaseReader[Boolean] =
    findUserId(username).map(option => option.isDefined)

  // Task 2e
  def checkPassword(username: String, passwordClear: String): DatabaseReader[Boolean] =
    Reader { (db: Database) =>
      db.passwords.get(username).exists { case (salt: String, hash: String) =>
        // val hashedCandidate = BCrypt.hashpw(passwordClear, salt)
        BCrypt.checkpw(passwordClear, hash)
      }
    }

  // Task 2f
  def checkLogin(userId: Int, passwordClear: String): DatabaseReader[Boolean] =
    findUsername(userId).flatMap {
        case Some(username) => checkPassword(username, passwordClear)
        case _ => false.pure[DatabaseReader]
    }


  // Task 2g

  def createUser(username: String, passwordClear: String):
      DatabaseReader[Option[Int]] =
    userExists(username).flatMap { (exists: Boolean) =>
      // println(s"create user $username $passwordClear exists $exists")
      if(exists) Reader ( _ => None)
      else Reader { (db: Database) => 
        val id = db.genId
        val salt: String = BCrypt.gensalt()
        val passwordHashed = BCrypt.hashpw(passwordClear, salt)

        db.users.update(id, username)
        db.passwords.update(username, (salt, passwordHashed))

        // println(db)

        Some(id)
      }
    }

}


object DatabaseExample extends App {




}

