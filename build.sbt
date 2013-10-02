sbtPlugin := true

name := "sbt-imagej"

organization := "net.sf.ij-plugins"

version := "0.1.0-SNAPSHOT"

description := "SBT plugin that helps create runtime directory structure for ImageJ plugin development."

licenses := Seq("GPLv3" -> url("http://www.gnu.org/licenses/gpl.html"))

scalacOptions := Seq("-deprecation", "-unchecked")

publishArtifact in (Compile, packageBin) := true

publishArtifact in (Test, packageBin) := false

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := true

publishArtifact in (Test, packageDoc) := false

publishArtifact in (Test, packageSrc) := false

//publishMavenStyle := false

publishTo <<= version {
  version: String =>
    if (version.contains("-SNAPSHOT"))
      Some("Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
    else
      Some("Sonatype Nexus Releases" at "https://oss.sonatype.org/content/repositories/releases")
}
