name := "sbt-imagej-example"
organization := "ij-plugins.sf.net"
version := "2.1.0"

scalaVersion := "2.12.8"

libraryDependencies += "net.imagej" % "ij" % "1.52i"

fork := true

// Customize `sbt-imagej` plugin
ijRuntimeSubDir := "sandbox"
ijPluginsSubDir := "ij-plugins"
ijCleanBeforePrepareRun := true
ijExclusions += """nativelibs4java\S*"""
// Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
cleanFiles += ijPluginsDir.value
