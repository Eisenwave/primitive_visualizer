package eisenwave.primvis.util;

import eisenwave.primvis.text.TrueType;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.*;

public class TextUtil {
    
    public final static int MIN_CHARACTER = 32;
    
    private static final File FONT_DIRECTORY = new File("/usr/share/fonts/truetype/");
    
    public static File getFontFile(String font) {
        return new File(FONT_DIRECTORY, font + ".ttf");
    }
    
    /**
     * Scales a given offset relative to the given center instead of the origin.
     *
     * @param center the center of scaling
     * @param offset the offset to be scaled
     * @param factor the factor by which to scale
     * @return the offset scaled relative to the origin
     */
    private static float scale(float center, float offset, float factor) {
        return (offset - center) * factor + center;
    }
    
    public static void printTextAtCoordinates(TrueType font, final float offX, final float offY, String text) {
        printTextAtBaseline(font, offX, offY + font.getAscent(), text);
        //System.out.println(font.getAscent());
    }
    
    public static void printTextAtBaseline(TrueType font, final float offX, final float baseY, String text) {
        // assume orthographic projection with units = screen pixels, origin at top left
        
        final float
            ascent = font.getAscent(),
            descent = font.getDescent(),
            lineGap = font.getLineGap();
        
        float scaleX = 1f, scaleY = 1f, lineY = baseY;
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            
            FloatBuffer x = stack.floats(offX);
            @SuppressWarnings("SuspiciousNameCombination")
            FloatBuffer y = stack.floats(baseY);
            
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, font.getBitmapId());
            glBegin(GL_QUADS);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                
                if (c == '\n') {
                    x.put(0, offX);
                    y.put(0, lineY = y.get(0) + (ascent - descent + lineGap) /* * scale*/);
                    continue;
                }
                else if (c < 32 || 128 <= c) continue;
                
                float charX = x.get(0);
                STBTTAlignedQuad q = new STBTTAlignedQuad(memAlloc(STBTTAlignedQuad.SIZEOF));
                stbtt_GetBakedQuad(font.getCharacterData(), font.getBitmapWidth(), font.getBitmapHeight(),
                    c - MIN_CHARACTER, x, y, q, true);
                x.put(0, scale(charX, x.get(0), scaleX));
                
                //float cpX = x;
                
                float
                    x0 = scale(offX, q.x0(), scaleX),
                    x1 = scale(offX, q.x1(), scaleX),
                    y0 = scale(lineY, q.y0(), scaleY),
                    y1 = scale(lineY, q.y1(), scaleY);
                
                glTexCoord2f(q.s0(), q.t0()); glVertex2f(x0, y0);
                glTexCoord2f(q.s0(), q.t1()); glVertex2f(x0, y1);
                glTexCoord2f(q.s1(), q.t1()); glVertex2f(x1, y1);
                glTexCoord2f(q.s1(), q.t0()); glVertex2f(x1, y0);
            }
            glEnd();
        }
        
    }
    
    public static void renderTextBitmap(TrueType font, float x, float y) {
        glBindTexture(GL_TEXTURE_2D, font.getBitmapId());
        glEnable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        {
            glTexCoord2f(0, 0); glVertex2f(x + 0, y + 0);
            glTexCoord2f(0, 1); glVertex2f(x + 0, y + 512);
            glTexCoord2f(1, 1); glVertex2f(x + 512, y + 512);
            glTexCoord2f(1, 0); glVertex2f(x + 512, y + 0);
        }
        glEnd();
    }
    
}
