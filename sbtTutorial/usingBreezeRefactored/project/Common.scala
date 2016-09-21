// taken from https://github.com/TrueCar/mleap/blob/master/project/Common.scala

import sbt._
import Keys._

object Common {

  // emulate this: https://github.com/TrueCar/mleap/blob/master/project/Common.scala

  // Task 5b
  // use Scala 2.11.8
  val scalaVer: String = ???

  /*
   http://www.scala-sbt.org/0.13/docs/Resolvers.html

   Add resolvers 
    "bintray/non" at "http://dl.bintray.com/non/maven",
   and
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
   */

  // Task 5c
  lazy val otherResolvers: Seq[Resolver] = ???

  // Task 5a and Task 5g
  lazy val commonSettings: Seq[Def.Setting[_]] = ???

}

