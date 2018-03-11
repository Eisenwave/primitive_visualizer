package eisenwave.primvis;

import eisenwave.primvis.util.ColorUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class TextLayer {
    
    private BufferedImage textLayer;
    private Graphics2D graphics;
    
    private final int x, y;
    private final Color background;
    
    /**
     * Initializes a new instance of TextLayer
     *
     * @param x the x-offset
     * @param y the y-offset
     * @param width of the viewport
     * @param height of the viewport
     */
    public TextLayer(int x, int y, int width, int height, Color background) {
        this.x = x;
        this.y = y;
        this.background = background;
        
        this.textLayer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.graphics = (Graphics2D) textLayer.getGraphics();
    }
    
    /**
     * Initializes a new instance of TextLayer
     *
     * @param x the x-offset
     * @param y the y-offset
     * @param width of the viewport
     * @param height of the viewport
     */
    public TextLayer(int x, int y, int width, int height) {
        this(x, y, width, height, new Color(255, 255, 255, 0));
    }
    
    public void print(String output, float x, float y, Font font) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(font);
        graphics.drawString(output, x, y);
    }
    
    public void flushText() {
        glEnable(GL_TEXTURE_2D);
        
        final int maxX = x + textLayer.getWidth() - 1, maxY = y + textLayer.getHeight() - 1;
        
        loadTexture(textLayer);
        graphics.setBackground(background);
        graphics.clearRect(0, 0, textLayer.getWidth(), textLayer.getHeight());
        //System.out.println(Long.toHexString(Integer.toUnsignedLong(textLayer.getRGB(0, 0))));
        
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1f);
        glVertex2f(x, maxY);
        glTexCoord2f(1f, 1f);
        glVertex2f(maxX, maxY);
        glTexCoord2f(1f, 0);
        glVertex2f(maxX, y);
        glTexCoord2f(0, 0);
        glVertex2f(x, y);
        glEnd();
        
        glDisable(GL_TEXTURE_2D);
    }
    
    public static int loadTexture(BufferedImage image) {
        ByteBuffer buffer = bufferRGBA(image);
        
        int textureID = glGenTextures(); // Generate texture ID
        glBindTexture(GL_TEXTURE_2D, textureID); // Bind texture ID
        
        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        
        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        
        //Return the texture ID so we can bind it later again
        return textureID;
    }
    
    private static final int BYTES_PER_PIXEL = 4;
    
    public static ByteBuffer bufferRGBA(BufferedImage image) {
        final int width = image.getWidth(), height = image.getHeight();
        
        int[] argbBuffer = new int[width * height];
        image.getRGB(0, 0, width, height, argbBuffer, 0, width);
    
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * BYTES_PER_PIXEL);
    
        for (int pixel : argbBuffer) {
            buffer.put((byte) ColorUtil.red(pixel));
            buffer.put((byte) ColorUtil.green(pixel));
            buffer.put((byte) ColorUtil.blue(pixel));
            //buffer.put((byte) 0);
            //buffer.put((byte) 127);
            buffer.put((byte) ColorUtil.alpha(pixel));
            
            //if (ColorUtil.red(pixel) == 0)
            //    System.out.println(ColorUtil.alpha(pixel));
        }
    
        buffer.flip();
        return buffer;
    }
    
}
