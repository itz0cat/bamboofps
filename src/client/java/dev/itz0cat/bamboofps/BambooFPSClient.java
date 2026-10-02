package dev.itz0cat.bamboofps;

import dev.itz0cat.bamboofps.command.BambooFPSCommand;
import dev.itz0cat.bamboofps.config.BambooFPSConfig;
import dev.itz0cat.bamboofps.config.ConfigManager;
import dev.itz0cat.bamboofps.config.ConfigWatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class BambooFPSClient implements ClientModInitializer {
    public static final String MOD_ID = "bamboofps";

    private static final KeyBinding.Category BAMBOOFPS_CATEGORY = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
    private static KeyBinding toggleKeyBinding;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        ConfigWatcher.start();

        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bamboofps.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                BAMBOOFPS_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKeyBinding.wasPressed()) {
                toggleMod(client);
            }
        });

        BambooFPSCommand.register();
    }

    public static void toggleMod(MinecraftClient client) {
        BambooFPSConfig config = ConfigManager.getConfig();
        config.enabled = !config.enabled;
        ConfigManager.save();
        reloadWorldRenderers();

        if (client != null && client.player != null) {
            Text message = Text.translatable(config.enabled ? "text.bamboofps.status.enabled" : "text.bamboofps.status.disabled");
            client.player.sendMessage(message, true);
        }
    }

    public static void reloadWorldRenderers() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.worldRenderer != null) {
            client.worldRenderer.reload();
        }
    }
}
