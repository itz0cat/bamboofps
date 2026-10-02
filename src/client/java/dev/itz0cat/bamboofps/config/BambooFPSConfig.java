package dev.itz0cat.bamboofps.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BambooFPSConfig {
    public boolean enabled = true;
    public boolean hideBlocks = true;
    public boolean hideBlockEntities = true;
    public boolean hideItems = true;
    public boolean cancelParticles = true;

    public Set<String> hiddenBlocks = new LinkedHashSet<>(List.of(
            "minecraft:hopper",
            "minecraft:dispenser",
            "minecraft:piston",
            "minecraft:sticky_piston",
            "minecraft:crafter",
            "minecraft:bamboo"
    ));

    public Set<String> hiddenItems = new LinkedHashSet<>(List.of(
            "minecraft:bamboo",
            "minecraft:bone",
            "minecraft:bone_meal",
            "minecraft:scaffolding",
            "minecraft:bamboo_block"
    ));

    public BambooFPSConfig() {
    }
}
