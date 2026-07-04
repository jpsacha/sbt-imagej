sbtPlugin := true

name                 := "sbt-imagej"
organization         := "net.sf.ij-plugins"
version              := "2.2.1.1-SNAPSHOT"
description          := "SBT plugin that helps create runtime directory structure for ImageJ plugin development."
homepage             := Some(url("http://github.com/jpsacha/sbt-imagej"))
organizationHomepage := Some(url("http://ij-plugins.sf.net"))
startYear            := Some(2013)
licenses             := Seq("GPLv3" -> url("http://www.gnu.org/licenses/gpl.html"))

ThisBuild / crossScalaVersions := Seq("3.8.4", "2.12.21")
ThisBuild / scalaVersion       := crossScalaVersions.value.head

lazy val root = (project in file("."))
  .settings(
    scalacOptions := Seq("-deprecation", "-unchecked"),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq("-Xcheckinit", "-Xsource:3", "-Xmigration")
        case Some((3, _)) => Seq(
            "-source:3.3-migration",
            "-explain",
            "-explain-types"
          )
        case _ => Seq.empty[String]
      }
    },
    // https://github.com/sbt/sbt2-compat
    addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0"),
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.12.13"
        case _      => "2.0.1"
      }
    }
  )
