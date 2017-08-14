name := "sbt-imagej-example"
organization := "ij-plugins.sf.net"
version := "2.0.1-SNAPSHOT"

scalaVersion := "2.12.3"

libraryDependencies += "net.imagej" % "ij" % "1.51p"

fork := true

// Enable and customize `sbt-imagej` plugin
enablePlugins(SbtImageJ)
ijRuntimeSubDir := "sandbox"
ijPluginsSubDir := "ij-plugins"
ijCleanBeforePrepareRun := true
ijExclusions += """nativelibs4java\S*"""
// Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
cleanFiles += ijPluginsDir.value
