package com.mrfrog.crystalline.util;

import com.mrfrog.crystalline.mixin.accessors.IKeyMapping;
import net.minecraft.client.KeyMapping;

public class KeyUtil {
    public static void simulatePress(KeyMapping bind, boolean pressed) {
        IKeyMapping accessor = (IKeyMapping) bind;

        if(pressed) {
            bind.setDown(true);
            accessor.setClickCount(accessor.getClickCount()+1);
        }else {
            accessor.setClickCount(0);
            bind.setDown(false);
        }
    }
}
