package dev.itz0cat.bamboofps.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.itz0cat.bamboofps.BambooFPSClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {
    public static final Logger LOGGER = LoggerFactory.getLogger(BambooFPSClient.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("bamboofps.json");

    private static volatile BambooFPSConfig config = new BambooFPSConfig();

    // Live thread-safe cache sets
    private static volatile Set<Identifier> cachedBlockIds = Collections.emptySet();
    private static volatile Set<TagKey<Block>> cachedBlockTags = Collections.emptySet();
    private static volatile Set<Identifier> cachedItemIds = Collections.emptySet();
    private static volatile Set<TagKey<Item>> cachedItemTags = Collections.emptySet();

    public static BambooFPSConfig getConfig() {
        return config;
    }

    public static synchronized void load() {
        if (!Files.exists(CONFIG_PATH)) {
            config = new BambooFPSConfig();
            save();
            updateCache();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            BambooFPSConfig loaded = GSON.fromJson(reader, BambooFPSConfig.class);
            if (loaded != null) {
                config = loaded;
                if (config.hiddenBlocks == null) config.hiddenBlocks = new HashSet<>();
                if (config.hiddenItems == null) config.hiddenItems = new HashSet<>();
            } else {
                LOGGER.warn("[BambooFPS] Config file was empty, falling back to defaults.");
                config = new BambooFPSConfig();
            }
        } catch (Exception e) {
            LOGGER.error("[BambooFPS] Failed to read config file, falling back to defaults: {}", e.getMessage());
            config = new BambooFPSConfig();
        }

        updateCache();
    }

    public static synchronized void save() {
        try {
            if (CONFIG_PATH.getParent() != null) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception e) {
            LOGGER.error("[BambooFPS] Failed to save config file: {}", e.getMessage());
        }
        updateCache();
    }

    public static void updateCache() {
        Set<Identifier> newBlockIds = ConcurrentHashMap.newKeySet();
        Set<TagKey<Block>> newBlockTags = ConcurrentHashMap.newKeySet();
        Set<Identifier> newItemIds = ConcurrentHashMap.newKeySet();
        Set<TagKey<Item>> newItemTags = ConcurrentHashMap.newKeySet();

        if (config.hiddenBlocks != null) {
            for (String entry : config.hiddenBlocks) {
                if (entry == null || entry.isBlank()) continue;
                String trimmed = entry.trim();
                if (trimmed.startsWith("#")) {
                    Identifier tagId = Identifier.tryParse(trimmed.substring(1));
                    if (tagId != null) {
                        newBlockTags.add(TagKey.of(RegistryKeys.BLOCK, tagId));
                    }
                } else {
                    Identifier id = Identifier.tryParse(trimmed);
                    if (id != null) {
                        newBlockIds.add(id);
                    }
                }
            }
        }

        if (config.hiddenItems != null) {
            for (String entry : config.hiddenItems) {
                if (entry == null || entry.isBlank()) continue;
                String trimmed = entry.trim();
                if (trimmed.startsWith("#")) {
                    Identifier tagId = Identifier.tryParse(trimmed.substring(1));
                    if (tagId != null) {
                        newItemTags.add(TagKey.of(RegistryKeys.ITEM, tagId));
                    }
                } else {
                    Identifier id = Identifier.tryParse(trimmed);
                    if (id != null) {
                        newItemIds.add(id);
                    }
                }
            }
        }

        cachedBlockIds = newBlockIds;
        cachedBlockTags = newBlockTags;
        cachedItemIds = newItemIds;
        cachedItemTags = newItemTags;
    }

    public static boolean isModEnabled() {
        return config.enabled;
    }

    public static boolean shouldHideBlock(BlockState state) {
        if (!config.enabled || !config.hideBlocks || state == null) {
            return false;
        }

        Block block = state.getBlock();
        Identifier id = Registries.BLOCK.getId(block);
        if (cachedBlockIds.contains(id)) {
            return true;
        }

        for (TagKey<Block> tag : cachedBlockTags) {
            if (state.isIn(tag)) {
                return true;
            }
        }

        return false;
    }

    public static boolean shouldHideBlock(Block block) {
        if (!config.enabled || block == null) {
            return false;
        }

        Identifier id = Registries.BLOCK.getId(block);
        if (cachedBlockIds.contains(id)) {
            return true;
        }

        BlockState state = block.getDefaultState();
        for (TagKey<Block> tag : cachedBlockTags) {
            if (state.isIn(tag)) {
                return true;
            }
        }

        return false;
    }

    public static boolean shouldHideBlockEntity(BlockEntity blockEntity) {
        if (!config.enabled || !config.hideBlockEntities || blockEntity == null) {
            return false;
        }

        // Check if it's a moving piston
        if (blockEntity instanceof PistonBlockEntity pistonBlockEntity) {
            BlockState pushedState = pistonBlockEntity.getPushedBlock();
            if (pushedState != null && shouldHideBlock(pushedState)) {
                return true;
            }
            // If piston block itself is hidden
            Identifier pistonId = Identifier.of("minecraft", "piston");
            Identifier stickyPistonId = Identifier.of("minecraft", "sticky_piston");
            if (cachedBlockIds.contains(pistonId) || cachedBlockIds.contains(stickyPistonId)) {
                return true;
            }
        }

        BlockState state = blockEntity.getCachedState();
        if (state != null && shouldHideBlock(state)) {
            return true;
        }

        Block block = state != null ? state.getBlock() : null;
        if (block != null && shouldHideBlock(block)) {
            return true;
        }

        Identifier beTypeId = Registries.BLOCK_ENTITY_TYPE.getId(blockEntity.getType());
        return beTypeId != null && cachedBlockIds.contains(beTypeId);
    }

    public static boolean shouldHideItem(Item item) {
        if (!config.enabled || !config.hideItems || item == null) {
            return false;
        }

        Identifier id = Registries.ITEM.getId(item);
        if (cachedItemIds.contains(id)) {
            return true;
        }

        for (TagKey<Item> tag : cachedItemTags) {
            if (item.getDefaultStack().isIn(tag)) {
                return true;
            }
        }

        return false;
    }

    public static boolean shouldCancelParticles(BlockState state) {
        if (!config.enabled || !config.cancelParticles) {
            return false;
        }
        return shouldHideBlock(state);
    }

    public static boolean shouldCancelWorldEvent(int eventId) {
        if (!config.enabled || !config.cancelParticles) {
            return false;
        }

        // 1000: DISPENSER_DISPENSES, 1001: DISPENSER_FAILS, 1002: DISPENSER_SHOOTS
        // 1049: CRAFTER_CRAFTS, 1050: CRAFTER_FAILS
        // 1505: BONE_MEAL_USED
        return eventId == 1000 || eventId == 1001 || eventId == 1002
                || eventId == 1049 || eventId == 1050 || eventId == 1505;
    }
}
