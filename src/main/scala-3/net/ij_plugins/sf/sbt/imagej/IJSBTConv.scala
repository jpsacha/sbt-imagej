package net.ij_plugins.sf.sbt.imagej

import sbt.*
import sbt.Keys.*
import sbt.internal.util.ManagedLogger
import sbtcompat.PluginCompat.*

import scala.util.Try

/** SBT 1 compatibility helper */
object IJSBTConv {

  def runTask(
    scalaRun: ScalaRun,
    classpath: Classpath,
    _conv: xsbti.FileConverter,
    logger: ManagedLogger,
    userDir: String
  ): Try[Unit] = {
    // SBT 1/2 compatibility file converter context
    implicit val conv: xsbti.FileConverter = _conv
    scalaRun.run("ij.ImageJ", toNioPaths(classpath), Seq("-ijpath", userDir), logger)
  }

}
