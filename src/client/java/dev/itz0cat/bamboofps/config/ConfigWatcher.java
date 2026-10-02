package dev.itz0cat.bamboofps.config;

import dev.itz0cat.bamboofps.BambooFPSClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.*;

public class ConfigWatcher implements Runnable {
    private static Thread watcherThread;
    private static volatile boolean running = true;

    public static synchronized void start() {
        if (watcherThread != null && watcherThread.isAlive()) {
            return;
        }

        running = true;
        watcherThread = new Thread(new ConfigWatcher(), "BambooFPS-ConfigWatcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    public static synchronized void stop() {
        running = false;
        if (watcherThread != null) {
            watcherThread.interrupt();
        }
    }

    @Override
    public void run() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

            while (running && !Thread.currentThread().isInterrupted()) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    break;
                }

                boolean configChanged = false;
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                    Path context = (Path) event.context();
                    if (context != null && "bamboofps.json".equals(context.getFileName().toString())) {
                        configChanged = true;
                    }
                }

                if (configChanged) {
                    // Small delay to prevent reading partial writes
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}

                    ConfigManager.load();
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null) {
                        client.execute(BambooFPSClient::reloadWorldRenderers);
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }
        } catch (IOException e) {
            ConfigManager.LOGGER.error("[BambooFPS] Config Watcher encountered error: {}", e.getMessage());
        }
    }
}
