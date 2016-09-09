name := "scala-class"

import sbtassembly.AssemblyPlugin._

// Layout influenced by https://github.com/TrueCar/mleap/blob/master/build.sbt

spIgnoreProvided := true

lazy val root = project.in(file(".")).
  settings(Common.settings).
  settings(unidocSettings: _*).
  settings(Common.additionalUnidocSettings).
  aggregate(common, slideCode, lab, labAnswer, tutorial, tutorialAnswer)

lazy val common = (project in file("common")).
  settings(
    name := "common",
    scalaVersion := Common.scalaVer,
    description := "All non-Spark-related sub-projects are dependent on the 'common' sub-project.  Our versions of FP in Scala classes go in here.  Boilerplate goes in here."
  ).settings(Common.settings)

lazy val slideCode = (project in file("slideCode")).
  settings(
    name := "slideCode",
    scalaVersion := Common.scalaVer
  ).settings(Common.settings).dependsOn(common)

lazy val lab = (project in file("lab")).
  settings(
    name := "lab",
    scalaVersion := Common.scalaVer,
    description := "Lab exercises"
  ).settings(Common.settings).dependsOn(common)

lazy val labAnswer = (project in file("labAnswer")).
  settings(
    name := "labAnswer",
    scalaVersion := Common.scalaVer,
    description := "Lab answers"
  ).settings(Common.settings).dependsOn(common)



lazy val tutorialCommon = (project in file("tutorialCommon")).
  settings(
    name := "tutorialCommon",
    scalaVersion := Common.scalaVer,
    description := "Tutorial common"
  ).settings(Common.settings).dependsOn(common)//.dependsOn(labAnswer)

lazy val tutorial = (project in file("tutorial")).
  settings(
    name := "tutorial",
    scalaVersion := Common.scalaVer,
    description := "Tutorial exercises"
  ).
  settings(Common.settings).
  dependsOn(common).
  dependsOn(tutorialCommon % "compile->compile;test->test").
  dependsOn(lab)

lazy val tutorialAnswer = (project in file("tutorialAnswer")).
  settings(
    name := "tutorialAnswer",
    scalaVersion := Common.scalaVer,
    description := "Tutorial answers"
  ).
  settings(Common.settings).
  dependsOn(common).
  dependsOn(tutorialCommon % "compile->compile;test->test").  
  dependsOn(labAnswer)


// assemblyMergeStrategy in assembly := {
//   case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
//   case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
//   case PathList("org", "apache", xs @ _*) => MergeStrategy.last
//   case PathList("com", "google", xs @ _*) => MergeStrategy.last
//   case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
//   case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
//   case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
//   case "about.html" => MergeStrategy.rename
//   case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
//   case "META-INF/mailcap" => MergeStrategy.last
//   case "META-INF/mimetypes.default" => MergeStrategy.last
//   case "plugin.properties" => MergeStrategy.last
//   case "log4j.properties" => MergeStrategy.last
//   case x =>
//     val oldStrategy = (assemblyMergeStrategy in assembly).value
//     oldStrategy(x)
// }

