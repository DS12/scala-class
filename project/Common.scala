// taken from https://github.com/TrueCar/mleap/blob/master/project/Common.scala

import sbt._
import Keys._

object Common {

  val scalaVer = "2.10.6"


  lazy val otherResolvers = Seq(
    "bintray/non" at "http://dl.bintray.com/non/maven",
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
    "twitter-repo" at "https://maven.twttr.com",
    "Clojars Repository" at "http://clojars.org/repo",
    "Conjars Repository" at "http://conjars.org/repo",
    "Artima Maven Repository" at "http://repo.artima.com/releases",
    "Spark Packages Repo Bintray" at "http://dl.bintray.com/spark-packages/maven",
    Opts.resolver.sonatypeSnapshots
  )
  

  lazy val settings: Seq[Def.Setting[_]] = Seq(
    version := "0.1",
    scalaVersion := scalaVer,
    organization := "com.datascience.waze",
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    resolvers ++= otherResolvers,
    libraryDependencies ++= Dependencies.dependencies
  )
  


}
