// taken from https://github.com/TrueCar/mleap/blob/master/project/Dependencies.scala

import sbt._

object Dependencies {
  import Common.scalaVer

  
  val catsVersion = "0.6.0"
  val dogsVersion = "0.2.2"
  val algebirdVersion = "0.12.1"
  val shapelessVersion = "2.3.2"
  val framelessVersion = "0.1.0"
  val summingbirdVersion = "0.11.0-RC1"
  val breezeVersion = "0.12"
  val spireVersion = "0.12.0"
  val json4sVersion = "3.4.0"

  val scalatestVersion = "3.0.0"

  // will use Spark 2.0 and Scala 2.11
  // Spark 1.6, Scala 2.10 could be delicately joined with the rest of the 2.11 repo
  // Fortunately this is no longer necessary
  //val sparkVer = "1.6.2"

  val kindProjector = compilerPlugin("org.spire-math" % "kind-projector" % "0.8.0" cross CrossVersion.binary)
  val si2712 = compilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)
  
  lazy val dependencies = Seq(
    "org.typelevel" %% "cats" % catsVersion,
    "org.typelevel" %% "dogs-core" % dogsVersion,
    "com.twitter" %% "algebird-core" % algebirdVersion,
    "com.twitter" %% "algebird-test" % algebirdVersion,
    "com.twitter" %% "algebird-util" % algebirdVersion,
    "com.twitter" %% "algebird-bijection" % algebirdVersion,
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "io.github.adelbertc" %% "frameless-cats"      % framelessVersion,
    "io.github.adelbertc" %% "frameless-dataset"   % framelessVersion,
    "io.github.adelbertc" %% "frameless-dataframe" % framelessVersion,
    "org.scalatest" %% "scalatest" % scalatestVersion % "test",
    "org.scalacheck" % "scalacheck_2.11" % "1.13.2" % "test",
    "org.scalanlp" %% "breeze" % breezeVersion,
    "org.scalanlp" %% "breeze-natives" % breezeVersion,
    "org.scalanlp" %% "breeze-viz" % breezeVersion,
    "org.spire-math" %% "spire" % spireVersion,
    "de.svenkubiak" % "jBCrypt" % "0.4.1",
    "com.twitter" %% "summingbird" % summingbirdVersion,
    "com.twitter" %% "summingbird-example" % summingbirdVersion,
    "com.twitter" %% "summingbird-core" % summingbirdVersion,
    "com.twitter" %% "summingbird-client" % summingbirdVersion,
    "com.twitter" %% "summingbird-chill" % summingbirdVersion,
    "com.twitter" %% "summingbird-batch" % summingbirdVersion,
    "com.twitter" %% "summingbird-builder" % summingbirdVersion,
    "com.twitter" %% "summingbird-storm" % summingbirdVersion,
    "org.json4s" %% "json4s-native" % json4sVersion,
    kindProjector,
    si2712
  )


  // lazy val sparkDependencies = Seq(
  //   "org.apache.spark" %% "spark-core" % sparkVer % "provided",
  //   "org.apache.spark" %% "spark-sql" % sparkVer % "provided").union(dependencies)

}
