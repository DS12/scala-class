// taken from https://github.com/TrueCar/mleap/blob/master/project/Common.scala

import sbt._
import Keys._

object Common {

  val scalaVer: String = "2.11.8"

  lazy val otherResolvers: Seq[Resolver] = Seq(
    "bintray/non" at "http://dl.bintray.com/non/maven",
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
  )

  lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
    version := "0.1",
    scalaVersion := scalaVer,
    organization := "com.datascience.education",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    resolvers ++= otherResolvers,
    libraryDependencies ++= Dependencies.commonDependencies
  )



}

