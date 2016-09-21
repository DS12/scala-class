
// Read here for dependencies
// https://github.com/scalanlp/breeze#sbt

// Set sub-project on SBT start: http://stackoverflow.com/a/22240142/1007926
//     onLoad in Global := { Command.process("project distributions", _: State) } compose (onLoad in Global).value



// formatting of dependencies
// http://www.scala-sbt.org/0.13/docs/Library-Dependencies.html#The++key
// Task 4d
lazy val commonSettings: Seq[Def.Setting[_]] = ???

// Task 4c
lazy val root: Project = ???

// Task 4a
lazy val distributions: Project = ???

// Task 4b
lazy val plotting: Project = ???


