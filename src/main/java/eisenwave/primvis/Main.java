package eisenwave.primvis;

import eisenwave.primvis.util.ColorUtil;
import eisenwave.primvis.util.KeyboardUtil;
import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class Main {
    
    private final static boolean PERSPECTIVE = true;
    private final static int WIDTH = 1280, HEIGHT = 720;
    private final static int FPS = 60;
    //private final static int FRAME_DURATION = 1_000_000_000 / FPS;
    private final static float CAMERA_SPEED = 0.1f;
    
    private static float aspect = (float) WIDTH / HEIGHT;
    private static long fps;
    
    private static Camera camera;
    
    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("java.library.path"));
        System.setProperty("sun.java2d.noddraw", Boolean.TRUE.toString());
        //System.out.println("Hello LWJGL " + getVersion() + "!");
        
        initDisplay();
        initGL();
        loop();
        
    }
    
    private static void initDisplay() throws LWJGLException {
        Display.create();
        Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
        Display.setTitle("Primitive Visualizer");
        Display.setVSyncEnabled(true);
        Display.setResizable(true);
    }
    
    private static void initGL() {
        glClearColor(0.1f, 0.1f, 0.1f, 1f);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_LINE_SMOOTH);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);
        
        camera = new Camera(0, 0);
        handleResize();
    }
    
    private static void handleResize() {
        int width = Display.getWidth(), height = Display.getHeight();
        aspect = (float) width / height;
    
        glMatrixMode(GL_PROJECTION);
        if (PERSPECTIVE)
            GLU.gluPerspective(90f, aspect, 0.001f, 100f);
        else
            GL11.glOrtho(-aspect, aspect, -1, 1, 0.001f, 100f);
        
        glViewport(0, 0, width, height);
    }
    
    private static void loop() {
        //long lastDrawTime = System.nanoTime();
        long fpsTime = System.nanoTime();
        int fpsCounter = 0;
        
        while (shouldContinue()) {
            long now = System.nanoTime();
            
            if (Display.wasResized()) {
                handleResize();
            }
            
            handleInput();
            render3();
            Display.update();
            Display.sync(FPS);
            
            fpsCounter++;
            if (now - fpsTime >= 1_000_000_000) {
                fpsTime = now;
                fps = fpsCounter;
                fpsCounter = 0;
                System.out.println(fps + "fps");
            }
        }
        
        Display.destroy();
    }
    
    private static void handleInput() {
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
            camera.translate(0, CAMERA_SPEED, 0);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
            camera.translate(0, -CAMERA_SPEED, 0);
        if (Keyboard.isKeyDown(Keyboard.KEY_W))
            camera.translateForwardXZ(CAMERA_SPEED);
        if (Keyboard.isKeyDown(Keyboard.KEY_A))
            camera.translateLeftXZ(CAMERA_SPEED);
        if (Keyboard.isKeyDown(Keyboard.KEY_S))
            camera.translateBackwardXZ(CAMERA_SPEED);
        if (Keyboard.isKeyDown(Keyboard.KEY_D))
            camera.translateRightXZ(CAMERA_SPEED);
        if (Keyboard.isKeyDown(Keyboard.KEY_X))
            camera.translate(KeyboardUtil.isControlDown()? -CAMERA_SPEED : CAMERA_SPEED, 0, 0);
        if (Keyboard.isKeyDown(Keyboard.KEY_Y))
            camera.translate(0, KeyboardUtil.isControlDown()? -CAMERA_SPEED : CAMERA_SPEED, 0);
        if (Keyboard.isKeyDown(Keyboard.KEY_Z))
            camera.translate(0, 0, KeyboardUtil.isControlDown()? -CAMERA_SPEED : CAMERA_SPEED);
        else if (Keyboard.isKeyDown(Keyboard.KEY_C))
            System.out.println(camera);
        
        int dx = Mouse.getDX(), dy = Mouse.getDY();
        
        if (Mouse.isButtonDown(0)) {
            camera.rotate(-0.1f * dx, -0.1f * dy);
        }
    }
    
    private static boolean shouldContinue() {
        return !Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
    }
    
    private static void render3() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        //glRotatef(30f, 1, 0, 0);
        
        glLineWidth(1);
        glColor3f(0.5f, 0.5f, 0.5f);
        drawYGrid(16);
        glLineWidth(5);
        drawCoordinateLines();
        
        
        
        
        //glRotatef(rotation += 0.1f, 0, 1, 0);
        //glTranslatef(0f, 0.0f, -7f);
        //glColor3f(0.5f, 0.5f, 1.0f);
        
        //drawLine(-1, -1, 1, 1);
        //drawLine(-1, 1, 1, -1);
        
        drawPolygon(ColorUtil.SOLID_RED,
            new Vector3f(-0.5f, -0.5f, -0.5f),
            new Vector3f(-0.5f, -0.5f, 0.5f),
            new Vector3f(-0.5f, 0.5f, 0.5f),
            new Vector3f(-0.5f, 0.5f, -0.5f));
        drawPolygon(ColorUtil.SOLID_CYAN,
            new Vector3f(0.5f, -0.5f, -0.5f),
            new Vector3f(0.5f, 0.5f, -0.5f),
            new Vector3f(0.5f, 0.5f, 0.5f),
            new Vector3f(0.5f, -0.5f, 0.5f));
        
        drawPolygon(ColorUtil.SOLID_GREEN,
            new Vector3f(-0.5f, -0.5f, -0.5f),
            new Vector3f(0.5f, -0.5f, -0.5f),
            new Vector3f(0.5f, -0.5f, 0.5f),
            new Vector3f(-0.5f, -0.5f, 0.5f));
        drawPolygon(ColorUtil.SOLID_MAGENTA,
            new Vector3f(-0.5f, 0.5f, -0.5f),
            new Vector3f(-0.5f, 0.5f, 0.5f),
            new Vector3f(0.5f, 0.5f, 0.5f),
            new Vector3f(0.5f, 0.5f, -0.5f));
    
        drawPolygon(ColorUtil.SOLID_BLUE,
            new Vector3f(-0.5f, -0.5f, -0.5f),
            new Vector3f(-0.5f, 0.5f, -0.5f),
            new Vector3f(0.5f, 0.5f, -0.5f),
            new Vector3f(0.5f, -0.5f, -0.5f));
        drawPolygon(ColorUtil.SOLID_YELLOW,
            new Vector3f(-0.5f, -0.5f, 0.5f),
            new Vector3f(0.5f, -0.5f, 0.5f),
            new Vector3f(0.5f, 0.5f, 0.5f),
            new Vector3f(-0.5f, 0.5f, 0.5f));
        
        /*
        
        drawLine(10, 10, 100, 100);
        drawLine(10, 10, 0, 100);
        
        drawQuad(100, 100, 100, 200, 200, 200, 200, 100);
        int w = Display.getWidth(), h = Display.getHeight();
        glColor3f(1, 0, 0);
        drawQuad(w - 100, 0, w - 1, 0, w - 1, h - 1, w - 100, h - 1);
        */
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
    
    private static void drawPolygon(int rgb, Vector3f... vectors) {
        glBegin(GL_QUADS);
        colorRGB(rgb);
        for (Vector3f v : vectors)
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
    
    private static void drawQuad(int ax, int ay, int bx, int by, int cx, int cy, int dx, int dy) {
        glBegin(GL_POLYGON);
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
