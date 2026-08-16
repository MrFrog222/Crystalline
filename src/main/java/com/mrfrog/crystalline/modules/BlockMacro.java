package com.mrfrog.crystalline.modules;

import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.mixin.accessors.IKeyMapping;
import com.mrfrog.crystalline.util.KeyUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BlockMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> key = sgGeneral.add(new KeybindSetting.Builder()
        .name("Key")
        .description("Macro bind")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_R))
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in ticks between block placement")
        .defaultValue(1)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("Blocks")
        .description("Acceptable blocks for  blocking up")
        .defaultValue(List.of(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN))
        .build()
    );

    public BlockMacro() {
        super(Crystalline.PVP, "Block Macro", "Automatically places blocks");
    }

    private boolean active = false;
    private int delayCounter = 0;
    private boolean useSim = false;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(useSim) {
            KeyUtil.simulatePress((IKeyMapping)mc.options.keyUse, false);
            useSim = false;
        }

        if(!key.get().isPressed() || mc.player == null || mc.screen != null || mc.player.gameMode() == GameType.SPECTATOR) return;

        if(!active) {
            for(int i = 0; i < 9; i++) {
                Item item = mc.player.getInventory().getItem(i).getItem();
                if(!(item instanceof BlockItem blockItem) || !blocks.get().contains(blockItem.getBlock())) continue;
                mc.player.getInventory().setSelectedSlot(i);
                break;
            }
            if(!(mc.player.getInventory().getSelectedItem().getItem() instanceof BlockItem blockItem) || !blocks.get().contains(blockItem.getBlock())) active = true;
        }

        if(delayCounter < delay.get()) {
            delayCounter++;
            return;
        }

        KeyUtil.simulatePress((IKeyMapping)mc.options.keyUse, true);
        useSim = true;

        active = false;
        delayCounter = 0;
    }
}
