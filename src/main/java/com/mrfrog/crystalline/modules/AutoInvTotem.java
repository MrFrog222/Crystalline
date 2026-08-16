package com.mrfrog.crystalline.modules;

import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.mixin.accessors.IKeyMapping;
import com.mrfrog.crystalline.util.KeyUtil;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

public class AutoInvTotem extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAutoInv = settings.createGroup("Auto Inv");

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in ticks before equipping totem")
        .defaultValue(1)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> autoInv = sgAutoInv.add(new BoolSetting.Builder()
        .name("Auto Inv")
        .description("Automatically handles open/close inventory")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> openDelay = sgAutoInv.add(new IntSetting.Builder()
        .name("Open delay")
        .description("Delay in ticks before opening inv")
        .defaultValue(3)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> closeDelay = sgAutoInv.add(new IntSetting.Builder()
        .name("Close delay")
        .description("Delay in ticks before closing inv")
        .defaultValue(5)
        .min(0)
        .sliderMax(20)
        .build()
    );

    public AutoInvTotem() {
        super(Crystalline.PVP, "Auto Inv Totem", "Automatically equips totem in inventory");
    }

    private boolean active = false;
    private int delayCounter = 0;

    private boolean openActive = false;
    private boolean closeActive = false;
    private int openDelayCounter = 0;
    private int closeDelayCounter = 0;
    private boolean invKeySim = false;

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if(!(event.screen instanceof InventoryScreen) || mc.player.gameMode() == GameType.CREATIVE ||
            mc.player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING) return;
        active = true;
    }

    @EventHandler
    private void onPacketRecieve(PacketEvent.Receive event) {
        if(!(event.packet instanceof ClientboundEntityEventPacket packet) || packet.getEventId() != 35 ||
            packet.getEntity(mc.level) != mc.player || !autoInv.get() || mc.screen != null) return;
        openActive = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        autoInv();

        if(!active) return;
        if (delayCounter < delay.get()) {
            delayCounter++;
            return;
        }
        if (!(mc.screen instanceof InventoryScreen)) {
            active = false;
            delayCounter = 0;
            return;
        }

        int slot = findTotem();
        if (slot != -1) mc.gameMode.handleContainerInput(0, slot, 40, ContainerInput.SWAP, mc.player);

        active = false;
        delayCounter = 0;
    }

    private int findTotem() {
        for(int i = 0; i < 36; i++) {
            if(mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    private void autoInv() {
        if(invKeySim) {
            KeyUtil.simulatePress((IKeyMapping)mc.options.keyInventory, false);
            invKeySim = false;
        }

        autoInvOpen();
        autoInvClose();
    }

    private void autoInvOpen() {
        if(!openActive) return;
        if(openDelayCounter < openDelay.get()) {
            openDelayCounter++;
            return;
        }

        KeyUtil.simulatePress((IKeyMapping)mc.options.keyInventory, true);
        invKeySim = true;

        openDelayCounter = 0;
        openActive = false;
        closeActive = true;
    }

    private void autoInvClose() {
        if(!closeActive) return;
        if(closeDelayCounter < closeDelay.get() + delay.get()) {
            closeDelayCounter++;
            return;
        }

        KeyUtil.simulatePress((IKeyMapping)mc.options.keyInventory, true);
        invKeySim = true;

        closeDelayCounter = 0;
        closeActive = false;
    }
}
