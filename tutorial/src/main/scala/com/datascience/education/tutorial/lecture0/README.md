# Lecture 0 Tutorial: Getting Started

Tutorials put a special emphasis on use of the REPL, in comparison to the other SBT sub-projects in this repository.

This document will teach you the REPL development cycle for the tutorials.

1. Open `code/tutorials/src/main/scala/tutorials/lecture0/GettingStarted.scala` in your editor.  
A symlink in `tutorials` shortens this to `code/tutorials/lecture0/GettingStarted.scala`

## `scala-2.11` approach

2. In your terminal, navigate to the directory containing `GettingStarted.scala`,  `code/tutorials/src/main/scala/tutorials/lecture0/`
We are going to use the `-i` flag of the Scala REPL.
```
bash-3.2$ scala-2.11 -help
Usage: scala <options> [<script|class|object|jar> <arguments>]
...
 -i <file>    preload <file> before starting the repl
...
```

3. Start the REPL, pre-loading our file, with `scala-2.11 -i GettingStarted.scala`
```
bash-3.2$ scala-2.11 -i GettingStarted.scala
Loading GettingStarted.scala...
defined object GettingStarted
Welcome to Scala version 2.11.7 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_74).
Type in expressions to have them evaluated.
Type :help for more information.
scala> 
```


4. Import members of the `GettingStarted` `object` to bring them into the outermost scope.
```
scala> import GettingStarted._
import GettingStarted._
scala>
```

5. Use the `add` function.
```
scala> add(1)
res3: Int = 2
```

6. In your editor, change the definition of `add`.
```
object GettingStarted {
  def add(i: Int): Int = i+2
}
```

7. Back in the REPL, reload with `:load GettingStarted.scala`
```
scala> :load GettingStarted.scala
Loading GettingStarted.scala...
defined object GettingStarted
scala> 
```

8. Again, `import GettingStarted._`
```
scala> import GettingStarted._
import GettingStarted._
scala> 
```

9. Try `add` again
```
scala> add(1)
res4: Int = 3
scala> 
```
You have successfully edited the `.scala` file and brought the changes into the Scala REPL.

10. Use `:replay` to re-do any commands you entered previously.  With some mutable variables, `:replay` will cause logical errors.  Imagine an accumulator `var` that is mutated with `:replay`.  Nonetheless, `:replay` is useful for saving some repetition.  Note that `add` behaves by its most recent version, adding two, in each call to it.  `:replay` can replace the usage of `:load`.
```
scala> :replay 
Replaying: :load GettingStarted.scala
Loading GettingStarted.scala...
defined object GettingStarted
Replaying: import GettingStarted._
import GettingStarted._
Replaying: add(1)
res2: Int = 3
Replaying: :load GettingStarted.scala
Loading GettingStarted.scala...
defined object GettingStarted
Replaying: import GettingStarted._
import GettingStarted._
Replaying: add(1)
res3: Int = 3
scala> 
```

## SBT console approach
This uses `console` inside SBT

2. Launch SBT in `code`
```
bash-3.2$ pwd
../code
bash-3.2$ sbt
Java HotSpot(TM) 64-Bit Server VM warning: ignoring option MaxPermSize=384m; support was removed in 8.0
[info] Loading project definition from ../code/project
[info] Set current project to code (in build file:../code/)
[info] Set current project to slideCode (in build file:../code/)
> 
```

3. List the sub-projects (optional).  These sub-projects may change slightly from the time of this writing, but if only one appears, you are in the wrong SBT project.  Launching SBT in a sub-directory of `code` is a common mistake.
```
> projects
projects
[info] In file:../code/
[info] 	   code
[info] 	   common
[info] 	   labAnswers
[info] 	   labExercises
[info] 	 * slideCode
[info] 	   spark
[info] 	   sparkLabAnswers
[info] 	   sparkLabExercises
[info] 	   tutorials
> 
```

4. Enter the `tutorials` sub-project.
```
> project tutorials
project tutorials
[info] Set current project to tutorials (in build file:../code/)
```

5. Start at *step 4* of the *`scala-2.11` approach*.
Because your working directory is `code` rather than `code/tutorials/src/main/scala/tutorials/lecture0/`,
usage of `:load` is a little different.
```
scala> :load tutorials/src/main/scala/tutorials/lecture0/GettingStarted.scala
Loading tutorials/src/main/scala/tutorials/lecture0/GettingStarted.scala...
defined object GettingStarted
```

---------------------------------
---------------------------------

# Tips

`:load` makes [triggered execution](http://www.scala-sbt.org/0.13/docs/Howto-Triggered.html) unnecessary.  Triggered execution, below, re-issues an SBT command when a source file is modified on disk.

```
> ~compile 
~compile 
[success] Total time: 0 s, completed May 16, 2016 5:01:23 PM
1. Waiting for source changes... (press enter to interrupt)
```

--------------------

`???` turns a compile-time exception into a run-time exception.  It is useful for leaving code un-implemented.  If `pascal2` is called, in this situation, it will give a run-time exception.

```
object Recursion {
  // ...

  // implement
  def pascal2(r: Int)(c: Int): Int = ???
  
  // ...
}
```

```
bash-3.2$ pwd
../code/tutorials/lecture1
bash-3.2$ scala-2.11 -i Recursion.scala 
Loading Recursion.scala...
defined object Recursion

Welcome to Scala version 2.11.7 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_74).
Type in expressions to have them evaluated.
Type :help for more information.

scala> import Recursion._
import Recursion._

scala> pascal2(4)(2)
scala.NotImplementedError: an implementation is missing
  at scala.Predef$.$qmark$qmark$qmark(Predef.scala:225)
  at Recursion$.pascal2(<console>:229)
  ... 33 elided
```


----------------

Elided lines of a run-time exception [can be retrieved with](http://www.scala-lang.org/old/node/8501
) `lastException.printStackTrace`.  This has been truncated.

```
scala> 1/0
java.lang.ArithmeticException: / by zero
  ... 42 elided

scala> lastException.printStackTrace 
java.lang.ArithmeticException: / by zero
	at $line4.$read$$iw$$iw$.<init>(<console>:12)
	at $line4.$read$$iw$$iw$.<clinit>(<console>)
	at $line4.$eval$.$print$lzycompute(<console>:7)
	at $line4.$eval$.$print(<console>:6)
	at $line4.$eval.$print(<console>)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at scala.tools.nsc.interpreter.IMain$ReadEvalPrint.call(IMain.scala:786)
	at scala.tools.nsc.interpreter.IMain$Request.loadAndRun(IMain.scala:1047)
	at scala.tools.nsc.interpreter.IMain$WrappedRequest$$anonfun$loadAndRunReq$1.apply(IMain.scala:638)
	...
```

Let's retrieve the full stack-trace of the `scala.NotImplementedError` we just saw.  This has been truncated.

```
scala> lastException.printStackTrace
scala.NotImplementedError: an implementation is missing
	at scala.Predef$.$qmark$qmark$qmark(Predef.scala:225)
	at $line3.$read$$iw$$iw$Recursion$.pascal2(<console>:229)
	at $line8.$read$$iw$$iw$$iw$$iw$.<init>(<console>:15)
	at $line8.$read$$iw$$iw$$iw$$iw$.<clinit>(<console>)
	at $line8.$eval$.$print$lzycompute(<console>:7)
	at $line8.$eval$.$print(<console>:6)
	at $line8.$eval.$print(<console>)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at ...
```

------------

It's helpful to know that expressions inside an `object` are only evaluated after a value or function inside the object is used.  This is easier to show, than to describe.


In `GettingStarted.scala`:

```
object GettingStarted {
  println("lazy evaluation of the contents of `GettingStarted`")
  def add(i: Int): Int = i+3
}
```

Using `GettingStarted` in the REPL:

```
bash-3.2$ pwd
/Users/peterbecich/scala-for-data-scientists/code/tutorials/lecture0
bash-3.2$ scala-2.11 -i GettingStarted.scala
Loading GettingStarted.scala...
defined object GettingStarted

Welcome to Scala version 2.11.7 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_74).
Type in expressions to have them evaluated.
Type :help for more information.

```

It would be intuitive to think that *"lazy evaluation of the contents of `GettingStarted`"* is printed upon import, below:

```
scala> import GettingStarted._
import GettingStarted._
```

This is not the case.
The string is printed when `add` is used for the first time.

```
scala> add(1)
lazy evaluation of the contents of `GettingStarted`
res0: Int = 4

scala> 
```

Calling `add` again does not have the same effect.  The print statement is only evaluated once.

```
scala> add(1)
lazy evaluation of the contents of `GettingStarted`
res0: Int = 4

scala> add(1)
res1: Int = 4

scala> 
```

This has implications for tutorial code.

```
object Recursion {

  //Pascal's Triangle
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || r == 0 || r == c) 1
    else pascal(c-1, r-1) + pascal(c, r-1)
  }

  def renderRow(r: Int) = ((0 to r).map(pascal(_,r))  mkString " ") + '\n'

  // The strictly functional approach -- Pascal's Triangle
  print((0 to 10).map(row => renderRow(row) ).reduce(_+_))

  // implement
  def factorial(n: Int): Int = ???

}
```

The `print` statement under "The strictly functional approach" will only be evaluated after a function or value within `Recursion` is used.  Below, Pascal's Triangle prints out when we call `println(factorial(5))`.  Understandably, `factorial` gives `scala.NotImplementedError` run-time exception.


```
bash-3.2$ scala-2.11 -i Recursion.scala 
Loading Recursion.scala...
defined object Recursion

Welcome to Scala version 2.11.7 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_74).
Type in expressions to have them evaluated.
Type :help for more information.

scala> import Recursion._
import Recursion._

scala> println(factorial(5))
1
1 1
1 2 1
1 3 3 1
1 4 6 4 1
1 5 10 10 5 1
1 6 15 20 15 6 1
1 7 21 35 35 21 7 1
1 8 28 56 70 56 28 8 1
1 9 36 84 126 126 84 36 9 1
1 10 45 120 210 252 210 120 45 10 1
scala.NotImplementedError: an implementation is missing
  at scala.Predef$.$qmark$qmark$qmark(Predef.scala:225)
  at Recursion$.factorial(<console>:78)
  ... 35 elided

scala> 
```

In other examples, expressions that are lazily evaluated can interfere with your work.  In other words, to skip forward in these tutorials, you need to comment things out.

```
object Recursion {

  //Pascal's Triangle
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || r == 0 || r == c) 1
    else pascal(c-1, r-1) + pascal(c, r-1)
  }

  def renderRow(r: Int): String = ???

  // The strictly functional approach
  print((0 to 10).map(row => renderRow(row) ).reduce(_+_))

  def factorial(n: Int): Int = ???

}
```

In this example, calling the unimplemented `factorial` function will not give the "missing implementation" error for `factorial` -- the error that arises will belong to `renderRow`.  In this situation it can help to read the stack trace carefully.

```
bash-3.2$ scala-2.11 -i Recursion.scala 
Loading Recursion.scala...
defined object Recursion

Welcome to Scala version 2.11.7 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_74).
Type in expressions to have them evaluated.
Type :help for more information.

scala> import Recursion._
import Recursion._

scala> println(factorial(5))
scala.NotImplementedError: an implementation is missing
  at scala.Predef$.$qmark$qmark$qmark(Predef.scala:225)
  at Recursion$.renderRow(<console>:22)
  at Recursion$$anonfun$2.apply(<console>:29)
  at Recursion$$anonfun$2.apply(<console>:29)
  at scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:245)
  at scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:245)
  at scala.collection.immutable.Range.foreach(Range.scala:166)
  at scala.collection.TraversableLike$class.map(TraversableLike.scala:245)
  at scala.collection.AbstractTraversable.map(Traversable.scala:104)
  ... 35 elided

scala> 

```



----------------

If your Scala `object` is not importable, check for errors in the loading of the `.scala` file.  Warnings may have no negative impact.

```
bash-3.2$ scala-2.11 -i Recursion.scala 
Loading Recursion.scala...
<console>:70: error: not found: value a
         for(i<- 10 to 1 by -1){ d = i :: a }
                                          ^
<console>:105: warning: a pure expression does nothing in statement position; you may be omitting necessary parentheses
         e
         ^

Welcome to Scala version 2.11.7 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_74).
Type in expressions to have them evaluated.
Type :help for more information.

scala> import Recursion._
<console>:10: error: not found: value Recursion
       import Recursion._
```

# Sources
[longer stack traces in REPL
](http://www.scala-lang.org/old/node/8501)


[How to reload a class or package in Scala REPL?](http://stackoverflow.com/questions/2471947/how-to-reload-a-class-or-package-in-scala-repl)
