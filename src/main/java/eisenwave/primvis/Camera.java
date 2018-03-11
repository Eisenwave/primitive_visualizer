package eisenwave.primvis;

import eisenwave.primvis.util.MathUtil;
import org.lwjgl.opengl.GL11;

public class Camera {
    
    private double x, y, z;
    private float yaw, pitch;
    
    public Camera(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        setYawPitch(yaw, pitch);
    }
    
    public Camera(float yaw, float pitch) {
        this(0, 0, 0, yaw, pitch);
    }
    
    public Camera() {
        this.x = this.y = this.z = 0d;
        this.yaw = this.pitch = 0f;
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }
    
    // GETTERS
    
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
    
    // MUTATORS
    
    public void translate(double x, double y, double z) {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glTranslated(-x, -y, -z);
        
        this.x += x;
        this.y += y;
        this.z += z;
    }
    
    public void translateForwardXZ(double distance) {
        double yawRadians = Math.toRadians(yaw);
        translate(Math.sin(-yawRadians) * distance, 0, Math.cos(yawRadians) * distance);
    }
    
    public void translateBackwardXZ(double distance) {
        translateForwardXZ(-distance);
    }
    
    public void translateLeftXZ(double distance) {
        double yawRadians = Math.toRadians(yaw);
        translate(Math.cos(yawRadians) * distance, 0, -Math.sin(-yawRadians) * distance);
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
        
        refreshGL();
    }
    
    public void setYawPitch(float yaw, float pitch) {
        this.yaw = MathUtil.normalize360(yaw);
        this.pitch = Math.max(-90, Math.min(90, pitch));
        
        refreshGL();
    }
    
    private void refreshGL() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glRotatef(pitch, 1, 0, 0);
        GL11.glRotatef(yaw + 180, 0, 1, 0);
        GL11.glTranslated(-x, -y, -z);
    }
    
    @Override
    public String toString() {
        return String.format("Camera{pos: [%.2f, %.2f, %.2f], yaw: %.2f, pitch: %.2f}", x, y, z, yaw, pitch);
    }
    
}
