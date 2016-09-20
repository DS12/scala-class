package com.datascience.storage

import scala.language.higherKinds
import scala.util.Try
import cats._
import cats.std.all._
import cats.syntax.functor._
import cats.data.Kleisli


/*

 This is a warm-up for Monads Week.
 You will see similar examples in lectures 11 and 12.
 The topic of this particular example is dependency injection with the Reader Monad.

 The example this is based upon hides a lot in its imports.  I think this version is a little more explicit with its use of the Reader Monad.
 http://eed3si9n.com/herding-cats/Reader.html


 */
object UserExperiment {

  case class User(id: Long, bossId: Long, name: String)

  trait UserRepo {
    // def users: List[User]
    def names: List[String]
    def get(id: Long): User
    def find(name: String): User
  }

  /*
   Later, handle context of failure -- user not in repo
   */

  type Id[A] = A
  def identity[A](a: A): Id[A] = a

  val FlatMapId = new FlatMap[Id] {
    def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] =
      f(fa)

    def map[A, B](fa: Id[A])(f: A => B): Id[B] =
      f(fa)
  }

  type UserRepoReader[A] = Kleisli[Id, UserRepo, A]

  def getUser(id: Long): UserRepoReader[User] =
    new Kleisli( (repo: UserRepo) =>
      identity(repo.get(id))
    )

  def findUser(name: String): UserRepoReader[User] =
    new Kleisli( (repo: UserRepo) =>
      identity(repo.find(name))
    )

  /*
   Generalized in `userSummarizer`.
   Add a context, like the user not existing in the Repo.
   Option handles this context.
   */
  // def userSummary(name: String): UserRepoReader[String] =
  //   findUser(name).flatMap { user =>
  //     getUser(user.bossId).map { boss =>
  //       s"${user.name}'s boss is ${boss.name}"
  //     }
  //   }



  // def summarizeAllUsers: UserRepoReader[List[String]] =
  //   new Kleisli( (repo: UserRepo) => {
  //     val names: List[String] = repo.names
  //     val summaries: List[UserRepoReader[String]] =
  //       names.map { name => userSummary(name) }
  //     this.traverse(summaries)
  //   }
  //   )


  // val idToOption = new Naturan

  val applicativeOption: Applicative[Option] = new Applicative[Option] {
    // depends on Option's Monad but that's okay for now
    def ap[A, B](ff: Option[A => B])(fa: Option[A]): Option[B] =
      ff.flatMap { ab =>
        fa.map { a => ab(a) }
      }
    def pure[A](x: A): Option[A] = Some(x)
  }

  val FlatMapOption = new FlatMap[Option] {
    def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] =
      fa.flatMap(f)

    def map[A, B](fa: Option[A])(f: A => B): Option[B] =
      fa.map(f)
  }

  /*
   Without altering UserRepo, which is unsafe,
   lets modify the Reader to handle exceptions thrown by UserRepo.

   We'll lift Try/Option into the Reader
   */

  type UserRepoReaderOption[A] = Kleisli[Option, UserRepo, A]


  // def userSummarySafe(name: String): UserRepoReaderOption[String] =
  //   userSummary(name).lift(applicativeOption)


  def getUserSafe(id: Long): UserRepoReaderOption[User] =
    new Kleisli( (repo: UserRepo) =>
      Try(repo.get(id)).toOption
    )

  def findUserSafe(name: String): UserRepoReaderOption[User] =
    new Kleisli( (repo: UserRepo) =>
      Try(repo.find(name)).toOption
    )


  /*
   There is probably a better way to add context to our original
   UserRepoReader, but this is a start.
   */
  def userSummarizer[F[_]](
    getUserMethod: Long => Kleisli[F, UserRepo, User],
    findUserMethod: String => Kleisli[F, UserRepo, User]
  )(implicit F: FlatMap[F]): String => Kleisli[F, UserRepo, String] =
    (name: String) =>
  findUserMethod(name).flatMap { user =>
    getUserMethod(user.bossId).map { boss =>
      s"${user.name}'s boss is ${boss.name}"
    }
  }


  def userSummary(name: String): UserRepoReader[String] =
    userSummarizer[Id](getUser, findUser)(FlatMapId)(name)

  def userSummarySafe(name: String): UserRepoReaderOption[String] =
    userSummarizer[Option](getUserSafe, findUserSafe)(FlatMapOption)(name)

}



object UserExample extends App {
  import UserExperiment._

  val repo = new UserRepo {
    private val users = List(User(1, 1, "Vito"), User(2, 1, "Michael"), User(3,2, "Fredo"))
    def names: List[String] = users.map { user => user.name }
    // intentionally unsafe
    // will add failure context later
    def get(id: Long): User = users.find { user => user.id == id }.get

    def find(name: String): User = users.find { user => user.name == name }.get
  }

  println("describe Fredo")
  val describeFredo: String = userSummary("Fredo").run(repo)

  println(describeFredo)

  println("describe Sonny")
  val describeSonny: Option[String] =
    userSummarySafe("Sonny").run(repo)

  println(describeSonny)


  println("the absence of Sonny in the repository is handled safely")
}
