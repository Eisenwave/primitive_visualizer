package eisenwave.primvis.text;

import eisenwave.primvis.util.TextUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;

public class TrueType {
    
    private final int
        bitmapId,
        bitmapWidth,
        bitmapHeight,
        height;
    
    private final float
        ascent,
        descent,
        lineGap;
    
    private final STBTTBakedChar.Buffer chardata = new STBTTBakedChar.Buffer(
        MemoryUtil.memAlloc(STBTTBakedChar.SIZEOF * 96)
    );
    
    public TrueType(ByteBuffer ttf, int bitmapWidth, int bitmapHeight, int fontHeight) {
        STBTTFontinfo info = STBTTFontinfo.create();
        if (!stbtt_InitFont(info, ttf))
            throw new IllegalStateException("Failed to initialize font information.");
        
        this.height = fontHeight;
        float scale = stbtt_ScaleForPixelHeight(info, fontHeight);
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pAscent = stack.mallocInt(1);
            IntBuffer pDescent = stack.mallocInt(1);
            IntBuffer pLineGap = stack.mallocInt(1);
            
            stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);
            
            ascent = pAscent.get(0) * scale;
            descent = pDescent.get(0) * scale;
            lineGap = pLineGap.get(0) * scale;
            System.out.println(ascent + " " + descent + " " + lineGap);
        }
        
        this.bitmapWidth = bitmapWidth;
        this.bitmapHeight = bitmapHeight;
        
        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitmapWidth * bitmapHeight);
        //printGrayScaleBuffer(bitmap, "buffer-before");
        
        // no guarantee this fits!
        stbtt_BakeFontBitmap(ttf, fontHeight, bitmap, bitmapWidth, bitmapHeight, TextUtil.MIN_CHARACTER, chardata);
        //for (int i = 0; i < bitmap.capacity(); i++)
        //    bitmap.put(i, (byte) (ThreadLocalRandom.current().nextBoolean()? 255 : 0));
        bitmap.position(0);
        //bitmap.flip();
        
        //printGrayScaleBuffer(bitmap, "buffer-after");
        //bitmap.position(0);
        
        bitmapId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, bitmapId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, bitmapWidth, bitmapHeight, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }
    
    public int getBitmapWidth() {
        return bitmapWidth;
    }
    
    public int getBitmapHeight() {
        return bitmapHeight;
    }
    
    public int getBitmapId() {
        return bitmapId;
    }
    
    public int getHeight() {
        return height;
    }
    
    /**
     * Returns the coordinate above the baseline to which the font extends.
     *
     * @return the ascent
     */
    public float getAscent() {
        return ascent;
    }
    
    /**
     * Returns the coordinate below the baseline to which the font extends. This number is typically negative.
     *
     * @return the descent
     */
    public float getDescent() {
        return descent;
    }
    
    /**
     * Returns the gap between the ascent and descent of two lines.
     *
     * @return the line gap
     */
    public float getLineGap() {
        return lineGap;
    }
    
    public STBTTBakedChar.Buffer getCharacterData() {
        return chardata;
    }
}
