name := "sbt-imagej-example"
organization := "ij-plugins.sf.net"
version := "2.2.0"

scalaVersion := "3.8.2"

libraryDependencies += "net.imagej" % "ij" % "1.54p"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-explain",
  "-explain-types",
  "-rewrite",
  "-source:3.8-migration",
)

fork := true

// Customize `sbt-imagej` plugin
ijRuntimeSubDir := "sandbox"
ijPluginsSubDir := "ij-plugins"
ijCleanBeforePrepareRun := true
ijExclusions += """nativelibs4java\S*"""
// Instruct `clean` to delete created plugins subdirectory created by `ijRun`/`ijPrepareRun`.
cleanFiles += ijPluginsDir.value
