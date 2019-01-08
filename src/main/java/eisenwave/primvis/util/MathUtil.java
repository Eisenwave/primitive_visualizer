package eisenwave.primvis.util;

public final class MathUtil {
    
    public final static double EPSILON = 1E-10;
    
    public static boolean eq(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }
    
    public static float normalize180(float yaw) {
        yaw %= 360;
        if (yaw < -180) return yaw + 360;
        if (yaw >= 180) return yaw - 360;
        return yaw;
    }
    
    public static float normalize360(float yaw) {
        yaw %= 360;
        return yaw < 0 ? yaw + 360 : yaw;
    }
    
    public static double[] yawToXZ(double yaw) {
        return new double[] {Math.sin(-yaw), Math.cos(yaw)};
    }
    
    public static double[] rypToXYZ(double radius, double yaw, double pitch) {
        if (eq(Math.abs(pitch), Math.PI / 2)) {
            return new double[] {0, pitch >= 0? -radius : radius, 0};
        } else {
            final double
                len = Math.cos(pitch) * radius,
                x = Math.sin(-yaw) * len,
                z = Math.cos( yaw) * len,
                y = -Math.tan(pitch) * len;
            return new double[] {x, y, z};
        }
    }
    
    public static double clamp(double min, double value, double max) {
        return value < min? min : value > max? max : value;
    }
    
    public static float clamp(float min, float value, float max) {
        return value < min? min : value > max? max : value;
    }
    
}
