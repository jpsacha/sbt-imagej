/*
 * Copyright (C) 2013-2026 Jarek Sacha
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

import sbt.*
import sbt.Keys.*
import sbt.internal.util.ManagedLogger
import sbtcompat.PluginCompat.*

import scala.util.Try

/** SBT plugin that helps create runtime directory structure for ImageJ plugin development. */
object SbtImageJ extends sbt.AutoPlugin {

  object autoImport {

    /** Main tasks for setting up ImageJ runtime directory. */
    lazy val ijRun: TaskKey[Unit] = TaskKey[Unit](
      "ijRun",
      "Prepare plugins directory and run ImageJ"
    )

    lazy val ijPrepareRun: TaskKey[Seq[File]] = TaskKey[Seq[File]](
      "ijPrepareRun",
      "Prepare plugins directory to run with ImageJ"
    )

    lazy val ijCleanBeforePrepareRun: SettingKey[Boolean] = SettingKey[Boolean](
      "ijCleanBeforeRun",
      "If `true` the plugins directory will be cleaned (deleted) before it is populated by `ijPrepareRun` task. " +
        "This is useful if jar names change during build, for instance, due to versioning."
    )

    lazy val ijProjectDependencyJars: TaskKey[Seq[File]] = TaskKey[Seq[File]](
      "ijProjectDependencyJars",
      "Prepare plugins directory to run with ImageJ"
    )

    lazy val ijRuntimeSubDir: SettingKey[String] = SettingKey[String](
      "ijRuntimeSubDir",
      "Location of ImageJ runtime directory relative to base directory."
    )

    lazy val ijPluginsSubDir: SettingKey[String] = SettingKey[String](
      "ijPluginsSubDir",
      "Subdirectory of the `plugins` directory, where all `jar`s will be copied, relative to `plugins` directory."
    )

    lazy val ijPluginsDir: SettingKey[File] = SettingKey[File](
      "ijPluginsDir",
      "Full path to `plugins` subdirectory, where all `jar`s will be copied. " +
        "By default, it is computed from `ijPluginsSubDir` and `ijRuntimeSubDir`."
    )

    lazy val ijExclusions: SettingKey[Seq[String]] = SettingKey[Seq[String]](
      "ijExclusions",
      "List of regex expressions that match JARs that will be excluded from the plugins directory."
    )
  }

  import net.ij_plugins.sf.sbt.imagej.SbtImageJ.autoImport.*

  override def projectSettings: Seq[Def.Setting[?]] = Seq(
    ijRun := Def.uncached {
      // SBT 1/2 compatibility file converter context
      implicit val conv: xsbti.FileConverter = fileConverter.value

      // ijPrepareRun should be executed first
      ijPrepareRun.value
      //
      runTask(
        (run / runner).value,
        (Runtime / baseDirectory).value,
        ijRuntimeSubDir.value,
        (Runtime / fullClasspath).value,
        streams.value,
        conv
      )
    },

    ijPrepareRun := Def.uncached {
      // SBT 1/2 compatibility file converter context
      implicit val conv: xsbti.FileConverter = fileConverter.value

      // packageBin needs to be executed first
      (Compile / packageBin).value

      //
      prepareRunTask(
        (Runtime / baseDirectory).value,
        ijCleanBeforePrepareRun.value,
        ijRuntimeSubDir.value,
        (Runtime / ijPluginsSubDir).value,
        (Runtime / ijExclusions).value,
        toFile((Compile / packageBin).value),
        (Runtime / fullClasspath).value.map(x => toFile(x.data)),
        streams.value
      )
    },

    ijCleanBeforePrepareRun := false,

    ijRuntimeSubDir := "sandbox",

    ijPluginsSubDir := "jars",

    ijPluginsDir := (Runtime / baseDirectory).value / ijRuntimeSubDir.value / "plugins" / ijPluginsSubDir.value,

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
  private def runTask(
                       scalaRun: ScalaRun,
                       base: java.io.File,
                       runtimeDir: String,
                       classpath: Classpath,
                       taskStreams: TaskStreams,
                       _conv: xsbti.FileConverter
                     ): Try[Unit] = {

    // val logger = sbt.Keys.streams.value.log
    val logger: ManagedLogger = taskStreams.log
    val userDir: String = (base / runtimeDir).getCanonicalPath
    logger.debug("Run ImageJ with -ijpath " + userDir)

    // Version-specific SBT 1/2 compatibility run task
    IJSBTConv.runTask(scalaRun, classpath, _conv, logger, userDir)
  }

  /**
    * Copy dependencies to ImageJ plugins directory
    */
  private def prepareRunTask(
                              base: java.io.File,
                              cleanBeforeRun: Boolean,
                              runtimeDir: String,
                              pluginsSubDir: String,
                              exclusions: Seq[String],
                              jar: java.io.File,
                              dependencies: Seq[java.io.File],
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
    val files = jar +: dependencies.map(_.toPath.toFile)
    for (
      f <- files
      if !f.isDirectory
      if exclusions.forall(!f.getName.matches(_))
    ) {
      logger.debug("Copying: " + f + " to " + (pluginsDir / f.getName).getCanonicalPath)
      IO.copyFile(f, pluginsDir / f.getName)
    }
    files
  }
}
