name := """lucene-perfieldpostingsformatordtermvectorscodec"""

organization := "fi.seco"

version := "1.3.0"

scalaVersion := "2.13.1"

crossScalaVersions := Seq("2.10.7", "2.11.12","2.12.10")

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-codecs" % "8.5.1",
  "org.apache.lucene" % "lucene-backward-codecs" % "8.5.1",
  "com.koloboke" % "koloboke-api-jdk8" % "1.0.0",
  "com.koloboke" % "koloboke-impl-jdk8" % "1.0.0", 
  "junit" % "junit" % "4.12" % "test"
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
