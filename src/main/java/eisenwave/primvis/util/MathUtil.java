package eisenwave.primvis.util;

public final class MathUtil {
    
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
    
}
