// taken from https://github.com/TrueCar/mleap/blob/master/project/Dependencies.scala

import sbt._

object Dependencies {

  import Common.scalaVer

  val breezeVersion = "0.12"

  // http://www.scala-sbt.org/1.0/docs/Compiler-Plugins.html

  // https://github.com/non/kind-projector
  // http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22kind-projector_2.11%22
  val kindProjector = compilerPlugin("org.spire-math" % "kind-projector" % "0.9.0" cross CrossVersion.binary)

  // https://github.com/milessabin/si2712fix-plugin
  // http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22si2712fix-plugin_2.11.8%22
  val si2712 = compilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)

  // formatting of dependencies
  // http://www.scala-sbt.org/0.13/docs/Library-Dependencies.html#The++key
  lazy val commonDependencies: Seq[ModuleID] = Seq(
    "org.scalanlp" %% "breeze" % breezeVersion,
    "org.scalanlp" %% "breeze-natives" % breezeVersion,
    kindProjector,
    si2712
  )

  lazy val plottingDependencies: Seq[ModuleID] = Seq(
    "org.scalanlp" %% "breeze-viz" % breezeVersion
  )



}
