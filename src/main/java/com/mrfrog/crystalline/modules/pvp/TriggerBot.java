package com.mrfrog.crystalline.modules.pvp;

import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.util.KeyUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Set;

public class TriggerBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("Entities")
        .description("Entities to hit")
        .defaultValue(Set.of(EntityTypes.PLAYER))
        .build()
    );

    public TriggerBot() {
        super(Crystalline.PVP, "Trigger Bot", "Automatically hits for you");
    }

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        if(hitDown) hit(false);
        if(mc.player == null || !(mc.hitResult instanceof EntityHitResult res) || mc.gui.screen() != null) return;
        if(!entities.get().contains(res.getEntity().getType())) return;
        if(mc.player.getAttackStrengthScale(0.0f) < 1.0f) return;

        hit(true);
    }

    private boolean hitDown = false;
    private void hit(boolean down) {KeyUtil.simulatePress(mc.options.keyAttack, down); hitDown = down;}
}
