

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8"
)

// Set sub-project on SBT start: http://stackoverflow.com/a/22240142/1007926
lazy val root = (project in file(".")).
  settings(
    onLoad in Global := { Command.process("project backend", _: State) } compose (onLoad in Global).value
  ).settings(commonSettings: _*)


lazy val backend = (project in file("backend")).
  settings(
    name := "backend",
    version := "1.0"
  ).settings(commonSettings:_*)

lazy val frontend = (project in file("frontend")).
  settings(
    name := "frontend",
    version := "1.0"
  ).settings(commonSettings:_*).dependsOn(backend)


