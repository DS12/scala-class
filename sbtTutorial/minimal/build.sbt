

// http://www.scala-sbt.org/0.13/docs/Hello.html

lazy val root = (project in file(".")).
  settings(
    name := "hello",
    version := "1.0",
    scalaVersion := "2.11.8"
  )
