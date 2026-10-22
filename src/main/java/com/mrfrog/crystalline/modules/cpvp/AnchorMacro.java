package com.mrfrog.crystalline.modules.cpvp;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.util.InvUtil;
import com.mrfrog.crystalline.util.KeyUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AnchorMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDelays = settings.createGroup("Delays");

    private final Setting<Keybind> key = sgGeneral.add(new KeybindSetting.Builder()
        .name("Key")
        .description("Macro bind")
        .defaultValue(Keybind.fromButton(InputConstants.MOUSE_BUTTON_4))
        .build()
    );

    private final Setting<Integer> anchorDelay = sgDelays.add(new IntSetting.Builder()
        .name("Anchor delay")
        .description("Delay in ticks before placing anchors")
        .defaultValue(1)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> chargeDelay = sgDelays.add(new IntSetting.Builder()
        .name("Charge delay")
        .description("Delay in ticks before charging anchors")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> explodeDelay = sgDelays.add(new IntSetting.Builder()
        .name("Explode delay")
        .description("Delay in ticks before exploding anchors")
        .defaultValue(0)
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

    private final Setting<Boolean> oneAnchor = sgGeneral.add(new BoolSetting.Builder()
        .name("One Anchor")
        .description("Limits to one anchor per key press")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> totemSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("Totem Switch")
        .description("Switches to totem after each anchor")
        .defaultValue(true)
        .build()
    );

    public AnchorMacro() {
        super(Crystalline.CPVP, "Anchor Macro", "Automatically anchors");
    }

    private boolean pressedLastTick = false;
    private boolean pressedLastAnchor = false;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(useDown) use(false);

        if(!key.get().isPressed() || !pressedLastTick) placed = false;
        pressedLastTick = key.get().isPressed();

        if(key.get().isPressed() && pressedLastAnchor && !holdKey.get()) return;
        pressedLastAnchor = false;

        if(!key.get().isPressed() || mc.player == null || mc.player.gameMode() == GameType.SPECTATOR ||
            mc.gui.screen() != null || !(mc.hitResult instanceof BlockHitResult hitResult)) return;

        if(!placeActive && !chargeActive && !explodeActive) {
            BlockState blockState = mc.level.getBlockState(hitResult.getBlockPos());
            if(blockState.is(Blocks.RESPAWN_ANCHOR)) {
                int charge = blockState.getValue(RespawnAnchorBlock.CHARGE);
                if(charge == 0) chargeActive = true;
                else explodeActive = true;
            }else placeActive = true;
        }

        place();
        charge();
        explode();
    }


    private boolean placeActive = false;
    private boolean placing = false;
    private int placeCounter = 0;
    private boolean placed = false;

    private void place() {
        if(!placeActive) return;
        if(!placing && (placed || !InvUtil.filterHotbar(Items.RESPAWN_ANCHOR, true))) {
            placeActive = false;
            return;
        }
        placing = true;

        if(placeCounter < anchorDelay.get()) {
            placeCounter++;
            return;
        }

        use(true);

        placing = false;
        placeActive = false;
        placeCounter = 0;
        if(oneAnchor.get()) placed = true;
    }


    private boolean chargeActive = false;
    private boolean charging = false;
    private int chargeCounter = 0;

    private void charge() {
        if(!chargeActive) return;
        if(!charging && !InvUtil.filterHotbar(Items.GLOWSTONE, true)) {
            chargeActive = false;
            return;
        }
        charging = true;

        if(chargeCounter < chargeDelay.get()) {
            chargeCounter++;
            return;
        }

        use(true);

        charging = false;
        chargeActive = false;
        chargeCounter = 0;
    }


    private boolean explodeActive = false;
    private boolean exploding = false;
    private int explodeCounter = 0;

    private void explode() {
        if(!explodeActive) return;
        if(!exploding && !(totemSwitch.get() && InvUtil.filterHotbar(Items.TOTEM_OF_UNDYING, true) || InvUtil.filterHotbar(Items.GLOWSTONE, false))) {
            explodeActive = false;
            return;
        }
        exploding = true;

        if(explodeCounter < explodeDelay.get()) {
            explodeCounter++;
            return;
        }

        use(true);

        exploding = false;
        explodeActive = false;
        explodeCounter = 0;
        pressedLastAnchor = true;
    }

    private boolean useDown = false;
    private void use(boolean down) {KeyUtil.simulatePress(mc.options.keyUse, down); useDown = down;}
}
