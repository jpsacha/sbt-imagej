name := "sbt-imagej-example"
organization := "ij-plugins.sf.net"
version := "2.1.1"

scalaVersion := "3.3.6"

libraryDependencies += "net.imagej" % "ij" % "1.54p"

fork := true

// Customize `sbt-imagej` plugin
ijRuntimeSubDir := "sandbox"
ijPluginsSubDir := "ij-plugins"
ijCleanBeforePrepareRun := true
ijExclusions += """nativelibs4java\S*"""
// Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
cleanFiles += ijPluginsDir.value
