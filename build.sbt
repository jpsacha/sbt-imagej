// @formatter:off

sbtPlugin := true

name          := "sbt-imagej"
organization  := "net.sf.ij-plugins"
version       := "2.0.2-SNAPSHOT"
description   := "SBT plugin that helps create runtime directory structure for ImageJ plugin development."
homepage      := Some(url("http://github.com/jpsacha/sbt-imagej"))
organizationHomepage := Some(url("http://ij-plugins.sf.net"))
startYear     := Some(2013)
licenses      := Seq("GPLv3" -> url("http://www.gnu.org/licenses/gpl.html"))

scalaVersion  := "2.12.8"

scalacOptions := Seq("-deprecation", "-unchecked")

shellPrompt in ThisBuild := { state => "sbt:"+Project.extract(state).currentRef.project + "> " }
