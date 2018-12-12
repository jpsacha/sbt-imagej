/*
 * Copyright (C) 2013-2014 Jarek Sacha
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

import sbt.Keys._
import sbt._

/** SBT plugin that helps create runtime directory structure for ImageJ plugin development. */
object SbtImageJ extends sbt.AutoPlugin {

  object autoImport {
    /** Main tasks for setting up ImageJ runtime directory. */

    lazy val ijRun: TaskKey[Unit] = TaskKey[Unit](
      "ijRun",
      "Prepare plugins directory and run ImageJ")

    lazy val ijPrepareRun: TaskKey[Seq[File]] = TaskKey[Seq[File]](
      "ijPrepareRun",
      "Prepare plugins directory to run with ImageJ")

    lazy val ijCleanBeforePrepareRun: SettingKey[Boolean] = SettingKey[Boolean](
      "ijCleanBeforeRun",
      "If `true` the plugins directory will be cleaned (deleted) before it is populated by `ijPrepareRun` task. " +
        "This is useful if jar names change during build, for instance, due to versioning.")

    lazy val ijProjectDependencyJars: TaskKey[Seq[File]] = TaskKey[Seq[File]](
      "ijProjectDependencyJars",
      "Prepare plugins directory to run with ImageJ")

    lazy val ijRuntimeSubDir: SettingKey[String] = SettingKey[String](
      "ijRuntimeSubDir",
      "Location of ImageJ runtime directory relative to base directory.")

    lazy val ijPluginsSubDir: SettingKey[String] = SettingKey[String](
      "ijPluginsSubDir",
      "Subdirectory of the `plugins` directory, where all `jar`s will be copied, relative to `plugins` directory.")

    lazy val ijPluginsDir: SettingKey[File] = SettingKey[File](
      "ijPluginsDir",
      "Full path to `plugins` subdirectory, where all `jar`s will be copied. " +
        "By default, it is computed from `ijPluginsSubDir` and `ijRuntimeSubDir`.")

    lazy val ijExclusions: SettingKey[Seq[String]] = SettingKey[Seq[String]](
      "ijExclusions",
      "List of regex expressions that match JARs that will be excluded from the plugins directory.")
  }


  import net.ij_plugins.sf.sbt.imagej.SbtImageJ.autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    //    ijRun := ((
    //      runner in run,
    //      baseDirectory in Runtime,
    //      ijRuntimeSubDir,
    //      fullClasspath in Runtime,
    //      streams
    //    ) map runTask).dependsOn(ijPrepareRun).value,
    ijRun := {
      // ijPrepareRun should be executed first
      ijPrepareRun.value
      //
      runTask(
        (runner in run).value,
        (baseDirectory in Runtime).value,
        ijRuntimeSubDir.value,
        (fullClasspath in Runtime).value,
        streams.value
      )
    },

    //    ijPrepareRun := ((
    //      baseDirectory in Runtime,
    //      ijCleanBeforePrepareRun,
    //      ijRuntimeSubDir,
    //      ijPluginsSubDir in Runtime,
    //      ijExclusions in Runtime,
    //      packageBin in Compile,
    //      fullClasspath in Runtime,
    //      streams
    //    ) map prepareRunTask).dependsOn(packageBin in Compile).value,
    ijPrepareRun := {
      // packageBin needs to be executed first
      (packageBin in Compile).value
      //
      prepareRunTask(
        (baseDirectory in Runtime).value,
        ijCleanBeforePrepareRun.value,
        ijRuntimeSubDir.value,
        (ijPluginsSubDir in Runtime).value,
        (ijExclusions in Runtime).value,
        (packageBin in Compile).value,
        (fullClasspath in Runtime).value,
        streams.value
      )
    },

    ijCleanBeforePrepareRun := false,

    ijRuntimeSubDir := "sandbox",

    ijPluginsSubDir := "jars",

    ijPluginsDir := (baseDirectory in Runtime).value / ijRuntimeSubDir.value / "plugins" / ijPluginsSubDir.value,

    ijExclusions := Seq(
      // ImageJ binaries
      """ij-\d.\d\d[a-z]\.jar""",
      """ij\.jar""",
      // Source archives
      """\S*-src\.\S*""",
      """\S*-sources\.\S*""",
      """\S*_src_\S*""",
      // Javadoc
      """\S*-javadoc\.\S*""",
      """\S*_javadoc_\S*""",
      // Scaladoc
      """\S*-scaladoc\.\S*""",
      """\S*_scaladoc_\S*"""
    )
  )


  override def trigger = allRequirements

  /**
    * Run ImageJ; pointing it to created plugins.
    */
  private def runTask(scalaRun: ScalaRun,
                      base: java.io.File,
                      runtimeDir: String,
                      classpath: Classpath,
                      taskStreams: TaskStreams) {
    // val logger = sbt.Keys.streams.value.log
    val logger = taskStreams.log
    val userDir = (base / runtimeDir).getCanonicalPath
    logger.debug("Run ImageJ with -ijpath " + userDir)
    scalaRun.run("ij.ImageJ", classpath.map(_.data), Seq("-ijpath", userDir), logger)
  }

  /**
    * Copy dependencies to ImageJ plugins directory
    */
  private def prepareRunTask(base: java.io.File,
                             cleanBeforeRun: Boolean,
                             runtimeDir: String,
                             pluginsSubDir: String,
                             exclusions: Seq[String],
                             jar: java.io.File,
                             dependencies: Seq[Attributed[File]],
                             taskStreams: TaskStreams
                            ): Seq[java.io.File] = {
    // val logger = sbt.Keys.streams.value.log
    val logger = taskStreams.log
    val pluginsDir = base / runtimeDir / "plugins" / pluginsSubDir
    logger.debug("Preparing ImageJ plugin directory: " + pluginsDir.getCanonicalPath)
    if (pluginsDir.exists && cleanBeforeRun) {
      logger.debug("Cleaning (deleting) ImageJ plugin directory: " + pluginsDir.getCanonicalPath)
      IO.delete(pluginsDir)
    }
    pluginsDir.mkdirs()
    logger.debug("Copying to ImageJ plugin directory: " + pluginsDir.getCanonicalPath)
    // TODO: [Issue #3] version number, if missing could be restored using Attributed[File] info
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
