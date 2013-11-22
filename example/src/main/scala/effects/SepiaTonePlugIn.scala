package effects

import ij.ImagePlus
import ij.plugin.filter.PlugInFilter
import ij.process.{ColorProcessor, ImageProcessor}

/**
 * ImageJ plugin that add a sepia tone to a color image.
 */
class SepiaTonePlugIn extends PlugInFilter {

  private val sepiaR = 112d / 112
  private val sepiaG = 66d / 112
  private val sepiaB = 20d / 112
  private val weight = 0.9

  def setup(arg: String, imp: ImagePlus) = PlugInFilter.DOES_RGB

  def run(ip: ImageProcessor) {

    val cp = ip.asInstanceOf[ColorProcessor]

    val n = cp.getPixelCount
    val channelR, channelG, channelB = new Array[Byte](n)
    cp.getRGB(channelR, channelG, channelB)

    for (i <- 0 until n) {
      // Convert from unsigned Byte to Int
      val r = channelR(i) & 0xFF
      val g = channelG(i) & 0xFF
      val b = channelB(i) & 0xFF
      // Weighted intensity
      val l = 0.299 * r + 0.587 * g + 0.114 * b

      // Apply sepia tone according to `weight`
      channelR(i) = clump(r * (1 - weight) + l * sepiaR * weight)
      channelG(i) = clump(g * (1 - weight) + l * sepiaG * weight)
      channelB(i) = clump(b * (1 - weight) + l * sepiaB * weight)
    }

    cp.setRGB(channelR, channelG, channelB)
  }

  /** Clamp input to unsigned Byte value. */
  private def clump(v: Double): Byte = {
    val i = math.round(v)
    if (i < 0) 0.toByte else if (i > 255) 255.toByte else (i & 0xFF).toByte
  }
}