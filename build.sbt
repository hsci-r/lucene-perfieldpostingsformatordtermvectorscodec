lazy val core = (projectMatrix in file("."))
  .settings(
    name := "lucene-perfieldpostingsformatordtermvectorscodec",
    publish / skip := true
  )
  .jvmPlatform(scalaVersions = Seq("3.1.2","2.13.8"))

ThisBuild / organization := "io.github.hsci-r"

ThisBuild / version := "1.2.11"

ThisBuild / versionScheme := Some("early-semver")

ThisBuild / scalaVersion := "3.1.2"

ThisBuild / libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-codecs" % "8.9.0",
  "org.apache.lucene" % "lucene-backward-codecs" % "8.9.0",
  "com.koloboke" % "koloboke-api-jdk8" % "1.0.0",
  "com.koloboke" % "koloboke-impl-jdk8" % "1.0.0", 
  "junit" % "junit" % "4.13.2" % "test"
)

ThisBuild / publishTo := sonatypePublishToBundle.value

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / assembly / assemblyMergeStrategy := {
  case PathList("org", "apache", "lucene", "codecs", "blocktreeords", "BlockTreeOrdsPostingsFormat.class") => MergeStrategy.first // override badly named contrib codec
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

