package com.herschclient.features.module;

import com.herschclient.core.module.Module;
import com.herschclient.core.module.ModuleCategory;
import com.herschclient.core.settings.BoolSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public final class XrayModule extends Module {

    private final Set<Block> whitelist = new HashSet<>();
    private final BoolSetting caveMode = new BoolSetting("cave_mode", "Cave Mode", false);

    public XrayModule() {
        super("Xray", ModuleCategory.VISUAL);
        settings.add(caveMode);
        // initWhitelist(); // access to Blocks too early causes crash if triggered by Mixin
    }

    public void init() {
        initWhitelist();
    }

    private void initWhitelist() {
        add(Blocks.COAL_ORE);
        add(Blocks.DEEPSLATE_COAL_ORE);
        add(Blocks.IRON_ORE);
        add(Blocks.DEEPSLATE_IRON_ORE);
        add(Blocks.GOLD_ORE);
        add(Blocks.DEEPSLATE_GOLD_ORE);
        add(Blocks.REDSTONE_ORE);
        add(Blocks.DEEPSLATE_REDSTONE_ORE);
        add(Blocks.LAPIS_ORE);
        add(Blocks.DEEPSLATE_LAPIS_ORE);
        add(Blocks.DIAMOND_ORE);
        add(Blocks.DEEPSLATE_DIAMOND_ORE);
        add(Blocks.EMERALD_ORE);
        add(Blocks.DEEPSLATE_EMERALD_ORE);
        add(Blocks.COPPER_ORE);
        add(Blocks.DEEPSLATE_COPPER_ORE);
        add(Blocks.NETHER_QUARTZ_ORE);
        add(Blocks.NETHER_GOLD_ORE);
        add(Blocks.ANCIENT_DEBRIS);
        add(Blocks.OBSIDIAN);
        add(Blocks.CHEST);
        add(Blocks.TRAPPED_CHEST);
        add(Blocks.ENDER_CHEST);
        add(Blocks.WATER); 
        add(Blocks.LAVA);
    }

    private void add(Block block) {
        whitelist.add(block);
    }

    @Override
    protected void onEnable() {
        reloadChunks();
    }

    @Override
    protected void onDisable() {
        reloadChunks();
    }

    private void reloadChunks() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    public boolean isWhitelisted(Block block) {
        return whitelist.contains(block);
    }
    
    public boolean isCaveMode() {
        return caveMode.get();
    }
}
