// sbt-imagej configuration keys
import ImageJKeys._

name := "sbt-imagej-example"

organization := "ij-plugins.sf.net"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.3"

// Point to location of a snapshot repositiry for ScalaFX
resolvers ++= Seq(
  "ImageJ Releases" at "http://maven.imagej.net/content/repositories/releases/"
)

libraryDependencies += "net.imagej" % "ij" % "1.47h"

// Set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "sbt:"+Project.extract(state).currentRef.project + "> " }

//
// sbt-imagej plugin settings
//
imageJSettings

imageJRuntimeDir := "sandbox"

imageJPluginsSubDir := "ij-plugins"

imageJExclusions += """nativelibs4java\S*"""
