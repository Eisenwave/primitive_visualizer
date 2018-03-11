package eisenwave.primvis.util;

import org.lwjgl.input.Keyboard;

public final class KeyboardUtil {
    
    public static boolean isControlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }
    
}
