package jzebra;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageWrapper {

    private static final String HEXES = "0123456789ABCDEF";

    public static String getImage(String url) {
        try {
            ImageIcon i = new ImageIcon(new URL(url));
            LogIt.log("Image specified: " + url);
            LogIt.log("Dimensions: " + i.getIconWidth() + "x" + i.getIconHeight());
            Dimension d = new Dimension(i.getIconWidth(), i.getIconHeight());
            BufferedImage buffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = buffer.createGraphics();
            g.drawImage(i.getImage(), 0, 0, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(buffer, "bmp", out);
            boolean black[] = getBlackPixels(out);
            int hex[] = getHexValues(black);
            LogIt.log("Bytes: " + (out.toByteArray().length - 54)
                    + ", Pixels: " + black.length);
            LogIt.log("Per Row: " + black.length / i.getIconHeight());
            LogIt.log("Binary Data: " + getHex(hex));
            
            // TODO: test ZPLII code
            // TODO: Use zebra compression for images
            // ~DGd:o.x,t,w,data
            //return "~DGR:JZEBRA.GRF," + (out.toByteArray().length - 54) + "," + 
            //        black.length / i.getIconHeight() + "," + getHex(hex);
            
            return "^GFA," + (out.toByteArray().length - 54) + "," + 
                    (out.toByteArray().length - 54) + "," + 
                    (black.length / i.getIconHeight()) + "," + 
                    getHex(hex);
            
            //return "";
        } catch (IOException e) {
            LogIt.log(e);
            return null;
        }
    }

    /**
     * Returns an array of ones or zeros.  boolean is used  instead of int
     * for memory considerations.
     * @param o
     * @return
     */
    private static boolean[] getBlackPixels(ByteArrayOutputStream o) {
        // Garbage (non-pixel) bytes to skip
        int skip = 54;

        // Image byte data
        byte[] data = o.toByteArray();

        // Byte lengh of the image data
        int length = data.length - skip;

        // Each pixel contains color data for red, green, blue
        boolean[] pixels = new boolean[(int) (length / 3)];

        for (int i = 0; i < pixels.length; i++) {
            int r = Integer.parseInt(Byte.toString(data[3 * i + skip]));
            int g = Integer.parseInt(Byte.toString(data[3 * i + skip + 1]));
            int b = Integer.parseInt(Byte.toString(data[3 * i + skip + 2]));

            // Assign "0" for all-white pixels, "1" for all others.
            pixels[i] = r * g * b == -1 ? false : true;
        }

        return pixels;
    }

    private static int[] getHexValues(boolean[] black) {
        int[] hex = new int[(int) (black.length / 8)];
        // Convert every eight zero's to a full byte, in decimal
        for (int i = 0; i < hex.length; i++) {
            for (int k = 0; k < 8; k++) {
                hex[i] += (black[8 * i + k] ? 1 : 0) << 7 - k;
            }
        }
        return hex;
    }

    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     * Returns the array of integers as a Hexadecimal String
     * 
     * @param raw
     * @return 
     */
    private static String getHex(int[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final int i : raw) {
            hex.append(HEXES.charAt((i & 0xF0) >> 4)).append(HEXES.charAt((i & 0x0F)));
        }
        return hex.toString();
    }
}
