name := "scala-class"

import sbtassembly.AssemblyPlugin._

// Layout influenced by https://github.com/TrueCar/mleap/blob/master/build.sbt

spIgnoreProvided := true

lazy val root = project.in(file(".")).
  settings(Common.settings).
  settings(unidocSettings: _*).
  settings(Common.additionalUnidocSettings).
  aggregate(
    common, slideCode,
    lab, labAnswer,
    tutorial, tutorialAnswer, tutorialCommon,
    misc, miscAnswer, miscCommon
  )

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


lazy val miscCommon = (project in file("miscCommon")).
  settings(
    name := "miscCommon",
    scalaVersion := Common.scalaVer,
    description := "Miscellaneous common"
  ).settings(Common.settings).dependsOn(common)

lazy val misc = (project in file("misc")).
  settings(
    name := "misc",
    scalaVersion := Common.scalaVer,
    description := "Miscellaneous"
  ).
  settings(Common.settings).
  dependsOn(common).
  dependsOn(miscCommon % "compile->compile;test->test")

lazy val miscAnswer = (project in file("miscAnswer")).
  settings(
    name := "miscAnswer",
    scalaVersion := Common.scalaVer,
    description := "Miscellaneous answers"
  ).
  settings(Common.settings).
  dependsOn(common).
  dependsOn(miscCommon % "compile->compile;test->test")


