package eisenwave.primvis;

import eisenwave.primvis.app.*;
import eisenwave.primvis.app.Window;
import eisenwave.primvis.text.TextLayer;
import eisenwave.primvis.text.TrueType;
import eisenwave.primvis.util.ColorUtil;
import eisenwave.primvis.util.KeyboardUtil;
import eisenwave.primvis.util.MathUtil;
import eisenwave.primvis.util.TextUtil;
import eisenwave.torrens.object.Vertex3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
// import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    
    private final static boolean VSYNC = true;
    private final static int WIDTH = 1280, HEIGHT = 720;
    private final static float CAMERA_SPEED = 0.1f;
    
    private static long fps;
    public static Window window;
    private static Camera camera;
    private static Mouse mouse;
    
    private static TextLayer fpsTextLayer = new TextLayer(8, 8, 80, 48);
    private static TextLayer camTextLayer = new TextLayer(8, 32, 140, 100);
    
    private static TrueType dejaVuSansMono;
    
    // allocate one MiB
    private final static int BITMAP_WIDTH = 512, BITMAP_HEIGHT = 512;
    
    // ASCII 32..126 is 95 glyphs
    //GLuint ftex;
    
    private static int[] pixelBufferFromRGB(ByteBuffer buffer) {
        int[] result = new int[buffer.limit() / 3];
        for (int i = 0, j = 0; i < result.length; i++) {
            result[i] = ColorUtil.fromRGB(
                buffer.get(j++),
                buffer.get(j++),
                buffer.get(j++));
        }
        return result;
    }
    
    private static int[] toInts(ByteBuffer bytes) {
        int[] result = new int[bytes.capacity()];
        for (int i = 0; i < result.length; i++)
            result[i] = bytes.get(i);
        return result;
    }
    
    public static void main(String[] args) throws Exception {
        // System.out.println(System.getProperty("java.library.path"));
        //System.setProperty("sun.java2d.noddraw", Boolean.TRUE.toString());
        //System.out.println("Hello LWJGL " + getVersion() + "!");
        
        init();
        loop();
        destroy();
    }
    
    /* private static void initFonts() throws SlickException {
        SLICK_FONT = new org.newdawn.slick.UnicodeFont(FONT);
        SLICK_FONT.addAsciiGlyphs();
        //noinspection unchecked
        SLICK_FONT.getEffects().add(new ColorEffect());
        SLICK_FONT.loadGlyphs();
    } */
    
    private static void init() throws IOException {
        GLFWErrorCallback.createPrint(System.err).set();
        
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        
        initGLFW();
        initInput();
        initGL();
        initFonts();
        //initFonts();
    }
    
    private static void loop() {
        //long lastDrawTime = System.nanoTime();
        
        long fpsTime = System.nanoTime();
        int fpsCounter = 0;
        
        //while (!glfwWindowShouldClose(window)) {
        while (!glfwWindowShouldClose(window.getId())) {
            glfwPollEvents();
            handleInput();
            
            /* if (windowDragged) {
                windowDragged = false;
                Thread.sleep(20);
                continue;
            }*/
            
            long now = System.nanoTime();
            
            render3();
            render2();
            glfwSwapBuffers(window.getId());
            
            fpsCounter++;
            if (now - fpsTime >= 1_000_000_000) {
                fpsTime = now;
                fps = fpsCounter;
                fpsCounter = 0;
            }
        }
    }
    
    private static void destroy() {
        // Free the window callbacks and destroy the window
        //  glfwFreeCallbacks(window);
        glfwDestroyWindow(window.getId());
        
        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
    
    private static void initGLFW() {
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        
        long windowId = glfwCreateWindow(WIDTH, HEIGHT, "Primitive Visualizer", NULL, NULL);
        if (windowId == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        window = new Window(windowId, WIDTH, HEIGHT);
        
        // Get the thread stack and push a new frame
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            
            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowId, pWidth, pHeight);
            
            // Get the resolution of the primary monitor
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (videoMode == null)
                throw new RuntimeException("failed to get primary monitor resolution");
            
            // Center the window
            glfwSetWindowPos(
                window.getId(),
                (videoMode.width() - pWidth.get(0)) / 2,
                (videoMode.height() - pHeight.get(0)) / 2
            );
        }
        
        // Make the OpenGL context current
        glfwMakeContextCurrent(window.getId());
        
        // Make the window visible
        glfwShowWindow(window.getId());
    }
    
    private static void initGL() {
        GL.createCapabilities();
        glClearColor(0.1f, 0.1f, 0.1f, 1f);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // Enable v-sync
        glfwSwapInterval(VSYNC? 1 : 0);
        
        camera = new Camera(window, Projection.PERSPECTIVE, 0, 0, 70);
        handleResize(window.getId(), WIDTH, HEIGHT);
    }
    
    private static void initFonts() throws IOException {
        //int index = stbtt_FindMatchingFont(ttf_buffer, "monospaced", STBTT_MACSTYLE_NONE);
        ByteBuffer ttf;
        
        Path path = TextUtil.getFontFile("dejavu/DejaVuSansMono").toPath();
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            Files.copy(path, stream);
            byte[] bytes = stream.toByteArray();
            ttf = BufferUtils.createByteBuffer(bytes.length);
            ttf.put(bytes);
        }
        //System.out.println(ttf.position());
        ttf.flip();
        //printFile(ttf, "ttf", ".tff");
    
        dejaVuSansMono = new TrueType(ttf, BITMAP_WIDTH, BITMAP_HEIGHT, 16);
    }
    
    private static void printFile(ByteBuffer buffer, String prefix, String suffix) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(File.createTempFile(prefix, suffix))) {
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            buffer.flip();
            stream.write(bytes);
        }
    }
    
    private static void printGrayScaleBuffer(ByteBuffer buffer, String prefix) throws IOException {
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_BYTE_GRAY);
        image.setRGB(0, 0, 512, 512, toInts(buffer), 0, 512);
        ImageIO.write(image, "bmp", File.createTempFile(prefix, ".bmp"));
    }
    
    @SuppressWarnings("unused")
    private static void handleResize(long window, int width, int height) {
        if (window == Main.window.getId()) {
            float aspect = (float) width / height;
            
            camera.refreshProjection();
            
            glViewport(0, 0, width, height);
            
            Main.window.setSize(width, height);
        }
    }
    
    @SuppressWarnings("unused")
    private static void handleKey(long window, int key, int scancode, int action, int mods) {
        //System.out.println(key);
        if (action == GLFW_PRESS) switch (key) {
            case GLFW_KEY_C:
                System.out.println(camera);
                break;
            
            case GLFW_KEY_F:
                System.out.println("FPS = " + fps);
                break;
            
            case GLFW_KEY_ESCAPE:
                glfwSetWindowShouldClose(Main.window.getId(), true);
                break;
        }
        
        /*
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
            camera.translate(0, -CAMERA_SPEED, 0);
         */
    }
    
    @SuppressWarnings("unused")
    private static void handleMouseButton(long window, int button, int action, int mods) {
        mouse.setLeftDown(action != GLFW_RELEASE);
    }
    
    private static void handleMousePos(long window, double x, double y) {
        // System.out.println(x + " " + y);
        
        if (window == Main.window.getId()) {
            if (mouse.isLeftDown()) {
                double dx = x - mouse.getX();
                double dy = y - mouse.getY();
                
                camera.rotate(-0.1f * (float) dx, -0.1f * (float) dy);
            }
            
            mouse.setPos(x, y);
        }
    }
    
    private static void handleInput() {
        long windowId = window.getId();
        
        if (glfwGetKey(windowId, GLFW_KEY_SPACE) == GLFW_PRESS)
            camera.translate(0, CAMERA_SPEED, 0);
        
        if (KeyboardUtil.isShiftDown(windowId))
            camera.translate(0, -CAMERA_SPEED, 0);
        
        if (glfwGetKey(windowId, GLFW_KEY_W) == GLFW_PRESS)
            camera.translateForwardXZ(CAMERA_SPEED);
        
        if (glfwGetKey(windowId, GLFW_KEY_A) == GLFW_PRESS)
            camera.translateLeftXZ(CAMERA_SPEED);
        
        if (glfwGetKey(windowId, GLFW_KEY_S) == GLFW_PRESS)
            camera.translateBackwardXZ(CAMERA_SPEED);
        
        if (glfwGetKey(windowId, GLFW_KEY_D) == GLFW_PRESS)
            camera.translateRightXZ(CAMERA_SPEED);
        
        //if (glfwGetKey(window, GLFW_KEY_PERIOD) == GLFW_PRESS)
        //    System.out.println('.');
    }
    
    private static void handleScroll(long windowId, @SuppressWarnings("unused") double dx, double dy) {
        if (windowId == window.getId()) {
            //System.out.println(dy);
            float newFov = MathUtil.clamp(30, camera.getFieldOfView() - 3 * (float) dy, 120);
            //System.out.println(camera.getFieldOfView() + " -> " + newFov);
            camera.setFieldOfView(newFov);
        }
    }
    
    /* @SuppressWarnings("unused")
    private static void handleWindowPos(long window, double x, double y) {
        // System.out.println(x + " " + y);
        
        windowDragged = true;
    } */
    
    private static void initInput() {
        long windowId = window.getId();
        
        mouse = new Mouse();
        
        glfwSetKeyCallback(windowId, Main::handleKey);
        glfwSetMouseButtonCallback(windowId, Main::handleMouseButton);
        glfwSetCursorPosCallback(windowId, Main::handleMousePos);
        glfwSetWindowSizeCallback(windowId, Main::handleResize);
        glfwSetScrollCallback(windowId, Main::handleScroll);
        //glfwSetWindowPosCallback(window, Main::handleWindowPos);
    }
    
    /*
    private static boolean shouldContinue() {
        return !Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
    }
    */
    
    private static void render3() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_TEXTURE_2D);
        
        glLineWidth(1);
        glColor3f(0.5f, 0.5f, 0.5f);
        drawYGrid(16);
        glLineWidth(5);
        drawCoordinateLines();
        
        drawPolygon(ColorUtil.SOLID_RED,
            new Vertex3f(-0.5f, -0.5f, -0.5f),
            new Vertex3f(-0.5f, -0.5f, 0.5f),
            new Vertex3f(-0.5f, 0.5f, 0.5f),
            new Vertex3f(-0.5f, 0.5f, -0.5f));
        drawPolygon(ColorUtil.SOLID_CYAN,
            new Vertex3f(0.5f, -0.5f, -0.5f),
            new Vertex3f(0.5f, 0.5f, -0.5f),
            new Vertex3f(0.5f, 0.5f, 0.5f),
            new Vertex3f(0.5f, -0.5f, 0.5f));
        
        drawPolygon(ColorUtil.SOLID_GREEN,
            new Vertex3f(-0.5f, -0.5f, -0.5f),
            new Vertex3f(0.5f, -0.5f, -0.5f),
            new Vertex3f(0.5f, -0.5f, 0.5f),
            new Vertex3f(-0.5f, -0.5f, 0.5f));
        drawPolygon(ColorUtil.SOLID_MAGENTA,
            new Vertex3f(-0.5f, 0.5f, -0.5f),
            new Vertex3f(-0.5f, 0.5f, 0.5f),
            new Vertex3f(0.5f, 0.5f, 0.5f),
            new Vertex3f(0.5f, 0.5f, -0.5f));
        
        drawPolygon(ColorUtil.SOLID_BLUE,
            new Vertex3f(-0.5f, -0.5f, -0.5f),
            new Vertex3f(-0.5f, 0.5f, -0.5f),
            new Vertex3f(0.5f, 0.5f, -0.5f),
            new Vertex3f(0.5f, -0.5f, -0.5f));
        drawPolygon(ColorUtil.SOLID_YELLOW,
            new Vertex3f(-0.5f, -0.5f, 0.5f),
            new Vertex3f(0.5f, -0.5f, 0.5f),
            new Vertex3f(0.5f, 0.5f, 0.5f),
            new Vertex3f(-0.5f, 0.5f, 0.5f));
        
        //glMatrixMode(GL_PROJECTION);
        //glPopMatrix();
    }
    
    private static void render2() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, window.getWidth(), window.getHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        
        glClear(GL_DEPTH_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        /* glBegin(GL_QUADS);
        glColor3f(1, 1, 0);
        drawRectangle(0, 0, 16, 16);
        glEnd(); */
        
        glColor3f(1, 0, 0);
        //drawQuad(0, 0, 0, 16, 16, 16, 16, 0);
        
        //TextUtil.renderTextBitmap(dejaVuSansMono, 256, 256);
        drawString(0, 0, 0xFFFF00, (fps < 10? "0" + fps : fps) + " FPS");
        
        drawFormatString(32, 32, 0x7F7F7F, "X: %.2f\nY: %.2f\nZ: %.2f\nYaw:   %.2f\nPitch: %.2f",
            camera.getX(), camera.getY(), camera.getZ(), camera.getYaw(), camera.getPitch());
        
        glEnable(GL_DEPTH_TEST);
        
        // Making sure we can render 3d again
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }
    
    private static void drawFormatString(int x, int y, int rgb, String str, Object... args) {
        drawString(x, y, rgb, String.format(str, args));
    }
    
    private static void drawString(int x, int y, int rgb, String str) {
        //SLICK_FONT.drawString(x, y, str, color);
        colorRGB(rgb);
        TextUtil.printTextAtCoordinates(dejaVuSansMono, x, y, str);
    }
    
    @SuppressWarnings("SameParameterValue")
    private static void drawYGrid(int size) {
        for (int x = 0; x <= size; x++) {
            drawLine(x, 0, 0, x, 0, size);
        }
        for (int z = 0; z <= size; z++) {
            drawLine(0, 0, z, size, 0, z);
        }
    }
    
    private static void drawCoordinateLines() {
        glColor3f(1, 0, 0);
        drawLine(0, 0, 0, 1, 0, 0);
        glColor3f(0, 1, 0);
        drawLine(0, 0, 0, 0, 1, 0);
        glColor3f(0, 0, 1);
        drawLine(0, 0, 0, 0, 0, 1);
    }
    
    private static void drawPolygon(int rgb, Vertex3f... vectors) {
        glBegin(GL_QUADS);
        colorRGB(rgb);
        for (Vertex3f v : vectors)
            glVertex3f(v.getX(), v.getY(), v.getZ());
        glEnd();
    }
    
    @SuppressWarnings("SameParameterValue")
    private static void drawLine(double x0, double y0, double z0, double x1, double y1, double z1) {
        glBegin(GL_LINES);
        glVertex3d(x0, y0, z0);
        glVertex3d(x1, y1, z1);
        glEnd();
    }
    
    @SuppressWarnings("SameParameterValue")
    private static void drawLine(int minX, int minY, int maxX, int maxY) {
        glBegin(GL_LINES);
        glVertex2i(minX, minY);
        glVertex2i(maxX, maxY);
        glEnd();
    }
    
    private static void drawRectangle(int minX, int minY, int maxX, int maxY) {
        drawQuad(minX, minY, minX, maxY, maxX, maxY, maxX, minY);
    }
    
    private static void drawQuad(int ax, int ay, int bx, int by, int cx, int cy, int dx, int dy) {
        glBegin(GL_QUADS);
        glVertex2i(ax, ay);
        glVertex2i(bx, by);
        glVertex2i(cx, cy);
        glVertex2i(dx, dy);
        glEnd();
    }
    
    private static void colorRGB(int rgb) {
        byte[] components = ColorUtil.bytesRGB(rgb);
        glColor3ub(components[0], components[1], components[2]);
    }
    
}
