package com.mrfrog.crystalline.modules.pvp;

import com.mrfrog.crystalline.Crystalline;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.Random;

public class JumpReset extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> chance = sgGeneral.add(new IntSetting.Builder()
        .name("Chance")
        .description("Chance to hit")
        .defaultValue(99)
        .min(1)
        .max(100)
        .build()
    );

    public JumpReset() {
        super(Crystalline.PVP, "Jump Reset", "Jumps when hit to minimize kb");
    }

    Random rng = new Random();

    @EventHandler
    private void onTick(TickEvent.Pre e) {
    }
}
