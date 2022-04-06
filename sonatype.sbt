// To sync with Maven central, you need to supply the following information:
publishMavenStyle := true

// Open-source license of your choice
licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

// Where is the source code hosted: GitHub or GitLab?
import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("hsci-r", "lucene-perfieldpostingsformatordtermvectorscodec", "eetu.makela@iki.fi"))

