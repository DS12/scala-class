
// Set sub-project on SBT start: http://stackoverflow.com/a/22240142/1007926

lazy val root = (project in file("."))
  .settings(
  onLoad in Global := {
    Command.process("project distributions", _: State)
  } compose (onLoad in Global).value
).settings(Common.commonSettings).aggregate(distributions, plotting)

lazy val distributions = (project in file("distributions"))
  .settings(name := "distributions")
  .settings(Common.commonSettings)
  

lazy val plotting = (project in file("plotting"))
  .settings(
    name := "plotting",
    libraryDependencies ++= Dependencies.plottingDependencies
  ).settings(Common.commonSettings).dependsOn(distributions)





