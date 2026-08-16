package com.mrfrog.crystalline.modules;

import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.mixin.accessors.IKeyMapping;
import com.mrfrog.crystalline.util.KeyUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Set;

public class CrystalMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup  sgAutoHit = settings.createGroup("Auto Hit");

    private final Setting<Keybind> key = sgGeneral.add(new KeybindSetting.Builder()
        .name("Key")
        .description("Macro bind")
        .defaultValue(Keybind.fromButton(GLFW.GLFW_MOUSE_BUTTON_5))
        .build()
    );

    private final Setting<Boolean> placeObbi = sgGeneral.add(new BoolSetting.Builder()
        .name("Place Obsidian")
        .description("Place obsidian when looking at other blocks")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> clickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Click delay")
        .description("Delay in ticks between clicks")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> autoHit = sgAutoHit.add(new BoolSetting.Builder()
        .name("Auto Hit")
        .description("Automatically hits entities with your sword")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> autoHitDelay = sgAutoHit.add(new IntSetting.Builder()
        .name("Auto Hit Delay")
        .description("Delay in ticks before hitting")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> autoHitBetweenDelay = sgAutoHit.add(new IntSetting.Builder()
        .name("Between Delay")
        .description("Delay in ticks between subsequent hits")
        .defaultValue(5)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Set<EntityType<?>>> autoHitEntities = sgAutoHit.add(new EntityTypeListSetting.Builder()
        .name("Entities")
        .description("Entities to hit")
        .defaultValue(Set.of(EntityType.PLAYER))
        .build()
    );

    public CrystalMacro() {
        super(Crystalline.PVP, "Crystal Macro", "Automatically crystals");
    }

    private boolean active = false;
    private int delayCounter = 0;
    private boolean hit = false;
    private boolean simHit = false;
    private boolean simUse = false;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(simHit) {
            KeyUtil.simulatePress((IKeyMapping)mc.options.keyAttack,false);
            simHit = false;
        } if(simUse) {
            KeyUtil.simulatePress((IKeyMapping)mc.options.keyUse,false);
            simUse = false;
        }

        if(!key.get().isPressed() || mc.player == null || mc.player.gameMode() == GameType.SPECTATOR || mc.screen != null || mc.hitResult == null) return;

        if(autoHit.get()) handleAutoHit();

        if(!active) {
            if(mc.hitResult instanceof BlockHitResult blockHit) {
                if(mc.level.getBlockState(blockHit.getBlockPos()).is(Blocks.OBSIDIAN) ||
                    mc.level.getBlockState(blockHit.getBlockPos()).is(Blocks.BEDROCK)) filterHotbar(Items.END_CRYSTAL, true);
                else if(placeObbi.get()) filterHotbar(Items.OBSIDIAN, true);
            }else if(mc.hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof EndCrystal) hit = true;
        }

        if(delayCounter < clickDelay.get()) {
            delayCounter++;
            return;
        }

        if(hit) {
            KeyUtil.simulatePress((IKeyMapping)mc.options.keyAttack,true);
            simHit = true;
        }
        else {
            KeyUtil.simulatePress((IKeyMapping)mc.options.keyUse,true);
            simUse = true;
        }

        reset();
    }

    private void filterHotbar(Item query, boolean whitelist) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() != query && whitelist) continue;
            mc.player.getInventory().setSelectedSlot(i);
        }
        if (mc.player.getInventory().getSelectedItem().getItem() == query && whitelist) active = true;
    }

    private final List<Item> swords = List.of(Items.STONE_SWORD, Items.COPPER_SWORD, Items.DIAMOND_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD,
        Items.NETHERITE_SWORD, Items.WOODEN_SWORD);
    private boolean autoHitActive = false;
    private int autoHitDelayCounter = 0;
    private int autoHitBetweenDelayCounter = autoHitBetweenDelay.get();

    private void handleAutoHit() {
        if(autoHitBetweenDelayCounter != 0) {
            autoHitBetweenDelayCounter--;
            return;
        }

        if(!(mc.hitResult instanceof EntityHitResult hitResult) || !autoHitEntities.get().contains(hitResult.getEntity().getType())) return;

        if(!autoHitActive) {
            for (int i = 0; i < 9; i++) {
                if (!swords.contains(mc.player.getInventory().getItem(i).getItem())) continue;
                mc.player.getInventory().setSelectedSlot(i);
            }
            if (swords.contains(mc.player.getInventory().getSelectedItem().getItem())) autoHitActive = true;
        }

        if(autoHitDelayCounter < autoHitDelay.get()) {
            autoHitDelayCounter++;
            return;
        }

        if(autoHitActive) {
            KeyUtil.simulatePress((IKeyMapping)mc.options.keyAttack,true);
            simHit = true;
        }

        autoHitActive = false;
        autoHitDelayCounter = 0;
        autoHitBetweenDelayCounter = autoHitBetweenDelay.get();
    }

    private void reset() {
        active = false;
        hit = false;
        delayCounter = 0;
    }
}
