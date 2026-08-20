package com.mrfrog.crystalline.modules;

import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.mixin.accessors.IKeyMapping;
import com.mrfrog.crystalline.util.KeyUtil;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

public class LegitAutoTotem extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgHotbar = settings.createGroup("Hotbar");
    private final SettingGroup sgAutoInv = settings.createGroup("Auto Inv");

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in ticks before equipping totem (beware that if this takes longer than the inv is open, it will fail)")
        .defaultValue(1)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> hotbar = sgHotbar.add(new BoolSetting.Builder()
        .name("Hotbar")
        .description("Replenishes hotbar totems as well")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> hotbarDelay = sgHotbar.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in ticks before replenishing totem (will be offset if offhand totems are replenished at the same time) (beware that if this takes longer than the inv is open, it will fail)")
        .defaultValue(1)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> hotbarBetweenDelay = sgHotbar.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in ticks before replenishing totem (beware that if this takes longer than the inv is open, it will fail)")
        .defaultValue(1)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<List<String>> hotbarSlots = sgHotbar.add(new StringListSetting.Builder()
        .name("Hotbar Slots")
        .description("Slots to refill with totems")
        .defaultValue(List.of())
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
        .defaultValue(3)
        .min(0)
        .sliderMax(20)
        .build()
    );

    public LegitAutoTotem() {
        super(Crystalline.PVP, "Legit Auto Totem", "Automatically equips totems");
    }

    //auto inv totem
    private boolean active = false;
    private int delayCounter = 0;
    private boolean invOpened = false;

    //hotbar replenish
    private boolean hotbarActive = false;
    private int hotbarDelayCounter = 0;
    private int hotbarIter = 0;
    private ArrayList<Integer> hotbarSlotCache = new ArrayList<>();

    //auto inv
    private boolean openActive = false;
    private boolean closeActive = false;
    private int openDelayCounter = 0;
    private int closeDelayCounter = 0;
    private boolean invKeySim = false;

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if(!(event.screen instanceof InventoryScreen) || mc.player.gameMode() == GameType.CREATIVE) return;
        if(mc.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) active = true;

        ArrayList<Integer> hotbarTotems = checkHotbarTotems();
        if(!hotbarTotems.isEmpty()) {
            hotbarActive = true;
            hotbarIter = hotbarTotems.size();
            hotbarSlotCache = hotbarTotems;
        }
    }

    @EventHandler
    private void onPacketRecieve(PacketEvent.Receive event) {
        if(!(event.packet instanceof ClientboundEntityEventPacket packet) || packet.getEventId() != 35 || packet.getEntity(mc.level) != mc.player || !autoInv.get() ||
            mc.screen != null) return;
        if(mc.player.getInventory().getSelectedItem().getItem() == Items.TOTEM_OF_UNDYING && !hotbar.get()) return;
        openActive = true;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        autoInv();

        if(active) {
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
            if(!hotbarActive && invOpened) closeActive = true;
        } else handleHotbar();
    }

    private int findTotem() {
        for(int i = 9; i < 36; i++) {
            if(mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) return i;
        }
        for(int i = 0; i < 9; i++) {
            if(mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) return i + 36;
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
        invOpened = true;
    }

    private void autoInvClose() {
        if(!closeActive) return;
        if(closeDelayCounter < closeDelay.get()) {
            closeDelayCounter++;
            return;
        }

        KeyUtil.simulatePress((IKeyMapping)mc.options.keyInventory, true);
        invKeySim = true;

        closeDelayCounter = 0;
        closeActive = false;
        invOpened = false;
    }

    private void handleHotbar() {
        if(!hotbarActive) return;
        if(hotbarDelayCounter < (hotbarIter == hotbarSlotCache.size() ? hotbarDelay.get() : hotbarBetweenDelay.get())) {
            hotbarDelayCounter++;
            return;
        }
        if (!(mc.screen instanceof InventoryScreen)) {
            hotbarActive = false;
            hotbarDelayCounter = 0;
            return;
        }

        int slot = findTotem();
        if (slot != -1 && slot < 36) mc.gameMode.handleContainerInput(0, slot, hotbarSlotCache.get(hotbarIter - 1), ContainerInput.SWAP, mc.player);

        hotbarDelayCounter = 0;
        hotbarIter--;
        if(hotbarIter == 0 || slot >= 36) {
            hotbarActive = false;
            hotbarSlotCache.clear();
            if(!active && invOpened) closeActive = true;
        }
    }

    private ArrayList<Integer> checkHotbarTotems() {
        ArrayList<Integer> list = new ArrayList<>();
        for(String slot : hotbarSlots.get()) {
            int num = Integer.parseInt(slot);
            if(mc.player.getInventory().getItem(num).getItem() != Items.TOTEM_OF_UNDYING) list.add(num);
        }
        return list;
    }
}
