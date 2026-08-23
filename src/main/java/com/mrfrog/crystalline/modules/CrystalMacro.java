package com.mrfrog.crystalline.modules;

import com.mrfrog.crystalline.Crystalline;
import com.mrfrog.crystalline.mixin.accessors.IKeyMapping;
import com.mrfrog.crystalline.util.InvUtil;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Set;

public class CrystalMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDelays = settings.createGroup("Delays");
    private final SettingGroup sgAutoHit = settings.createGroup("Auto Hit");

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

    private final Setting<Integer> placeDelay = sgDelays.add(new IntSetting.Builder()
        .name("Place Delay")
        .description("Delay in ticks between placing crystals")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> explodeDelay = sgDelays.add(new IntSetting.Builder()
        .name("Explode Delay")
        .description("Delay in ticks between exploding crystals")
        .defaultValue(1)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> placeObbiDelay = sgDelays.add(new IntSetting.Builder()
        .name("Obsidian Place Delay")
        .description("Delay in ticks before placing obsidian")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> placeObbiBetweenDelay = sgDelays.add(new IntSetting.Builder()
        .name("Obsidian Place Between Delay")
        .description("Delay in ticks between placing obsidian")
        .defaultValue(1)
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

    private final Setting<Integer> autoHitDelay = sgDelays.add(new IntSetting.Builder()
        .name("Auto Hit Delay")
        .description("Delay in ticks before hitting")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Integer> autoHitBetweenDelay = sgDelays.add(new IntSetting.Builder()
        .name("Auto Hit Between Delay")
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

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(useDown) use(false);
        if(hitDown) hit(false);

        if(autoHitBetweenCounter != 0 && autoHit.get()) autoHitBetweenCounter--;
        if(placeObbiBetweenCounter != 0 && placeObbi.get()) placeObbiBetweenCounter--;

        if(!key.get().isPressed() || mc.player == null || mc.player.gameMode() == GameType.SPECTATOR || mc.screen != null || mc.hitResult == null) return;

        if(!placeActive && !explodeActive && !placeObbiActive && !autoHitActive) {
            if(mc.hitResult instanceof BlockHitResult blockHit) {
                BlockState blockState = mc.level.getBlockState(blockHit.getBlockPos());
                if(blockState.is(Blocks.OBSIDIAN) || blockState.is(Blocks.BEDROCK)) placeActive = true;
                else if(placeObbi.get()) placeObbiActive = true;
            }else if(mc.hitResult instanceof EntityHitResult entityHit) {
                if(entityHit.getEntity() instanceof EndCrystal) explodeActive = true;
                else if(autoHitEntities.get().contains(entityHit.getEntity().getType()) && autoHit.get()) autoHitActive = true;
            }
        }

        place();
        explode();
        placeObbiF();
        autoHitF();
    }


    private boolean placeActive = false;
    private boolean placing = false;
    private int placeCounter = 0;

    private void place() {
        if(!placeActive) return;
        if(!placing && !InvUtil.filterHotbar(Items.END_CRYSTAL, true)) {
            placeActive = false;
            return;
        }
        placing = true;

        if(placeCounter < placeDelay.get()) {
            placeCounter++;
            return;
        }

        use(true);

        placing = false;
        placeActive = false;
        placeCounter = 0;
    }


    private boolean explodeActive = false;
    private int explodeCounter = 0;

    private void explode() {
        if(!explodeActive) return;

        if(explodeCounter < explodeDelay.get()) {
            explodeCounter++;
            return;
        }

        hit(true);

        explodeActive = false;
        explodeCounter = 0;
    }


    private boolean placeObbiActive = false;
    private boolean placingObbi = false;
    private int placeObbiCounter = 0;
    private int placeObbiBetweenCounter = 0;

    private void placeObbiF() {
        if(!placeObbiActive) return;
        if((!placingObbi && !InvUtil.filterHotbar(Items.OBSIDIAN, true)) || placeObbiBetweenCounter != 0) {
            placeObbiActive = false;
            return;
        }
        placingObbi = true;

        if(placeObbiCounter < placeObbiDelay.get()) {
            placeObbiCounter++;
            return;
        }

        use(true);

        placingObbi = false;
        placeObbiActive = false;
        placeObbiCounter = 0;
        placeObbiBetweenCounter = placeObbiBetweenDelay.get() + 1;
    }


    private boolean autoHitActive = false;
    private boolean autoHitting = false;
    private int autoHitCounter = 0;
    private int autoHitBetweenCounter = 0;
    private final List<Item> swords = List.of(Items.STONE_SWORD, Items.COPPER_SWORD, Items.DIAMOND_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD,
        Items.NETHERITE_SWORD, Items.WOODEN_SWORD);

    private void autoHitF() {
        if(!autoHitActive) return;
        if((!autoHitting && !InvUtil.filterHotbar(swords, true)) || autoHitBetweenCounter != 0) {
            autoHitActive = false;
            return;
        }
        autoHitting = true;

        if(autoHitCounter < autoHitDelay.get()) {
            autoHitCounter++;
            return;
        }

        hit(true);

        autoHitting = false;
        autoHitActive = false;
        autoHitCounter = 0;
        autoHitBetweenCounter = autoHitBetweenDelay.get() + 1;
    }

    private boolean useDown = false;
    private void use(boolean down) {KeyUtil.simulatePress((IKeyMapping)mc.options.keyUse, down); useDown = down;}

    private boolean hitDown = false;
    private void hit(boolean down) {KeyUtil.simulatePress((IKeyMapping)mc.options.keyAttack, down); hitDown = down;}
}
