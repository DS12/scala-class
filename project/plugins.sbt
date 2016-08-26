resolvers ++= Seq(
  "Artima Maven Repository" at "http://repo.artima.com/releases",
  "Spark Packages Repo Bintray" at "https://dl.bintray.com/spark-packages/maven/"
)

addSbtPlugin("org.spark-packages" % "sbt-spark-package" % "0.2.4")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")
