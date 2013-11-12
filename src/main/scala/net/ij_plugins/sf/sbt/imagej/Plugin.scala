/*
 * Copyright (C) 2013 Jarek Sacha
 * email: jpsacha at gmail dot com
 *
 * This file is part of sbt-imagej.
 *
 * sbt-imagej is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sbt-imagej is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with sbt-imagej.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.ij_plugins.sf.sbt.imagej

import sbt._
import sbt.Keys._

/** SBT plugin that helps create runtime directory structure for ImageJ plugin development. */
object Plugin extends sbt.Plugin {

  import ImageJKeys._

  object ImageJKeys {
    /** Main tasks for setting up ImageJ runtime directory. */
    lazy val imageJRun = TaskKey[Unit]("imagej-run",
      "Prepare plugins directory and run ImageJ") //

    lazy val imageJPrepareRun = TaskKey[Seq[File]]("imagej-prepare-run",
      "Prepare plugins directory to run with ImageJ")

    lazy val imageJProjectDependencyJars = TaskKey[Seq[File]]("imagej-project-dependency-jars",
      "Prepare plugins directory to run with ImageJ")

    lazy val imageJRuntimeDir = SettingKey[String]("imagej-runtime-dir",
      "Location of ImageJ runtime directory relative to base directory.")

    lazy val imageJPluginsSubDir = SettingKey[String]("imagej-plugins-subdir",
      "Subdirectory of the `plugins` directory, where all `jar`s will be copied.")

    lazy val imageJExclusions = SettingKey[Seq[String]]("imagej-exclusions",
      "List of regex expressions that match JARs that will be excluded from the plugins directory.")
  }

  lazy val imageJSettings: Seq[Project.Setting[_]] = Seq(

    imageJRun <<= ((
      runner in run,
      baseDirectory in Runtime,
      imageJRuntimeDir,
      fullClasspath in Runtime,
      streams
      ) map runTask).dependsOn(imageJPrepareRun),

    imageJPrepareRun <<= ((
      baseDirectory in Runtime,
      imageJRuntimeDir,
      imageJPluginsSubDir in Runtime,
      imageJExclusions in Runtime,
      packageBin in Compile,
      fullClasspath in Runtime,
      streams
      ) map prepareRunTask).dependsOn(packageBin in Compile),

    imageJRuntimeDir := "sandbox",

    imageJPluginsSubDir := "jars",

    imageJExclusions := Seq(
      // ImageJ binaries
      """ij-\d.\d\d[a-z]\.jar""", """ij\.jar""",
      // Source archives
      """\S*-src\.\S*""", """\S*-sources\.\S*""", """\S*_src_\S*""",
      // Javadoc
      """\S*-javadoc\.\S*""", """\S*_javadoc_\S*""",
      // Scaladoc
      """\S*-scaladoc\.\S*""", """\S*_scaladoc_\S*"""
    )
  )

  private def runTask(scalaRun: ScalaRun,
                      base: java.io.File,
                      runtimeDir: String,
                      classpath: Classpath,
                      taskStreams: TaskStreams) {
    val logger = taskStreams.log
    val userDir = (base / runtimeDir).getCanonicalPath
    logger.debug("Run ImageJ with -ijpath " + userDir)
    scalaRun.run("ij.ImageJ", classpath.map(_.data), Seq("-ijpath", userDir), logger)
  }

  /**
   * Copy dependencies to ImageJ plugins directory
   */
  private def prepareRunTask(base: java.io.File,
                             runtimeDir: String,
                             pluginsSubDir: String,
                             exclusions: Seq[String],
                             jar: java.io.File,
                             dependencies: Seq[Attributed[File]],
                             taskStreams: TaskStreams
                              ): Seq[java.io.File] = {
    val logger = taskStreams.log
    val pluginsDir = base / runtimeDir / "plugins" / pluginsSubDir
    logger.debug("Copying to ImageJ plugin directory: " + pluginsDir.getCanonicalPath)
    pluginsDir.mkdirs()
    val files = jar +: (for (f <- dependencies) yield f.data)
    for (f <- files
         if !f.isDirectory
         if exclusions.forall(!f.getName.matches(_))) {
      logger.debug("Copying: " + f + " to " + (pluginsDir / f.getName).getCanonicalPath)
      IO.copyFile(f, pluginsDir / f.getName)
    }
    files
  }
}
