package com.mrfrog.crystalline.util;

import net.minecraft.world.item.Item;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class InvUtil {
    public static boolean filterHotbar(Item query, boolean whitelist) {
        for (int i = 0; i < 9; i++) {
            if ((mc.player.getInventory().getItem(i).getItem() == query) ^ whitelist) continue;
            mc.player.getInventory().setSelectedSlot(i);
            break;
        }
        return (mc.player.getInventory().getSelectedItem().getItem() != query) ^ whitelist;
    }

    public static boolean filterHotbar(List<Item> query, boolean whitelist) {
        for (int i = 0; i < 9; i++) {
            if (query.contains(mc.player.getInventory().getItem(i).getItem()) ^ whitelist) continue;
            mc.player.getInventory().setSelectedSlot(i);
            break;
        }
        return !query.contains(mc.player.getInventory().getSelectedItem().getItem()) ^ whitelist;
    }
}
