package effects;

import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

import java.awt.*;

/**
 * Add a vignette effect to a color image. Borders of the image are darken.
 */
public final class VignettePlugIn implements PlugInFilter {

    private static final int BORDER_RATIO = 20;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_RGB;
    }

    @Override
    public void run(ImageProcessor ip) {
        final ColorProcessor cp = (ColorProcessor) ip;
        final int width = cp.getWidth();
        final int height = cp.getHeight();
        final int border = Math.min(width, height) / BORDER_RATIO;

        // Prepare vignette
        final FloatProcessor vignette = new FloatProcessor(width, height);
        vignette.setRoi(border, border, width - 2 * border, height - 2 * border);
        vignette.setColor(1);
        vignette.fill();
        vignette.setRoi((Rectangle) null);
        new GaussianBlur().blurFloat(vignette, 4 * border, 4 * border, 0.01);

        // Apply vignette to each color channel
        for (int i = 1; i <= 3; i++) {
            final FloatProcessor f = (FloatProcessor) cp.getChannel(i, null).convertToFloat();
            f.copyBits(vignette, 0, 0, Blitter.MULTIPLY);
            cp.setChannel(i, (ByteProcessor) f.convertToByte(false));
        }
    }
}
