name := """lucene-perfieldpostingsformatordtermvectorscodec"""

organization := "fi.seco"

version := "1.0.3"

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.10.6", "2.11.8")

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-codecs" % "6.6.0",
   "junit" % "junit" % "4.12" % "test"
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
