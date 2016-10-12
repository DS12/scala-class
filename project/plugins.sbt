resolvers ++= Seq(
  "bintray-sbt-plugins" at "http://dl.bintray.com/sbt/sbt-plugin-releases",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Artima Maven Repository" at "http://repo.artima.com/releases",
  "clojars" at "https://clojars.org/repo",
  "conjars" at "http://conjars.org/repo",
  "plugins" at "http://repo.spring.io/plugins-release",
  "sonatype" at "http://oss.sonatype.org/content/groups/public/"
)

// addSbtPlugin("org.spark-packages" % "sbt-spark-package" % "0.2.4")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.3")

addSbtPlugin("com.thoughtworks.sbt-api-mappings" % "sbt-api-mappings" % "0.2.3")

