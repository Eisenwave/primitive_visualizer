package eisenwave.primvis.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

/**
 * A utility library for performing calculations with colors represented as primitive ARGB integers.
 */
@SuppressWarnings("unused")
public final class ColorUtil {
    
    private final static Random RANDOM = new Random();
    
    public final static int
        INVISIBLE_WHITE = 0x00_FF_FF_FF,
        INVISIBLE_BLACK = 0,
        SOLID_BLACK = 0xFF_00_00_00,
        SOLID_RED = 0xFF_FF_00_00,
        SOLID_GREEN = 0xFF_00_FF_00,
        SOLID_BLUE = 0xFF_00_00_FF,
        SOLID_YELLOW = SOLID_RED | SOLID_GREEN,
        SOLID_CYAN = SOLID_GREEN | SOLID_BLUE,
        SOLID_MAGENTA = SOLID_RED | SOLID_BLUE,
        SOLID_WHITE = SOLID_RED | SOLID_GREEN | SOLID_BLUE,
        DEBUG1 = SOLID_MAGENTA,
        DEBUG2 = SOLID_CYAN,
        DEFAULT_TINT = 0xFF_8DB360;
    
    private ColorUtil() {}
    
    // RGB "CONSTRUCTORS"
    
    @Contract(pure = true)
    public static int fromRGB(int r, int g, int b, int a) {
        if (r > 0xFF || g > 0xFF || b > 0xFF || a > 0xFF || r < 0 || g < 0 || b < 0 || a < 0)
            throw new IllegalArgumentException("channel out of range; argb{" + r + ", " + g + ", " + b + ", " + a + "}");
        
        return a << 24 | r << 16 | g << 8 | b;
    }
    
    @Contract(pure = true)
    public static int fromRGB(float r, float g, float b, float a) {
        return fromRGB((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }
    
    @Contract(pure = true)
    public static int fromRGB(float r, float g, float b) {
        return fromRGB(r, g, b, 1);
    }
    
    @Contract(pure = true)
    public static int fromRGB(int r, int g, int b) {
        return fromRGB(r, g, b, 0xFF);
    }
    
    // OTHER "CONSTRUCTORS"
    
    @Contract(pure = true)
    public static int fromHSB(float h, float s, float b, float a) {
        return (fromHSB(h, s, b) & 0x00FFFFFF) | ((int) (a * 255) << 24);
    }
    
    @Contract(pure = true)
    public static int fromHSB(float h, float s, float b) {
        return Color.HSBtoRGB(h, s, b);
    }
    
    public static int applyTint(int rgb, int tint) {
        return fromHSB(hue(tint), saturation(tint), brightness(rgb));
    }
    
    //OPERATIONS
    
    /**
     * <p>
     * Returns the total difference between the components of two colors.
     * <p>
     * This is the sum of the differences of the red, blue, green (and alpha) channels of both colors.
     * <p>
     * With alpha, the maximum difference may be {@code 1020}, without: {@code 765}.
     *
     * @param a first color
     * @param b second color
     * @param alpha true if alpha channel difference is to be measured
     * @return difference between colors
     */
    @Contract(pure = true)
    public static int componentDiff(int a, int b, boolean alpha) {
        return Math.abs(red(a) - red(b)) +
            Math.abs(green(a) - green(b)) +
            Math.abs(blue(a) - blue(b)) +
            (alpha? Math.abs(alpha(a) - alpha(b)) : 0);
    }
    
    /**
     * <p>
     * Returns the visual difference between two colors.
     * <p>
     * This methods is solely to be used for comparison of differences, the returned value itself is mathematically
     * useless, its sole purpose is to be compared with other values returned by this method.
     *
     * @return the visual difference between the colors
     */
    @Contract(pure = true)
    public static int visualDiff(int redA, int grnA, int bluA, int redB, int grnB, int bluB) {
        int redM = (redA + redB) >> 1,
            red = redA - redB,
            grn = grnA - grnB,
            blu = bluA - bluB;
        red = ((512 + redM) * red * red) >> 8;
        grn = 4 * grn * grn;
        blu = ((767 - redM) * blu * blu) >> 8;
        
        return red + grn + blu;
    }
    
    /**
     * <p>
     * Returns the visual difference between two colors.
     * </p>
     * <p>
     * <p>
     * This methods is solely to be used for comparison of differences, the returned value itself is mathematically
     * useless, its sole purpose is to be compared with other values returned by this method.
     * </p>
     *
     * @param a the first color
     * @param b the second color
     * @return the visual difference between the colors
     */
    @Contract(pure = true)
    public static int visualDiff(int a, int b) {
        return visualDiff(red(a), green(a), blue(a), red(b), green(b), blue(b));
    }
    
    /**
     * "Stacks" two colors which means rendering one color in front of another or rendering one layer above another.
     *
     * @param btmR the bottom layer red
     * @param btmG the bottom layer green
     * @param btmB the bottom layer blue
     * @param btmA the bottom layer alpha
     * @param topR the top layer red
     * @param topG the top layer green
     * @param topB the top layer blue
     * @param topA the top layer alpha
     * @return a new rendered color
     */
    @Contract(pure = true)
    public static int stack(float btmR, float btmG, float btmB, float btmA,
                            float topR, float topG, float topB, float topA) {
        final float
            deficit = (1 - topA),
            outA = topA + btmA * deficit;
        
        return fromRGB(
            (topR * topA + btmR * btmA * deficit) / outA,
            (topG * topA + btmG * btmA * deficit) / outA,
            (topB * topA + btmB * btmA * deficit) / outA,
            outA);
    }
    
    /**
     * "Stacks" two colors which means rendering one color in front of another or rendering one layer above another.
     *
     * @param btmR the bottom layer red
     * @param btmG the bottom layer green
     * @param btmB the bottom layer blue
     * @param btmA the bottom layer alpha
     * @param topR the top layer red
     * @param topG the top layer green
     * @param topB the top layer blue
     * @param topA the top layer alpha
     * @return a new rendered color
     */
    @Contract(pure = true)
    public static int stack(int btmR, int btmG, int btmB, int btmA, int topR, int topG, int topB, int topA) {
        return stack(btmR / 255F, btmG / 255F, btmB / 255F, btmA / 255F, topR / 255F, topG / 255F, topB / 255F, topA / 255F);
    }
    
    @Contract(pure = true)
    public static int blend(int a, int b, float weightA) {
        if (weightA < 0 || weightA > 1) throw new IllegalArgumentException("weight out of range (0-1)");
        float weightB = 1 - weightA;
        
        int
            red = (int) (red(a) * weightA + red(b) * weightB),
            grn = (int) (green(a) * weightA + green(b) * weightB),
            blu = (int) (blue(a) * weightA + blue(b) * weightB),
            alp = (int) (alpha(a) * weightA + alpha(b) * weightB);
        
        return fromRGB(red, grn, blu, alp);
    }
    
    @Contract(pure = true)
    public static int scaleRGB(int rgb, float scale) {
        if (scale < 0 || scale > 1) throw new IllegalArgumentException("weight out of range (0-1)");
        
        return fromRGB(
            (int) (red(rgb) * scale),
            (int) (green(rgb) * scale),
            (int) (blue(rgb) * scale),
            alpha(rgb));
    }
    
    /**
     * "Stacks" two colors which means rendering one color in front of another or rendering one layer above another.
     *
     * @param bottom the bottom layer color
     * @param top the top layer color
     * @return a new rendered color
     */
    @Contract(pure = true)
    public static int stack(int bottom, int top) {
        final int topAlpha = alpha(top);
        return topAlpha == 0? bottom : topAlpha == 0xFF? top : stack(
            red(bottom), green(bottom), blue(bottom), alpha(bottom),
            red(top), green(top), blue(top), topAlpha);
    }
    
    // COLOR MODEL CONVERSIONS
    
    /**
     * Converts an rgb color into the <a href="https://en.wikipedia.org/wiki/CIE_1931_color_space">CIE 1931 Color
     * Space</a>.
     *
     * @param r the red value (0-1)
     * @param g the green value(0-1)
     * @param b the blue value (0-1)
     * @return the xyz values
     */
    @NotNull
    public static float[] xyz(float r, float g, float b) {
        final float
            x = 0.4124F * r + 0.3576F * g + 0.1805F * b,
            y = 0.2126F * r + 0.7152F * g + 0.0722F * b,
            z = 0.0193F * r + 0.1192F * g + 0.9505F * b;
        //sum = x + y + z;
        
        return new float[] {x, y, z};
        /*
        if (Spatium.isZero(sum))
            return new float[] {0, 0, z};
        
        return new float[] {x/sum, y/sum, z};
        */
    }
    
    /**
     * Converts an rgb color into the <a href="https://en.wikipedia.org/wiki/CIE_1931_color_space">CIE 1931 Color
     * Space</a>.
     *
     * @param rgb the color
     * @return the xyz values
     */
    @NotNull
    public static float[] xyz(int rgb) {
        return xyz(red(rgb) / 255F, green(rgb) / 255F, blue(rgb) / 255F);
    }
    
    @NotNull
    public static float[] hsb(int r, int g, int b) {
        return Color.RGBtoHSB(r, g, b, null);
    }
    
    @NotNull
    public static float[] hsb(int rgb) {
        return hsb(red(rgb), green(rgb), blue(rgb));
    }
    
    /**
     * Returns an accurate luminance value of a color.
     *
     * @param r the red channel (0-1)
     * @param g the green channel (0-1)
     * @param b the blue channel (0-1)
     * @return the color's luminance
     */
    public static float luminance(float r, float g, float b) {
        //min to compensate for result > 1 due to imprecision
        return Math.min(1F, 0.2126F * r + 0.7152F * g + 0.0722F * b);
    }
    
    /**
     * Returns an accurate luminance value of a color.
     *
     * @param r the red channel (0-0xFF)
     * @param g the green channel (0-0xFF)
     * @param b the blue channel (0-0xFF)
     * @return the color's luminance
     */
    public static float luminance(int r, int g, int b) {
        return luminance(r / 255F, g / 255F, b / 255F);
    }
    
    /**
     * Returns an accurate luminance value of a color.
     *
     * @param rgb an ARGB color
     * @return the color's luminance
     */
    public static float luminance(int rgb) {
        return luminance(red(rgb), green(rgb), blue(rgb));
    }
    
    /**
     * Returns the brightness value of the color.
     *
     * @param rgb the color
     * @return the color's brightness
     */
    public static float brightness(int rgb) {
        return max(red(rgb), green(rgb), blue(rgb)) / 255F;
    }
    
    /**
     * Returns the brightness value of the color.
     *
     * @param r the red channel (0-0xFF)
     * @param g the green channel (0-0xFF)
     * @param b the blue channel (0-0xFF)
     * @return the color's brightness
     */
    @Contract(pure = true)
    public static float saturation(int r, int g, int b) {
        final int
            min = min(r, g, b),
            max = max(r, g, b);
        return (max - min) / (float) max;
    }
    
    /**
     * Returns the brightness value of the color.
     *
     * @param rgb the color
     * @return the color's brightness
     */
    @Contract(pure = true)
    public static float saturation(int rgb) {
        return saturation(red(rgb), green(rgb), blue(rgb));
    }
    
    /**
     * Returns the hue value of the color.
     *
     * @param r the red channel (0-0xFF)
     * @param g the green channel (0-0xFF)
     * @param b the blue channel (0-0xFF)
     * @return the color's hue
     */
    @Contract(pure = true)
    public static float hue(int r, int g, int b) {
        final int
            min = min(r, g, b),
            max = max(r, g, b);
        
        float
            rc = (max - r) / (float) (max - min),
            gc = (max - g) / (float) (max - min),
            bc = (max - b) / (float) (max - min),
            hue;
        
        if (r == max)
            hue = bc - gc;
        else if (g == max)
            hue = 2.0f + rc - bc;
        else
            hue = 4.0f + gc - rc;
        
        hue /= 6.0f;
        return hue < 0? hue + 1.0f : hue;
    }
    
    /**
     * Returns the hue value of the color.
     *
     * @param rgb the color
     * @return the color's hue
     */
    @Contract(pure = true)
    public static float hue(int rgb) {
        return hue(red(rgb), green(rgb), blue(rgb));
    }
    
    /**
     * Returns a fast approximation of the luminance of a color.
     *
     * @param r the red channel (0-0xFF)
     * @param g the green channel (0-0xFF)
     * @param b the blue channel (0-0xFF)
     * @return the color's luminance
     */
    @Contract(pure = true)
    public static int luminance2(int r, int b, int g) {
        return (r + r + r + b + g + g + g + g) >> 3;
    }
    
    /**
     * Returns a fast approximation of the luminance of a color.
     *
     * @param rgb an ARGB color
     * @return the color's luminance
     */
    @Contract(pure = true)
    public static int luminance2(int rgb) {
        return luminance2(red(rgb), green(rgb), blue(rgb));
    }
    
    /**
     * Returns the alpha channel of an ARGB color.
     *
     * @param rgb the color
     * @return the color's alpha channel
     */
    @Contract(pure = true)
    public static int alpha(int rgb) {
        return rgb >> 24 & 0xFF;
    }
    
    /**
     * Returns the red channel of an ARGB color.
     *
     * @param rgb the color
     * @return the color's red channel
     */
    @Contract(pure = true)
    public static int red(int rgb) {
        return rgb >> 16 & 0xFF;
    }
    
    /**
     * Returns the green channel of an ARGB color.
     *
     * @param rgb the color
     * @return the color's green channel
     */
    @Contract(pure = true)
    public static int green(int rgb) {
        return rgb >> 8 & 0xFF;
    }
    
    /**
     * Returns the blue channel of an ARGB color.
     *
     * @param rgb the color
     * @return the color's blue channel
     */
    @Contract(pure = true)
    public static int blue(int rgb) {
        return rgb & 0xFF;
    }
    
    // MISC
    
    /**
     * Returns a random color.
     *
     * @param alpha whether the color should have random alpha (if false, alpha = 255)
     * @return a new random color
     */
    public static int random(boolean alpha) {
        return fromRGB(
            RANDOM.nextInt(256),
            RANDOM.nextInt(255),
            RANDOM.nextInt(255),
            alpha? RANDOM.nextInt(255) : 255);
    }
    
    /**
     * Splits up an ARGB int into its 4 components (alpha, red, green blue)
     *
     * @param rgb the argb value
     * @return an array of components in ARGB order
     */
    @NotNull
    public static int[] argb(int rgb) {
        return new int[] {alpha(rgb), red(rgb), green(rgb), blue(rgb)};
    }
    
    /**
     * Splits up an ARGB int into its 3 components (red, green blue)
     *
     * @param rgb the argb value
     * @return an array of components in ARGB order
     */
    @NotNull
    public static int[] rgb(int rgb) {
        return new int[] {red(rgb), green(rgb), blue(rgb)};
    }
    
    /**
     * Splits up an ARGB int into its 4 components (alpha, red, green blue)
     *
     * @param rgb the argb value
     * @return an array of components in ARGB order
     */
    @NotNull
    public static byte[] bytesARGB(int rgb) {
        return new byte[] {(byte) alpha(rgb), (byte) red(rgb), (byte) green(rgb), (byte) blue(rgb)};
    }
    
    /**
     * Splits up an ARGB int into its 3 components (red, green blue)
     *
     * @param rgb the argb value
     * @return an array of components in RGB order
     */
    @NotNull
    public static byte[] bytesRGB(int rgb) {
        return new byte[] {(byte) red(rgb), (byte) green(rgb), (byte) blue(rgb)};
    }
    
    // TRANSPARENCY
    
    /**
     * Returns the {@link Transparency} of an ARGB integer.
     *
     * @param rgb the rgb value.
     * @return the transparency
     */
    @Contract(pure = true)
    public static int getTransparency(int rgb) {
        if ((rgb & 0xFF_000000) == 0xFF_000000)
            return Transparency.OPAQUE;
        else if ((rgb & 0xFF_000000) == 0)
            return Transparency.BITMASK;
        else
            return Transparency.TRANSLUCENT;
    }
    
    @Contract(pure = true)
    public static boolean isTransparent(int rgb) {
        return (rgb & 0xFF_000000) != 0xFF_000000;
    }
    
    @Contract(pure = true)
    public static boolean isSolid(int rgb) {
        return (rgb & 0xFF_000000) == 0xFF_000000;
    }
    
    @Contract(pure = true)
    public static boolean isVisible(int rgb) {
        return (rgb & 0xFF_000000) != 0;
    }
    
    @Contract(pure = true)
    public static boolean isInvisible(int rgb) {
        return (rgb & 0xFF_000000) == 0;
    }
    
    // UTIL
    
    private static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
    
    private static int max(int a, int b, int c) {
        return Math.max(Math.max(a, b), c);
    }
    
}
