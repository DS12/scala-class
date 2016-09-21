

// http://www.scala-sbt.org/0.13/docs/Hello.html

lazy val root = (project in file(".")).
  settings(
    name := "hello",
    version := "1.0",
    scalaVersion := "2.11.8",
    // formatting of dependencies
    // http://www.scala-sbt.org/0.13/docs/Library-Dependencies.html#The++key
    libraryDependencies += "org.typelevel" % "cats-core_2.11" % "0.5.0"

  )


