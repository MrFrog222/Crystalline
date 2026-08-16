package com.mrfrog.crystalline.modules;

import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.mixin.accessors.IKeyMapping;
import com.mrfrog.crystalline.util.KeyUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import org.lwjgl.glfw.GLFW;

public class PearlHotkey extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> key = sgGeneral.add(new KeybindSetting.Builder()
        .name("Key")
        .description("Hotkey to throw pearl")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_Q))
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in ticks before throwing")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> switchBack = sgGeneral.add(new BoolSetting.Builder()
        .name("Switch Back")
        .description("Switch back to current slot")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> switchBackDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Switch Back Delay")
        .description("Delay in ticks before switching back")
        .defaultValue(1)
        .min(1)
        .sliderMax(20)
        .build()
    );

    public PearlHotkey() {
        super(Crystalline.PVP, "Pearl Hotkey", "Hotkey to switch to pearl and throw it");
    }

    private boolean pressedLastTick = false;
    private boolean active = false;
    private int lastSlot = -1;
    private int delayCounter = 0;
    private int switchDelayCounter = 0;
    private boolean pressSim = false;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(pressSim) {
            KeyUtil.simulatePress((IKeyMapping) mc.options.keyUse, false);
            pressSim = false;
        }
        if(mc.player == null || mc.player.gameMode() == GameType.SPECTATOR || mc.screen != null) return;
        boolean keyPressed = key.get().isPressed();

        if(keyPressed && !pressedLastTick) {
            lastSlot = mc.player.getInventory().getSelectedSlot();
            for(int i = 0; i < 9; i++) {
                if(mc.player.getInventory().getItem(i).getItem() != Items.ENDER_PEARL) continue;
                mc.player.getInventory().setSelectedSlot(i);
                active = true;
                break;
            }
        }

        pressedLastTick = keyPressed;

        if(!active) return;

        if(delayCounter < delay.get()) {
            delayCounter++;
            return;
        }
        KeyUtil.simulatePress((IKeyMapping) mc.options.keyUse, true);
        pressSim = true;

        if(switchBack.get()) {
            if(switchDelayCounter < switchBackDelay.get()) {
                switchDelayCounter++;
                return;
            }

            mc.player.getInventory().setSelectedSlot(lastSlot);
        }

        reset();
    }

    private void reset() {
        active = false;
        delayCounter = 0;
        switchDelayCounter = 0;
        lastSlot = -1;
    }
}
