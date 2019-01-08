package eisenwave.primvis.gl;

import static org.lwjgl.opengl.GL11.*;

public class GLU {
    
    public static void gluPerspective(float fov, float aspect, float near, float far) {
        float bottom = -near * (float) Math.tan(fov / 2);
        float top = -bottom;
        float left = aspect * bottom;
        float right = -left;
        glFrustum(left, right, bottom, top, near, far);
    }
    
}
