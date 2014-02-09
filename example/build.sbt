// sbt-imagej configuration keys
import ImageJKeys._

name := "sbt-imagej-example"

organization := "ij-plugins.sf.net"

version := "1.2.0-SNAPSHOT"

scalaVersion := "2.10.3"

// Point to location of a snapshot repository for ImageJ
resolvers += "ImageJ Releases" at "http://maven.imagej.net/content/repositories/releases/"

libraryDependencies += "net.imagej" % "ij" % "1.47v"

// Set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "sbt:"+Project.extract(state).currentRef.project + "> " }

fork := true

//
// Import and customize sbt-imagej plugin tasks
//
ijSettings

ijRuntimeSubDir := "sandbox"

ijPluginsSubDir := "ij-plugins"

ijExclusions += """nativelibs4java\S*"""

// Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
cleanFiles += ijPluginsDir.value
