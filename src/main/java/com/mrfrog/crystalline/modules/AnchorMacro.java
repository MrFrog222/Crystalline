package com.mrfrog.crystalline.modules;

import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.mixin.accessors.IKeyMapping;
import com.mrfrog.crystalline.util.KeyUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;

public class AnchorMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> key = sgGeneral.add(new KeybindSetting.Builder()
        .name("Key")
        .description("Macro bind")
        .defaultValue(Keybind.fromButton(GLFW.GLFW_MOUSE_BUTTON_4))
        .build()
    );

    private final Setting<Integer> useDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Use delay")
        .description("Delay in ticks before using relevant items")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> betweenDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Between Delay")
        .description("Delay in ticks between anchors")
        .defaultValue(8)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> holdKey = sgGeneral.add(new BoolSetting.Builder()
        .name("Hold Key")
        .description("Allows holding the macro key to do multiple anchors subsequently")
        .defaultValue(false)
        .build()
    );

    public AnchorMacro() {
        super(Crystalline.PVP, "Anchor Macro", "Automatically anchors");
    }

    private int delayCounter = 0;
    private boolean active = false;
    private boolean simPress = false;
    private int betweenDelayCounter = 0;
    private boolean setBetweenDelay = false;
    private boolean pressedLastAnchor = false;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(key.get().isPressed() && pressedLastAnchor && !holdKey.get()) return;
        pressedLastAnchor = false;

        if(simPress) {
            simPress = false;
            KeyUtil.simulatePress((IKeyMapping)mc.options.keyUse, false);
        }

        if(betweenDelayCounter != 0 && holdKey.get()) {
            betweenDelayCounter--;
            return;
        }

        if(!key.get().isPressed() || mc.player == null || mc.player.gameMode() == GameType.SPECTATOR ||
            mc.screen != null || !(mc.hitResult instanceof BlockHitResult)) return;

        BlockHitResult hitResult = (BlockHitResult) mc.hitResult;
        BlockState blockState = mc.level.getBlockState(hitResult.getBlockPos());
        if(!active) {
            if(blockState.is(Blocks.RESPAWN_ANCHOR)) {
                int charge = blockState.getValue(RespawnAnchorBlock.CHARGE);
                if(charge == 0) filterHotbar(Items.GLOWSTONE, true);
                else {
                    filterHotbar(Items.GLOWSTONE, false);
                    setBetweenDelay = true;
                }
            }else filterHotbar(Items.RESPAWN_ANCHOR, true);
        }

        if(delayCounter < useDelay.get()) {
            delayCounter++;
            return;
        }

        KeyUtil.simulatePress((IKeyMapping)mc.options.keyUse, true);
        simPress = true;

        delayCounter = 0;
        active = false;
        if(setBetweenDelay) {
            betweenDelayCounter = betweenDelay.get();
            pressedLastAnchor = true;
        }
        setBetweenDelay = false;
    }

    private void filterHotbar(Item query, boolean whitelist) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() != query && whitelist) continue;
            mc.player.getInventory().setSelectedSlot(i);
            break;
        }
        if (mc.player.getInventory().getSelectedItem().getItem() == query && whitelist) active = true;
    }
}
