
lazy val breezeVersion = "0.12"

lazy val commonSettings = Seq(
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

lazy val root = (project in file(".")).
  settings(
    onLoad in Global := { Command.process("project distributions", _: State) } compose (onLoad in Global).value
  ).settings(commonSettings: _*)

lazy val distributions = (project in file("distributions")).
  settings(
    name := "distributions"
  ).settings(commonSettings: _*)

lazy val plotting = (project in file("plotting")).
  settings(
    name := "plotting",
    libraryDependencies += "org.scalanlp" %% "breeze-viz" % breezeVersion
  ).settings(commonSettings: _*).dependsOn(distributions)





