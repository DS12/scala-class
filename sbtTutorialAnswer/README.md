# SBT Lab

This lab teaches the basics of the Simple Build Tool.

Specifically: 

* multiple sub-project build
* dependency resolution
* reducing code duplication in your SBT build configuration
* setting the version of SBT
* setting the version of Scala
* [Java/Scala package hierarchy](https://en.wikipedia.org/wiki/Java_package)
* how repositories are organized
	
There is no implementation required <b>Parts 1, 2, and 3</b>.  Your tasks are in <b>Parts 4 and 5</b>.

## Part 1: `minimal`

`scala-class/sbtTutorial/minimal/src/main/scala`

Directory `minimal` contains an SBT build definition that is close to minimal complexity, and some very simple Scala code in `minimal/src/main/scala/Hello.scala`.

Start SBT inside directory `minimal`:

```
bash-3.2$ pwd
../sbtTutorial/minimal
bash-3.2$ sbt
Java HotSpot(TM) 64-Bit Server VM warning: ignoring option MaxPermSize=384m; support was removed in 8.0
[info] Loading global plugins from /Users/peterbecich/dotfiles/sbt/0.13/plugins
[info] Loading project definition from ../sbtTutorial/minimal/project
[info] Set current project to hello (in build file:../sbtTutorial/minimal/)
> 
```

Two runnable main classes are defined in `Hello.scala`.  This means that `run` will prompt you to choose a main class to run:

```
> run 
run 
[warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list

Multiple main classes detected, select one to run:

 [1] Hello
 [2] Hello2
^JEnter number: 1
1

[info] Running Hello 
hello
[success] Total time: 4 s, completed May 21, 2016 5:14:34 PM
> 
```


`minimal/build.sbt` is a [build definition](http://www.scala-sbt.org/0.13/docs/Basic-Def.html).

It is not an absolutely minimal build definition, [as shown here](http://alvinalexander.com/scala/sbt-syntax-examples):

```
name := "Test 1"

version := "1.0"

scalaVersion := "2.9.1"
```

The reason we skip over this absolutely minimal build definition is that such a build definition is not suitable to be expanded to multiple sub-projects, as show in <b>Part 2</b>.  Our Scala code, and supporting code like build definitions, will keep scalability in mind whenever possible.

The version of SBT is set outside `minimal/build.sbt`, in `minimal/project/build.properties`.

## Part 2: `multi`

`scala-class-private/sbtTutorial/multi`

Directory `multi` contains an SBT build definition with multiple SBT sub-projects.

Start SBT inside directory `multi`.

You can list the sub-projects with `projects`:
```
> projects
projects
[info] 	 * backend
[info] 	   frontend
[info] 	   root
> 
```

`backend` and `frontend` are defined in `multi/build.sbt`. [The `root` sub-project](http://www.scala-sbt.org/0.13/docs/Multi-Project.html#Default+root+project) will exist in any SBT project. 


Enter project `frontend`:

```
> project frontend
project frontend
[info] Set current project to frontend (in build file:/Users/peterbecich/misc-private/sbtTutorial/multi/)
> 
```

Type `run`:

```
> run 
run 
[info] Running com.datascience.reporting.PrintPi 
Pi is estimated to equal 3.1484375 with 4096 Monte Carlo iterations
[success] Total time: 1 s, completed May 21, 2016 5:45:53 PM
> 
```

This SBT project demonstrates a dependency of `frontend` upon `backend`.

### reduction of code duplication

Simple code re-use is demonstrated.  `commonSettings` is shared by `backend` and `frontend`.  

Settings can be stacked.  Settings specific to sub-project `frontEnd` are stacked with `commonSettings`:

```
settings(
    name := "frontend",
    version := "1.0"
  ).settings(commonSettings:_*)
```

### [`aggregates`](http://www.scala-sbt.org/0.13/docs/Multi-Project.html#Aggregation)

```
lazy val frontend = (project in file("frontend")).
  settings(
    ...
  ).settings(commonSettings:_*).aggregates(backend)
```

`aggregates(backend)` means that whenever sub-project `frontend` is compiled, sub-project `backend` will be compiled.  It does not bring `backend`'s code into `frontend`'s scope.  It is one-directional.

```
lazy val backend = (project in file("backend")).
  ....aggregates(frontend)
```

would be necessary for bi-directional dependence.  With such tight coupling, the use of multiple sub-projects is called into question.

By default, `root` aggregates all other sub-projects.


### [`dependsOn`](http://www.scala-sbt.org/0.13/docs/Multi-Project.html#Classpath+dependencies)

```
lazy val frontend = (project in file("frontend")).
  settings(
    ...
  ).settings(commonSettings:_*).dependsOn(backend)
```

`dependsOn(backend)` gives `frontend` access to `backend`'s code.

[To assemble a runnable JAR with multiple sub-projects](https://github.com/sbt/sbt-assembly#assembly-task), the `root` sub-project must depend on the sub-project containing the runnable class.


`aggregates` is *not* strictly a subset of the functionality of `dependsOn`.

`aggregates` will make the "aggregator" sub-project run the tests of the "aggregated" sub-projects.  `dependsOn` will not do this.

See [How to make `sbt test` to run tests in main project and all subprojects (or some selected set)](http://stackoverflow.com/questions/32884308/how-to-make-sbt-test-to-run-tests-in-main-project-and-all-subprojects-or-some)


### Java package hierarchy

Informally, code can be located by filesystem location (`multi/backend/src/main/scala/MonteCarlo.scala`) or by Java package (`com.datascience.approximations.MonteCarlo`).  

I suggest referring to these separate systems as the *filesystem hierarchy* and the *class hierarchy*, respectively.  Either one of these will be useful to refer a colleague to a particular piece of code.  

Java and Scala code only cares about class hierarchy, though.  Outside of the REPL, you cannot import Scala code from a file (`import src/main/scala/Foo.scala`) -- rather, import from a class (`import backend.Foo`).

Because of the importance of the class hierarchy, I recommend thinking in terms of packages and classes more often than `.scala` files.

You can navigate by package or class in any IDE:


[IntelliJ's Navigate by Name](https://www.jetbrains.com/help/idea/2016.1/navigating-to-class-file-or-symbol-by-name.html)

[Eclipse](http://stackoverflow.com/a/5298518/1007926)


### code

You can find two pieces of code in 

* `com.datascience.approximations.MonteCarlo`
    * located in `multi/backend/src/main/scala/MonteCarlo.scala`
  
* `com.datascience.reporting.PrintPi`
    * located in `multi/frontend/src/main/scala/PrintPi.scala`
  
Note that the class and filesystem hierarchies do not necessarily correspond.  `PrintPi`'s class hierarchy does not incorparate `frontend`.  This demonstrates how these two hierarchies can be "orthogonal".


[IntelliJ's Navigate by Name](https://www.jetbrains.com/help/idea/2016.1/navigating-to-class-file-or-symbol-by-name.html) tool or a similar tool will hunt down the class for you.  This is the way to find a class of ambiguous filesystem location.

## Part 3: `usingCats`

`scala-class/sbtTutorial/usingCats`

`usingCats` shows how to add an external dependency.  This SBT project contains only a single sub-project.

Start SBT inside directory `usingCats`.

The [Cats library](http://typelevel.org/cats/) is a dependency:

```
lazy val root = (project in file(".")).
  settings(
    name := "hello",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies += "org.typelevel" % "cats-core_2.11" % "0.5.0"

  )
```

`usingCats` contains a warm-up exercise for Monads week, based upon [this resource]( http://eed3si9n.com/herding-cats/Reader.html).

Look at class `com.datascience.storage.UserExperiment`, located in `usingCats/src/main/scala/UserRepository.scala`.

Many things are imported from the Scala standard library and the Cats library:
```
import scala.util.Try
import cats._
import cats.std.all._
import cats.syntax.functor._
import cats.data.Kleisli

```

Let's hone in on `import cats.data.Kleisli`.

Cats objects and classes are organized by package/class hierarchy.  `Kleisli` exists in package `cats.data`.

ScalaDoc is organized by package/class hierarchy.  [ScalaDoc for `cats.data.Kleisli`](http://typelevel.org/cats/api/#cats.data.Kleisli)

![](images/cats_data_kleisli.png)


Some class hierarchies begin with a company name, like `org.typelevel.discipline.Laws` or `com.datascience.storage.UserExperiment`.  Others do not, like `cats.data.Kleisli`.


The example, revisited in Week 11, demonstrates the use of a Reader Monad (Kleisli) for dependency injection.  The dependency is a repository of mafiosos.  Proper use of dependency injection assures us that our logic is not "hard-wired" to any particular mafioso repository.  We can swap alternative repositories in and out and re-use the logic we have implemented.

```
> run 
run 
[info] Running com.datascience.storage.UserExample 
describe Fredo
Fredo's boss is Michael
describe Sonny
None
the absence of Sonny in the repository is handled safely
[success] Total time: 2 s, completed May 21, 2016 6:01:20 PM
> 
```


## Part 4: `usingBreeze` Task

`scala-class/sbtTutorial/usingBreeze`

Use `minimal`, `multi`, and `usingCats` to set up the SBT project in directory `usingBreeze`.

Start SBT inside directory `usingBreeze`.

`usingBreeze` depends on 3 Breeze libraries, found here:

[https://github.com/scalanlp/breeze#sbt](https://github.com/scalanlp/breeze#sbt)
  
* Breeze
* Breeze Natives
* Breeze Viz


`usingBreeze` has two runnable sub-projects:

* `distributions`
    * `distributions` depends on Breeze and Breeze Natives.

* `plotting` 
    * `plotting` depends on `distributions`.  This is an internal dependency.
    * `plotting` also depends on Breeze, Breeze Natives, and Breeze Viz.  These are [external dependencies](https://www.ibm.com/support/knowledgecenter/SSMKHH_9.0.0/com.ibm.etools.mft.doc/ac30280_.htm%23ac30280_).


Set up these two sub-projects in `usingBreeze/build.sbt` and try to reduce code duplication.

### Task 4a: Set up `distributions`

`distributions` is not dependent on `plotting`.  That means `plotting` can remain uncompilable and unrunnable while `distributions` is runnable.

Resolve the dependencies of `distributions`.  Do not add dependencies that `distributions` does not need -- Breeze Viz.

At the SBT prompt, enter the `distributions` sub-project.

`reload` at the SBT prompt to take in changes made to `build.sbt`.

`run` to see some outputs of various statistical distributions.

### Task 4b: Set up `plotting`

`plotting` depends on `distributions`, so will not be compilable or runnable if `distributions` is not set up correctly.

Resolve the dependencies of `plotting`.

`reload` at the SBT prompt to take in changes made to `build.sbt`.

`run` to produce plots of two distributions.  The rendered PNGs will appear in `usingBreeze/`.

![](images/gaussian_histogram_example.png)

### Task 4c: Set up `root`

Skim the documentation on the [*default `root` sub-project*](http://www.scala-sbt.org/0.13/docs/Multi-Project.html#Default+root+project).

> If a project is not defined for the root directory in the build, sbt creates a default one that aggregates all other projects in the build. 

Define `root` explicitly instead of letting SBT create it implicitly.

`root` should aggregate `distributions` and `plotting`.

### Task 4d: Refactor to reduce duplication

`plotting` and `distributions` share two external dependencies.

Take another look at `multi/build.sbt` from <b>Part 2</b>.  Define `commonSettings` so that these two dependencies only appear once in `usingBreeze.sbt`.  Use stacking, as described in <b>Part 2</b>, to resolve `plotting`'s third external dependency -- Breeze Viz.

There will be no difference in the behavior of `plotting` or `distributions`, but you have eliminated some obvious code duplication and room for error.  Come time to change the version of these Breeze dependencies, there will be less room for error.

Use [*How `build.sbt` defines settings*](http://www.scala-sbt.org/0.13/docs/Basic-Def.html#How+build.sbt+defines+settings) as a guide:

```
lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0",
  scalaVersion := "2.11.8"
)
```


###Terminal commands you will use

* `sbt` in the root of `minimal`, `multi`, `usingCats` or `usingBreeze` to enter SBT.

* `sbt` in a sub-folder an SBT project will not load the project correctly.

###SBT commands you will use

* `projects` - list the sub-projects.  The one with an asterix is the sub-project you're currently in.

* `project X` - enter a sub-project

* `reload` - reload `build.sbt`.  Equivalent to exiting SBT and starting it again.

* `compile` - fetches dependencies if necessary

* `run` - run the main method in an SBT sub-project, or pick a main method if there is more than one is an SBT sub-project

* `exit`

* `help` - Displays this help message or prints detailed help on requested commands (run `help <command>`)


## Part 5: `usingBreezeRefactored` Task

`scala-class/sbtTutorial/usingBreezeRefactored`

In this <b>Part</b> we will refactor our build configuration from <b>Part 4</b> in the style of the [MLeap library](https://github.com/TrueCar/mleap).

We will split our `build.sbt` into multiple files: `build.sbt`, `project/Common.scala` and `project/Dependencies.scala`.

The extra imports in the `*.scala` files

```
import sbt._
import Keys._
```

are explained in [*What is the difference between `build.sbt` and `build.scala`?*](http://stackoverflow.com/a/18010698/1007926).


### Task 5a: `Common`

`scala-class/sbtTutorial/usingBreezeRefactored/project/Common.scala`

Emulate [`mleap/project/Common.scala`]( https://github.com/TrueCar/mleap/blob/master/project/Common.scala)

Refactor `commonSettings` from `scala-class/sbtTutorial/usingBreeze/build.sbt` in <b>Part 4</b>.

```
import sbt._
import Keys._

object Common {

  lazy val commonSettings: Seq[Def.Setting[_]] = ???

}
```

Define the *organization* as `com.datascience.education`.

Include

```
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
```

### Task 5b: `scalaVer`

`scala-class/sbtTutorial/usingBreezeRefactored/project/Common.scala`

Emulate [`mleap/project/Common.scala`]( https://github.com/TrueCar/mleap/blob/master/project/Common.scala)

Factor out the string containing the version of Scala from `commonSettings`.




```
import sbt._
import Keys._

object Common {

  // use Scala 2.11.8
  val scalaVer: String = ???

  lazy val commonSettings: Seq[Def.Setting[_]] = ???

}
```

### Task 5c: additional resolvers

`scala-class/sbtTutorial/usingBreezeRefactored/project/Common.scala`

Emulate [`mleap/project/Common.scala`]( https://github.com/TrueCar/mleap/blob/master/project/Common.scala)

Skim over [SBT's documentation on Resolvers](http://www.scala-sbt.org/0.13/docs/Resolvers.html).

Fill in 

```
  lazy val otherResolvers: Seq[Resolver] = ???
```

with

```
"bintray/non" at "http://dl.bintray.com/non/maven",
```

and

```
"Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
```
.


Put `otherResolvers` into `commonSettings` with, right below the `organization`:

```
    resolvers ++= otherResolvers
```

### Task 5d: `Dependencies`

`scala-class/sbtTutorial/usingBreezeRefactored/project/Dependencies.scala`

Emulate [`mleap/project/Dependencies.scala`](https://github.com/TrueCar/mleap/blob/master/project/Dependencies.scala).

Refactor `libraryDependencies` from `scala-class/sbtTutorial/usingBreeze/build.sbt` in <b>Part 4</b>.

Fill in 

```
  lazy val commonDependencies: Seq[ModuleID] = ???
```

and

```
  lazy val plottingDependencies: Seq[ModuleID] = ???
```

.  `plottingDependencies` should not include the `commonDependencies`.  They can be combined, if need be, with `commonDependencies.union(plottingDependencies)`.  [You have all the methods of `Seq` at your disposal.](http://www.scala-lang.org/files/archive/api/2.11.8/#scala.collection.immutable.Seq)


### Task 5e: `breezeVersion`

`scala-class/sbtTutorial/usingBreezeRefactored/project/Dependencies.scala`

Factor out the version of Breeze into a `val`:

```
  val breezeVersion: String = ???
```

### Task 5f: compiler plugins

`scala-class/sbtTutorial/usingBreezeRefactored/project/Dependencies.scala`

Skim the [SBT documentation on compiler plugins.](http://www.scala-sbt.org/1.0/docs/Compiler-Plugins.html)

[si2712fix-plugin](https://github.com/milessabin/si2712fix-plugin) and [kind-projector](https://github.com/non/kind-projector) are compiler plugins.  They are dependencies, but must be wrapped with `compilerPlugin(...)`.

The necessity of these two particular compiler plugins is a topic for another day.

```
  val kindProjector = compilerPlugin("org.spire-math" % "kind-projector" % "0.9.0" cross CrossVersion.binary)
  val si2712 = compilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)
```

Append these two dependencies to `commonDependencies: Seq[ModuleID]`.


### Task 5g: putting common dependencies in common settings

`scala-class/sbtTutorial/usingBreezeRefactored/project/Dependencies.scala`

Include `Dependencies.commonDependencies` with `scalaVersion`, `organization`, `scalacOptions` and `resolvers` in:

```
lazy val commonSettings: Seq[Def.Setting[_]] = ???
```

### Task 5h: `build.sbt`

`scala-class/sbtTutorial/usingBreezeRefactored/build.sbt`

Copy and refactor 

```
lazy val root = ???

lazy val distributions = ???

lazy val plotting = ???
```

from `scala-class/sbtTutorial/usingBreeze/build.sbt` in <b>Part 4</b>.

`root` should aggregate `distributions` and `plotting`.

`plotting` should depend on `distributions`.

Ensure the `plotting` sub-project has access to `Dependencies.plottingDependencies`.

This `build.sbt` will be much more concise than `scala-class/sbtTutorial/usingBreeze/build.sbt` in <b>Part 4</b>.

## References

[SBT 0.13 Scaladoc](http://www.scala-sbt.org/0.13.12/api/#package)

[.sbt build examples](http://www.scala-sbt.org/0.13/docs/Basic-Def-Examples.html)

[`ModuleID` Scaladoc](http://www.scala-sbt.org/0.13.12/api/#sbt.ModuleID) - the type that holds a dependency

[`Resolver` Scaladoc](http://www.scala-sbt.org/0.13.12/api/#sbt.Resolver)

[`Def.Setting` Scaladoc](http://www.scala-sbt.org/0.13.12/api/#sbt.Init$Setting)



