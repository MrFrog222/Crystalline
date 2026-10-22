package com.mrfrog.crystalline;

import com.mrfrog.crystalline.commands.PunchingBagCommand;
import com.mrfrog.crystalline.modules.cpvp.*;
import com.mrfrog.crystalline.modules.pvp.SprintReset;
import com.mrfrog.crystalline.modules.pvp.TriggerBot;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class Crystalline extends MeteorAddon {
    public static final Category CPVP = new Category("Crystalline CPvP");
    public static final Category PVP = new Category("Crystalline PvP");

    @Override
    public void onInitialize() {
        Modules.get().add(new PearlHotkey());
        Modules.get().add(new AnchorMacro());
        Modules.get().add(new CrystalMacro());
        Modules.get().add(new LegitAutoTotem());
        Modules.get().add(new BlockMacro());
        Modules.get().add(new TriggerBot());
        Modules.get().add(new SprintReset());

        Commands.add(new PunchingBagCommand());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CPVP);
        Modules.registerCategory(PVP);
    }

    @Override
    public String getPackage() {
        return "com.mrfrog.crystalline";
    }

    @Override
    public GithubRepo getRepo() {return new GithubRepo("MrFrog222", "Crystalline");}
}
