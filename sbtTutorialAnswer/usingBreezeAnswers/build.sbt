
lazy val breezeVersion = "0.12"

// Task 4d
lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
  // resolvers ++= Seq(
  //   "bintray/non" at "http://dl.bintray.com/non/maven",
  //   "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
  // ),
  libraryDependencies ++= Seq(
    "org.scalanlp" %% "breeze" % breezeVersion,
    "org.scalanlp" %% "breeze-natives" % breezeVersion
  ),
  scalaVersion := "2.11.8"
)

// Set sub-project on SBT start: http://stackoverflow.com/a/22240142/1007926

// Task 4c
lazy val root = (project in file(".")).
  settings(
    onLoad in Global := { Command.process("project distributions", _: State) } compose (onLoad in Global).value
  ).settings(commonSettings: _*)

// Task 4a
lazy val distributions = (project in file("distributions")).
  settings(
    name := "distributions"
  ).settings(commonSettings: _*)


// Task 4b and Task 4d
lazy val plotting = (project in file("plotting")).
  settings(
    name := "plotting",
    libraryDependencies += "org.scalanlp" %% "breeze-viz" % breezeVersion
  ).settings(commonSettings: _*).dependsOn(distributions)





