package dev.itz0cat.bamboofps.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.itz0cat.bamboofps.BambooFPSClient;
import dev.itz0cat.bamboofps.config.BambooFPSConfig;
import dev.itz0cat.bamboofps.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new BambooFPSConfigScreen(parent);
    }

    public static class BambooFPSConfigScreen extends Screen {
        private final Screen parent;

        public BambooFPSConfigScreen(Screen parent) {
            super(Text.literal("BambooFPS Configuration"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            BambooFPSConfig config = ConfigManager.getConfig();
            int buttonWidth = 220;
            int buttonHeight = 20;
            int startY = this.height / 6;

            // Master Toggle Button
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("BambooFPS: " + (config.enabled ? "§aENABLED" : "§cDISABLED")),
                    button -> {
                        config.enabled = !config.enabled;
                        button.setMessage(Text.literal("BambooFPS: " + (config.enabled ? "§aENABLED" : "§cDISABLED")));
                        ConfigManager.save();
                        BambooFPSClient.reloadWorldRenderers();
                    }
            ).dimensions(this.width / 2 - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

            // Hide Blocks Toggle
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Hide Blocks: " + (config.hideBlocks ? "§aON" : "§cOFF")),
                    button -> {
                        config.hideBlocks = !config.hideBlocks;
                        button.setMessage(Text.literal("Hide Blocks: " + (config.hideBlocks ? "§aON" : "§cOFF")));
                        ConfigManager.save();
                        BambooFPSClient.reloadWorldRenderers();
                    }
            ).dimensions(this.width / 2 - buttonWidth / 2, startY + 26, buttonWidth, buttonHeight).build());

            // Hide Block Entities Toggle
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Hide Block Entities: " + (config.hideBlockEntities ? "§aON" : "§cOFF")),
                    button -> {
                        config.hideBlockEntities = !config.hideBlockEntities;
                        button.setMessage(Text.literal("Hide Block Entities: " + (config.hideBlockEntities ? "§aON" : "§cOFF")));
                        ConfigManager.save();
                        BambooFPSClient.reloadWorldRenderers();
                    }
            ).dimensions(this.width / 2 - buttonWidth / 2, startY + 52, buttonWidth, buttonHeight).build());

            // Hide Items Toggle
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Hide Items: " + (config.hideItems ? "§aON" : "§cOFF")),
                    button -> {
                        config.hideItems = !config.hideItems;
                        button.setMessage(Text.literal("Hide Items: " + (config.hideItems ? "§aON" : "§cOFF")));
                        ConfigManager.save();
                    }
            ).dimensions(this.width / 2 - buttonWidth / 2, startY + 78, buttonWidth, buttonHeight).build());

            // Cancel Particles Toggle
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Cancel Particles: " + (config.cancelParticles ? "§aON" : "§cOFF")),
                    button -> {
                        config.cancelParticles = !config.cancelParticles;
                        button.setMessage(Text.literal("Cancel Particles: " + (config.cancelParticles ? "§aON" : "§cOFF")));
                        ConfigManager.save();
                    }
            ).dimensions(this.width / 2 - buttonWidth / 2, startY + 104, buttonWidth, buttonHeight).build());

            // Done / Back Button
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Done"),
                    button -> {
                        if (this.client != null) {
                            this.client.setScreen(this.parent);
                        }
                    }
            ).dimensions(this.width / 2 - buttonWidth / 2, startY + 140, buttonWidth, buttonHeight).build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7Use '/bamboofps add/remove block/item <id>' for block/item lists"), this.width / 2, this.height - 25, 0xAAAAAA);
        }

        @Override
        public void close() {
            if (this.client != null) {
                this.client.setScreen(this.parent);
            }
        }
    }
}
