package com.mrfrog.crystalline.mixin.accessors;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface IMouseHandler {
    @Invoker("onButton")
    void invokeOnButton(long handle, MouseButtonInfo rawButtonInfo, int action);
}
