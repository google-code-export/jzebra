package jzebra;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageWrapper {

    private static final String HEXES = "0123456789ABCDEF";

    public static String getImage(ImageIcon i, LanguageType lang, Charset charset) throws IOException {
        //try {
        int w = i.getIconWidth();
        int h = i.getIconHeight();
        LogIt.log("Image specified: " + i.getDescription());
        LogIt.log("Dimensions: " + w + "x" + h);
        BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffer.createGraphics();
        g.drawImage(i.getImage(), 0, 0, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(buffer, "bmp", out);
        boolean black[] = getBlackPixels(out, w, h);
        int hex[] = getHexValues(black);

        String data = getHex(hex);
        int bytes = data.length() / 2;
        int perRow = bytes / h;
        int pixels = black.length;

        LogIt.log("Bytes: " + bytes
                + ", Pixels: " + pixels);
        LogIt.log("Pixels/Row: " + pixels / h);
        LogIt.log("Bytes/Row: " + perRow);
        LogIt.log("Binary Data: " + data);


        // TODO: Use zebra compression for zpl2 supported printers

        switch (lang) {
            case ESCP:
            case CPCL:
                return "EG " + (int)(w/8) + " " + h + " 0 0 " + data;
            case ESCP2:
                // n1 and n2 represent the image height in 2-byte format
                // example: the number "09" would be "0009"
                //          the number "10" would be "000A"
                //          the number "255" would be "00FF"
                //          the number "510" would be "FFFF"
                // Images larger than 510pixels must not be supporteds
                if (h > 510) {
                    return " ESCP IMAGES TALLER THAN 510 PIXELS NOT SUPPORTED ";
                }
                int n1 = h > 255 ? h - 255 : 0;
                int n2 = h <= 255 ? h : 255;
                return (char) 27 + (char) 86 + (byte) n1 + (byte) n2 + new String(getBytes(hex), charset.name());
            case ZPLII:
                return "^GFA," + bytes + "," + bytes + "," + perRow + "," + data;
            default:
                return " ERROR CONVERTING JZEBRA IMAGE TO COMMANDS ";
        }


        // } 

        /* catch (IOException e) {
        LogIt.log("IOException reading \"" + url + "\".  Check that path "
        + "is correct and that you have proper permissions to read "
        + "from that location.", e);
        return null;
        }*/
    }

    public static String getImage(byte[] imgData, LanguageType lang, Charset charset) throws IOException, MalformedURLException, IllegalArgumentException {
        return getImage(new ImageIcon(imgData, "Byte Array"), lang, charset);
    }

    public static String getImage(String url, LanguageType lang, Charset charset) throws IOException, MalformedURLException, IllegalArgumentException {
        return getImage(new ImageIcon(new URL(url), url), lang, charset);
    }

    /**
     * REMOVED 2/14/2012
     * For ZPLII images, flips just row content to fix mirroring that occurs
     * @param hex
     * @param height
     * @return 
     */
    private static String flipRows(String hex, int height) {
        String flipped = "";
        int width = hex.length() / height;

        for (int i = 0; i < height; i++) {
            flipped += new StringBuilder(hex.substring(i * width, (i + 1) * width)).reverse().toString();
        }
        return flipped;
    }

    /**
     * Returns an array of ones or zeros.  boolean is used  instead of int
     * for memory considerations.
     * @param o
     * @return
     */
    private static boolean[] getBlackPixels(ByteArrayOutputStream o, int width, int height) {
        // Garbage (non-pixel) bytes to skip
        int skip = 54;

        // Image byte data
        byte[] data = o.toByteArray();

        // Byte lengh of the image data
        int length = data.length - skip;

        // Each pixel contains color data for red, green, blue
        boolean[] pixels = new boolean[(int) (length / 3)];

        int pos = 0;
        // Bitmap scanlanes are reversed!?, blashphemy! :)
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int i = (width * y) + x;
                int r = Integer.parseInt(Byte.toString(data[(3 * i) + skip]));
                int g = Integer.parseInt(Byte.toString(data[(3 * i) + skip + 1]));
                int b = Integer.parseInt(Byte.toString(data[(3 * i) + skip + 2]));

                // TODO: Better color parsing for non-B&W images
                // Assign "0" for all-white pixels, "1" for all others.


                pixels[pos++] = r * g * b == -1 ? false : true;
                // Uncomment to flip image 180 degrees  
                //pixels[pixels.length - 1 - i] = r * g * b == -1 ? false : true;

                // Debug
                //LogIt.log(pixels[i] + ",");

            }
        }

        /*
        for (int i = 0; i < pixels.length; i++) {
        int r = Integer.parseInt(Byte.toString(data[3 * i + skip]));
        int g = Integer.parseInt(Byte.toString(data[3 * i + skip + 1]));
        int b = Integer.parseInt(Byte.toString(data[3 * i + skip + 2]));
        
        // Assign "0" for all-white pixels, "1" for all others.
        
        
        pixels[i] = r * g * b == -1 ? false : true;
        // Uncomment to flip image 180 degrees  
        //pixels[pixels.length - 1 - i] = r * g * b == -1 ? false : true;
        
        // Debug
        LogIt.log(pixels[i] + ",");
        
        }
         * 
         */

        return pixels;
    }

    private static byte[] getBytes(int ints[]) {
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            bytes[i] = (byte) ints[i];
        }
        return bytes;
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
