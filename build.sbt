name := """lucene-perfieldpostingsformatordtermvectorscodec"""

organization := "fi.seco"

version := "1.1.0"

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.10.6", "2.11.8")

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-codecs" % "7.1.0",
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
