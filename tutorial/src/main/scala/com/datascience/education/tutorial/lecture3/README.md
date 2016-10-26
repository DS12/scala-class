# Lecture 3 Tutorial: Monads


----------------

## Part 1: [`WriterT[Id,L,V]`](http://typelevel.org/cats/api/#cats.data.WriterT) ([`Writer[L,V]`](https://github.com/typelevel/cats/blob/master/core/src/main/scala/cats/data/package.scala#L39))


`scala-class/tutorials/src/main/scala/com/datascience/education/tutorial/lecture3/AsynchronousFactorials.scala`

There are some similarities between `WriterT` and [`StateT`](http://typelevel.org/cats/api/index.html#cats.data.StateT).

```
new WriterT(run: F[(L, V)]) {
  val run: F[(L, V)] = ...
  def value(implicit functorF: Functor[F]): F[V] = ...
  def written(implicit functorF: Functor[F]): F[L] = ...
  ...
}
```

```
new StateT(runF: F[(S) ⇒ F[(S, A)]]) { 
  def run(initial: S)(implicit F: FlatMap[F]): F[(S, A)]
  def runA(s: S)(implicit F: FlatMap[F]): F[A]
  def runS(s: S)(implicit F: FlatMap[F]): F[S]
  ...
}
```


A method that calculates factorial asynchronously is provided.  It is a surrogate for any recursive or iterative method that requires logging.  `factorialAsync` logs its progress to the console.


```

[info] Running com.datascience.education.tutorial.lecture3.AsynchronousFactorialsExample
unrelated: 1
factorial(0) = 1
unrelated: 2
factorial(1) = 1
unrelated: 3
factorial(2) = 2
unrelated: 4
factorial(3) = 6
unrelated: 5
factorial(4) = 24
factorial(5) = 120
unrelated: 6
factorial(6) = 720
unrelated: 7
unrelated: 8
factorial(7) = 5040
unrelated: 9
factorial(8) = 40320
factorial(9) = 362880
unrelated: 10
factorial(10) = 3628800
unrelated: 11
factorial 10 is 3628800
unrelated: 12
unrelated: 13
unrelated: 14
unrelated: 15
unrelated: 16
unrelated: 17
unrelated: 18
unrelated: 19
unrelated: 20
unrelated: 21
unrelated: 22
unrelated: 23
unrelated: 24
unrelated: 25
unrelated: 26
unrelated: 27
unrelated: 28
unrelated: 29
unrelated: 30
```

The `Future` prints "factorial 10 is 3628800" when complete.

This quickly becomes unwieldy when multiple asynchronous processes are running concurrently.  Here we calculate the 10th and 20th Factorial numbers (0-indexed), which, incidentially, rolls over the maximum `Int`:

```
[info] Running com.datascience.education.tutorial.lecture3.AsynchronousFactorialsExample 
factorial(0) = 1
factorial(0) = 1
unrelated: 1
factorial(1) = 1
factorial(1) = 1
unrelated: 2
factorial(2) = 2
factorial(2) = 2
unrelated: 3
factorial(3) = 6
factorial(3) = 6
...
unrelated: 17
factorial(16) = 2004189184
factorial(17) = -288522240
unrelated: 18
unrelated: 19
factorial(18) = -898433024
unrelated: 20
factorial(19) = 109641728
unrelated: 21
factorial(20) = -2102132736
factorial 20 is -2102132736
unrelated: 22
unrelated: 23
unrelated: 24
unrelated: 25
unrelated: 26
unrelated: 27
unrelated: 28
unrelated: 29
unrelated: 30

```

A `Future` which returns a `Writer` will clean up this example.  (Jumping ahead to monad transformers, there is another improvement to be made, here.)

### Task (1a): `type Logged[A]`

Implement 

```
type Logged[A] = ???
```

It is a type alias for `Writer`, and should contain multiple log `String`s, and a single value `A`.

You can choose between many types to contain these multiple log `String`s, but note a [requirement of `flatMap`](http://typelevel.org/cats/api/index.html#cats.data.WriterT@flatMap[U](f:V=>cats.data.WriterT[F,L,U])(implicitflatMapF:cats.FlatMap[F],implicitsemigroupL:cats.Semigroup[L]):cats.data.WriterT[F,L,U]):

```
def flatMap[U](f: (V) ⇒ WriterT[F, L, U])(
  implicit flatMapF: FlatMap[F], semigroupL: Semigroup[L]): WriterT[F, L, U]
```

Find an `implicit` from the Cats Scaladoc that will satisfy this requirement.


[`Writer[L,V]`](https://github.com/typelevel/cats/blob/master/core/src/main/scala/cats/data/package.scala#L39) is a type alias for [`WriterT[Id,L,V]`](http://typelevel.org/cats/api/#cats.data.WriterT), and `FlatMap[Id]` is provided.


### Task (1b): `factorial`

Complete the signature and implement `factorial`.  Use `Logged`.

It is a recursive function.  Insert `Thread.sleep(500)` at the end of your implementation, but before the returned value, so that each call of `factorial` costs 500 milliseconds.

```
  // Task (1b)
  def factorial(n: Int): ???
```

The log message of `factorial(1)` should be "hit bottom", or something similar.

Otherwise, the log message of `factorial(n)` should be `"factorial($n) = ..."`

Write a simple test in `FactorialWriterExample`.


### Task (1c): `factorialAsync`


Complete the signature and wrap `factorial` in a `Future`.

```
  def factorialAsync(n: Int): ???
```

### Task (1d): Testing

In `FactorialWriterAsyncExample`, use `factorialAsync` to calculate two Factorials asynchronously.  Don't `zip` or combine them.

Hint: Ensure that the `Writer`s inside actually *begin* work.

Choose between `Await` and `onSuccess` to print the result.

-------------------

## Part 2: [`Kleisli[Id, A, B]`](http://typelevel.org/cats/api/#cats.data.Kleisli) ([`Reader[A,B]`](https://github.com/typelevel/cats/blob/master/core/src/main/scala/cats/data/package.scala#L34))


`scala-class/tutorials/src/main/scala/com/datascience/education/tutorial/lecture3/Database.scala`


With the `State` Monad, we could thread an `S` through our program implicitly.  As long as we used the combinators that did not handle `S` directly, we were assured that `S` transitioned correctly.

```
S => (S, A)
         v
	     S => (S, A)
               v
         	   S => (S, A)
                     v
                    ....
```

`Reader` is similar to and simpler than `State`.  `Reader` is used to thread a static resource through your program.  This resource need not be immutable, as this <b>Part</b> will show.

Their `flatMap`s are quite similar.  Unfortunately the analogous generics do not share a letter between definitions.

On [`StateT`](http://typelevel.org/cats/api/index.html#cats.data.StateT@flatMap[B](fas:A=%3Ecats.data.StateT[F,S,B])(implicitF:cats.Monad[F]):cats.data.StateT[F,S,B]):

```
def flatMap[B](fas: (A) ⇒ StateT[F, S, B])(implicit F: Monad[F]): StateT[F, S, B] 
```

On [`KleisliT`](http://typelevel.org/cats/api/index.html#cats.data.Kleisli@flatMap[C](f:B=%3Ecats.data.Kleisli[F,A,C])(implicitF:cats.FlatMap[F]):cats.data.Kleisli[F,A,C]):

```
def flatMap[C](f: (B) ⇒ Kleisli[F, A, C])(implicit F: FlatMap[F]): Kleisli[F, A, C] 
```

Note that the supertype of `Reader` is not [`MonadReader`](http://typelevel.org/cats/api/#cats.MonadReader).  It is [`Kleisli`](http://typelevel.org/cats/api/#cats.data.Kleisli$).


Usernames, user IDs, salts and password hashes are stored in these [mutable `Map`s](http://www.scala-lang.org/api/current/index.html#scala.collection.mutable.Map):

```
  // user ID, username
  val users: MutableMap[Int, String]

  // username, (salt, hash)
  val passwords: MutableMap[String, (String,String)]
```

### Task (2a): type alias

Define 

```
  type DatabaseReader[A] = ???
```

so that any instance of trait `Database` can be used as the "resource" in our `DatabaseReader` methods.


### Task (2b): `findUsername`

Complete the signature and implement 

```
def findUsername(userId: Int) = ???
```

### Task (2c): `findUserId`


Complete the signature and Implement

```
  def findUserId(username: String): ???
```


### Task (2d): `userExists`

Complete the signature and implement 

```
def userExists(username: String): ???
```

### Task (2e): `checkPassword`

Complete the signature and implement

```
  def checkPassword(username: String, passwordClear: String): ???
```


### Task (2f): `checkLogin`

`checkLogin` should utilize `checkPassword`.

```
  def checkLogin(userId: Int, passwordClear: String): ???
```

### Task (2g): salt and `createUser`

Skim [salt](https://en.wikipedia.org/wiki/Salt_(cryptography)):


> In cryptography, a salt is random data that is used as an additional input to a one-way function that "hashes" a password or passphrase. ...

> A new salt is randomly generated for each password. 

Read the example of `jBCrypt` usage: [jBCrypt](http://www.mindrot.org/projects/jBCrypt/)


Implement 

```
  def createUser(username: String, passwordClear: String): ???
```


----------------------------

## Part 3: Property-based Testing

`scala-class/tutorials/src/test/scala/com/datascience/education/tutorial/lecture3/DatabaseSpec.scala`

`scala-class/tutorials/src/main/scala/com/datascience/education/tutorial/lecture3/Database.scala`

Materials of the `ScalaCheck` library can be found at:

- [https://www.scalacheck.org/](https://www.scalacheck.org/)
- [https://github.com/rickynils/scalacheck/blob/master/doc/UserGuide.md](https://github.com/rickynils/scalacheck/blob/master/doc/UserGuide.md) 
- [https://www.scalacheck.org/files/scalacheck_2.11-1.13.1-api/index.html#package](https://www.scalacheck.org/files/scalacheck_2.11-1.13.1-api/index.html#package)

For some of these tests it may be necessary to replace `forAll` with

```
    check {
      forAllNoShrink(...) { ...
	  }
	}
```

For each of these <b>Task</b>s, initialise a fresh copy of 

```
case class TestDatabase() extends Database { ... }
```

Note the *combinatory* behavior of `forAll`.  If given two generators, `forAll` will test various combinators of elements from the two generators.  Is this desirable behavior for testing user names and passwords?  Find a work-around.


### Task (3a): Non-existant users

Implement test 

```
  property("User does not exist in database") {
    ...
  }
```


### Task (3b): `createUser` works

The full signature of `createUser` was not given.  Test the return type of the implementation you decided upon.

```
property("create user") {
   ...
  }
```

### Task (3c): User exists


Insert a random user into the database and check they exist.

```
  property("User exists in database") {

	...
  }
```

Don't check that the password hashing works, here.  That's in the next <b>Task</b>



### Task (3d): Password works

Similar to prior <b>Task</b>.  Test that a valid password is successfully authenticated.

```
  property("Password works") {
     ...
	 
	 }
	 
```

### Task (3e): Bad password fails


```
  property("Bad password fails") {
     ...

  }  
```



---------------------

## Resources 

[Dead-Simple Dependency Injection](https://www.youtube.com/watch?v=ZasXwtTRkio)

[jBCrypt](http://www.mindrot.org/projects/jBCrypt/)

[herding cats: `Reader`](http://eed3si9n.com/herding-cats/Reader.html)


