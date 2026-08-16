package com.mrfrog.crystalline.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class PunchingBagCommand extends Command {
    public PunchingBagCommand() {
        super("punchingbag", "Gives spawn egg for test subject");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(_ -> {
            mc.player.connection.sendChat(".give zombie_spawn_egg[entity_data={id:\"minecraft:zombie\", NoAI:0b,equipment:{feet:{id:\"minecraft:netherite_boots\",count:1,components:{\"minecraft:enchantments\":{\"protection\":4},\"minecraft:unbreakable\":{}}},legs:{id:\"minecraft:netherite_leggings\",count:1,components:{\"minecraft:enchantments\":{\"protection\":4},\"minecraft:unbreakable\":{}}},chest:{id:\"minecraft:netherite_chestplate\",count:1,components:{\"minecraft:enchantments\":{\"protection\":4},\"minecraft:unbreakable\":{}}},head:{id:\"minecraft:netherite_helmet\",count:1,components:{\"minecraft:enchantments\":{\"protection\":4},\"minecraft:unbreakable\":{}}},offhand:{id:\"minecraft:totem_of_undying\",count:64}}}]");

            return SINGLE_SUCCESS;
        });
    }
}
