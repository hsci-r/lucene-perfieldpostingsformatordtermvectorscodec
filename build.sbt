name := """lucene-perfieldpostingsformatordtermvectorscodec"""

organization := "fi.hsci"

version := "1.2.6"

scalaVersion := "2.13.5"

javacOptions ++= Seq("-source", "11", "-target", "11")

scalacOptions += "-target:jvm-11"

crossScalaVersions := Seq("2.10.7", "2.11.12","2.12.10")

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-codecs" % "8.8.2",
  "org.apache.lucene" % "lucene-backward-codecs" % "8.8.2",
  "com.koloboke" % "koloboke-api-jdk8" % "1.0.0",
  "com.koloboke" % "koloboke-impl-jdk8" % "1.0.0", 
  "junit" % "junit" % "4.13.2" % "test"
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "lucene", "codecs", "blocktreeords", "BlockTreeOrdsPostingsFormat.class") => MergeStrategy.first // override badly named contrib codec
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

