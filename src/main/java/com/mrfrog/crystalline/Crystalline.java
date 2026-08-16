package com.mrfrog.crystalline;

import com.mrfrog.crystalline.commands.PunchingBagCommand;
import com.mrfrog.crystalline.modules.*;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class Crystalline extends MeteorAddon {
    public static final Category PVP = new Category("Crystalline PvP");

    @Override
    public void onInitialize() {
        Modules.get().add(new PearlHotkey());
        Modules.get().add(new AnchorMacro());
        Modules.get().add(new CrystalMacro());
        Modules.get().add(new AutoInvTotem());
        Modules.get().add(new BlockMacro());

        Commands.add(new PunchingBagCommand());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(PVP);
    }

    @Override
    public String getPackage() {
        return "com.mrfrog.crystalline";
    }

    @Override
    public GithubRepo getRepo() {return new GithubRepo("MrFrog222", "Crystalline");}
}
