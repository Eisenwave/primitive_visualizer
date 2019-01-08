package eisenwave.primvis.app;

import eisenwave.primvis.util.MathUtil;
import org.lwjgl.opengl.GL11;

import static eisenwave.primvis.gl.GLU.gluPerspective;
import static org.lwjgl.opengl.GL11.*;

public class Camera {
    
    private final Window window;
    
    private Projection projection;
    private double x, y, z;
    private float yaw, pitch;
    private float fov;
    
    public Camera(Window window, Projection projection, double x, double y, double z,
                  float yaw, float pitch, float fov) {
        this.window = window;
        this.projection = projection;
        this.x = x;
        this.y = y;
        this.z = z;
        this.fov = fov;
        setYawPitch(yaw, pitch);
    }
    
    public Camera(Window window, Projection projection, float yaw, float pitch, float fov) {
        this(window, projection, 0, 0, 0, yaw, pitch, fov);
    }
    
    // GETTERS
    
    public Projection getProjection() {
        return projection;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public float getFieldOfView() {
        return fov;
    }
    
    // MUTATORS
    
    public void setFieldOfView(float fov) {
        if (fov <= 0)
            throw new IllegalArgumentException("FOV must be > 0");
        this.fov = fov;
        refreshProjection();
    }
    
    public void translate(double x, double y, double z) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glTranslated(-x, -y, -z);
        
        this.x += x;
        this.y += y;
        this.z += z;
    }
    
    public void translateForward(double distance) {
        double[] xyz = MathUtil.rypToXYZ(distance, Math.toRadians(yaw), Math.toRadians(pitch));
        translate(xyz[0], xyz[1], xyz[2]);
    }
    
    public void translateBackward(double distance) {
        translateForward(-distance);
    }
    
    public void translateForwardXZ(double distance) {
        double[] xz = MathUtil.yawToXZ(Math.toRadians(yaw));
        translate(xz[0] * distance, 0, xz[1] * distance);
    }
    
    public void translateBackwardXZ(double distance) {
        translateForwardXZ(-distance);
    }
    
    public void translateLeftXZ(double distance) {
        double[] xz = MathUtil.yawToXZ(Math.toRadians(yaw));
        translate(xz[1] * distance, 0, -xz[0] * distance);
    }
    
    public void translateRightXZ(double distance) {
        translateLeftXZ(-distance);
    }
    
    public void rotate(float yaw, float pitch) {
        setYawPitch(this.yaw + yaw, this.pitch + pitch);
    }
    
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        
        refreshModelView();
    }
    
    public void setYawPitch(float yaw, float pitch) {
        this.yaw = MathUtil.normalize360(yaw);
        this.pitch = Math.max(-90, Math.min(90, pitch));
        
        refreshModelView();
    }
    
    public void refreshModelView() {
        glMatrixMode(GL11.GL_MODELVIEW);
        glLoadIdentity();
        
        glRotatef(pitch, 1, 0, 0);
        glRotatef(yaw + 180, 0, 1, 0);
        glTranslated(-x, -y, -z);
    }
    
    public void refreshProjection() {
        glMatrixMode(GL11.GL_PROJECTION);
        glLoadIdentity();
        float aspect = window.getAspectRatio();
        if (projection == Projection.PERSPECTIVE)
            gluPerspective((float) Math.toRadians(fov), window.getAspectRatio(), 0.001f, 100f);
        else if (projection == Projection.ORTHOGONAL)
            glOrtho(-aspect, aspect, -1, 1, 0.001f, 100f);
    }
    
    @Override
    public String toString() {
        return String.format("Camera{pos: [%.2f, %.2f, %.2f], yaw: %.2f, pitch: %.2f}", x, y, z, yaw, pitch);
    }
    
}
