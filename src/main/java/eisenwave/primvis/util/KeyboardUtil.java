package eisenwave.primvis.util;

//import org.lwjgl.input.Keyboard;

import static org.lwjgl.glfw.GLFW.*;

public final class KeyboardUtil {
    
    public static boolean isControlDown(long window) {
        return glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS
            || glfwGetKey(window, GLFW_KEY_RIGHT_CONTROL) == GLFW_PRESS;
    }
    
    public static boolean isAltDown(long window) {
        return glfwGetKey(window, GLFW_KEY_LEFT_ALT) == GLFW_PRESS
            || glfwGetKey(window, GLFW_KEY_RIGHT_ALT) == GLFW_PRESS;
    }
    
    public static boolean isShiftDown(long window) {
        return glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS
            || glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS;
    }
    
}
