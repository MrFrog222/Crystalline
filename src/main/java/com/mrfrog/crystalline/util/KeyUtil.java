package com.mrfrog.crystalline.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrfrog.crystalline.mixin.accessors.IKeyMapping;
import com.mrfrog.crystalline.mixin.accessors.IKeyboardHandler;
import com.mrfrog.crystalline.mixin.accessors.IMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;

public class KeyUtil {
    public static void simulatePress(IKeyMapping bind, boolean pressed) {
        Minecraft mc = Minecraft.getInstance();
        IKeyboardHandler kbHandler = (IKeyboardHandler) mc.keyboardHandler;
        IMouseHandler mouseHandler = (IMouseHandler) mc.mouseHandler;
        InputConstants.Key key = bind.getKey();
        int action = pressed ? 1 : 0;

        switch(key.getType()) {
            case KEYSYM:
                kbHandler.invokeKeyPress(mc.getWindow().handle(), action, new KeyEvent(key.getValue(), 0, 0));
                break;
            case SCANCODE:
                kbHandler.invokeKeyPress(mc.getWindow().handle(), action, new KeyEvent(GLFW.GLFW_KEY_UNKNOWN, key.getValue(), 0));
                break;
            case MOUSE:
                mouseHandler.invokeOnButton(mc.getWindow().handle(), new MouseButtonInfo(key.getValue(), 0), action);
                break;
            default:
                System.out.println("Invalid key mapping type!");
                break;
        }
    }
}
