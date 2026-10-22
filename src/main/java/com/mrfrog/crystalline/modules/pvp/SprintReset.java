package com.mrfrog.crystalline.modules.pvp;

import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.util.KeyUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.phys.EntityHitResult;

public class SprintReset extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SprintResetModes> mode = sgGeneral.add(new EnumSetting.Builder<SprintResetModes>()
        .name("Mode")
        .description("The method of sprint resetting")
        .defaultValue(SprintResetModes.STap)
        .build()
    );

    public SprintReset() {
        super(Crystalline.PVP, "Sprint Reset", "Makes combos easier in pvp", "W-Tap");
    }

    private boolean prevDown = false;

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        if(SDown) STap(false);
        if(!WDown) WTap(true);
        if(mc.player == null || !mc.options.keyAttack.isDown() || prevDown || !mc.options.keyUp.isDown() || !(mc.hitResult instanceof EntityHitResult) || !mc.player.isSprinting()) {
            prevDown = mc.options.keyAttack.isDown();
            return;
        }
        prevDown = true;

        if(mode.get() == SprintResetModes.STap) STap(true);
        else WTap(false);
    }

    private boolean SDown = false;
    private void STap(boolean down) {KeyUtil.simulatePress(mc.options.keyDown, down); SDown = down;}

    private boolean WDown = true;
    private void WTap(boolean down) {KeyUtil.simulatePress(mc.options.keyUp, down); WDown = down;}
}
