package dev.itz0cat.bamboofps.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.itz0cat.bamboofps.BambooFPSClient;
import dev.itz0cat.bamboofps.config.BambooFPSConfig;
import dev.itz0cat.bamboofps.config.ConfigManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BambooFPSCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("bamboofps")
                .then(ClientCommandManager.literal("toggle")
                    .executes(context -> {
                        FabricClientCommandSource source = context.getSource();
                        BambooFPSClient.toggleMod(source.getClient());
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("reload")
                    .executes(context -> {
                        ConfigManager.load();
                        BambooFPSClient.reloadWorldRenderers();
                        context.getSource().sendFeedback(Text.literal("§8[§aBambooFPS§8]§r ")
                                .append(Text.translatable("text.bamboofps.reload")));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("list")
                    .executes(context -> {
                        FabricClientCommandSource source = context.getSource();
                        BambooFPSConfig config = ConfigManager.getConfig();

                        source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§r ")
                                .append(Text.translatable(config.enabled ? "text.bamboofps.status.enabled" : "text.bamboofps.status.disabled")));
                        source.sendFeedback(Text.literal("§7Features: ")
                                .append(String.format("Blocks=%s, BlockEntities=%s, Items=%s, Particles=%s",
                                        config.hideBlocks, config.hideBlockEntities, config.hideItems, config.cancelParticles)));
                        source.sendFeedback(Text.literal("§7Hidden Blocks (" + config.hiddenBlocks.size() + "): §f"
                                + String.join(", ", config.hiddenBlocks)));
                        source.sendFeedback(Text.literal("§7Hidden Items (" + config.hiddenItems.size() + "): §f"
                                + String.join(", ", config.hiddenItems)));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.literal("block")
                        .then(ClientCommandManager.argument("id", StringArgumentType.string())
                            .suggests((context, builder) -> CommandSource.suggestIdentifiers(Registries.BLOCK.getIds(), builder))
                            .executes(context -> {
                                String id = StringArgumentType.getString(context, "id");
                                return addBlock(context.getSource(), id);
                            })
                        )
                    )
                    .then(ClientCommandManager.literal("item")
                        .then(ClientCommandManager.argument("id", StringArgumentType.string())
                            .suggests((context, builder) -> CommandSource.suggestIdentifiers(Registries.ITEM.getIds(), builder))
                            .executes(context -> {
                                String id = StringArgumentType.getString(context, "id");
                                return addItem(context.getSource(), id);
                            })
                        )
                    )
                )
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.literal("block")
                        .then(ClientCommandManager.argument("id", StringArgumentType.string())
                            .suggests((context, builder) -> CommandSource.suggestMatching(ConfigManager.getConfig().hiddenBlocks, builder))
                            .executes(context -> {
                                String id = StringArgumentType.getString(context, "id");
                                return removeBlock(context.getSource(), id);
                            })
                        )
                    )
                    .then(ClientCommandManager.literal("item")
                        .then(ClientCommandManager.argument("id", StringArgumentType.string())
                            .suggests((context, builder) -> CommandSource.suggestMatching(ConfigManager.getConfig().hiddenItems, builder))
                            .executes(context -> {
                                String id = StringArgumentType.getString(context, "id");
                                return removeItem(context.getSource(), id);
                            })
                        )
                    )
                )
            );
        });
    }

    private static int addBlock(FabricClientCommandSource source, String id) {
        if (!isValidIdentifierOrTag(id, true)) {
            source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§c ")
                    .append(Text.translatable("text.bamboofps.invalid_id", id)));
            return 0;
        }

        BambooFPSConfig config = ConfigManager.getConfig();
        if (config.hiddenBlocks.contains(id)) {
            source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§e ")
                    .append(Text.translatable("text.bamboofps.already_present", id)));
            return 0;
        }

        config.hiddenBlocks.add(id);
        ConfigManager.save();
        BambooFPSClient.reloadWorldRenderers();

        source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§a ")
                .append(Text.translatable("text.bamboofps.add.block", id)));
        return 1;
    }

    private static int removeBlock(FabricClientCommandSource source, String id) {
        BambooFPSConfig config = ConfigManager.getConfig();
        if (!config.hiddenBlocks.remove(id)) {
            source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§c ")
                    .append(Text.translatable("text.bamboofps.not_present", id)));
            return 0;
        }

        ConfigManager.save();
        BambooFPSClient.reloadWorldRenderers();

        source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§a ")
                .append(Text.translatable("text.bamboofps.remove.block", id)));
        return 1;
    }

    private static int addItem(FabricClientCommandSource source, String id) {
        if (!isValidIdentifierOrTag(id, false)) {
            source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§c ")
                    .append(Text.translatable("text.bamboofps.invalid_id", id)));
            return 0;
        }

        BambooFPSConfig config = ConfigManager.getConfig();
        if (config.hiddenItems.contains(id)) {
            source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§e ")
                    .append(Text.translatable("text.bamboofps.already_present", id)));
            return 0;
        }

        config.hiddenItems.add(id);
        ConfigManager.save();

        source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§a ")
                .append(Text.translatable("text.bamboofps.add.item", id)));
        return 1;
    }

    private static int removeItem(FabricClientCommandSource source, String id) {
        BambooFPSConfig config = ConfigManager.getConfig();
        if (!config.hiddenItems.remove(id)) {
            source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§c ")
                    .append(Text.translatable("text.bamboofps.not_present", id)));
            return 0;
        }

        ConfigManager.save();

        source.sendFeedback(Text.literal("§8[§aBambooFPS§8]§a ")
                .append(Text.translatable("text.bamboofps.remove.item", id)));
        return 1;
    }

    private static boolean isValidIdentifierOrTag(String id, boolean block) {
        if (id == null || id.isBlank()) return false;
        String trimmed = id.trim();
        if (trimmed.startsWith("#")) {
            Identifier parsed = Identifier.tryParse(trimmed.substring(1));
            return parsed != null;
        }
        Identifier parsed = Identifier.tryParse(trimmed);
        if (parsed == null) return false;
        return block ? Registries.BLOCK.containsId(parsed) : Registries.ITEM.containsId(parsed);
    }
}
