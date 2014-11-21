sbtPlugin := true

name := "sbt-imagej"
organization := "net.sf.ij-plugins"
version := "2.0.0-SNAPSHOT"
description := "SBT plugin that helps create runtime directory structure for ImageJ plugin development."
homepage := Some(url("http://github.com/jpsacha/sbt-imagej"))
organizationHomepage := Some(url("http://ij-plugins.sf.net"))
startYear := Some(2013)
licenses := Seq("GPLv3" -> url("http://www.gnu.org/licenses/gpl.html"))

scalaVersion := "2.10.4"

scalacOptions := Seq("-deprecation", "-unchecked")

publishArtifact in(Test, packageBin) := false
publishArtifact in(Test, packageDoc) := false
publishArtifact in(Test, packageSrc) := false

shellPrompt in ThisBuild := { state => "sbt:"+Project.extract(state).currentRef.project + "> " }

publishTo <<= version {
  version: String =>
    val nexus = "https://oss.sonatype.org/"
    if (version.contains("-SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=
  <scm>
    <url>git@github.com:jpsacha/sbt-imagej.git</url>
    <connection>scm:git@github.com:jpsacha/sbt-imagej.git</connection>
  </scm>
  <developers>
    <developer>
      <id>jpsacha</id>
      <name>Jarek Sacha</name>
      <url>https://github.com/jpsacha</url>
    </developer>
  </developers>

// Import default Sonatype publish settings.
sonatypeSettings
