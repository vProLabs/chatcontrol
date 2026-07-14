package xyz.vprolabs.chatcontrol;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public final class ConfigMigrator {

    private ConfigMigrator() {}

    private static final String OLD_NAME = "vChatUtils";

    public static void migrate(JavaPlugin plugin) {
        File dataFolder = plugin.getDataFolder();
        File parent = dataFolder.getParentFile();

        File oldFolder = new File(parent, OLD_NAME);
        File oldConfig = new File(oldFolder, "config.yml");

        if (oldConfig.exists()) {
            plugin.getLogger().info("Found old vChatUtils config. Migrating...");
            try {
                FileConfiguration old = YamlConfiguration.loadConfiguration(oldConfig);
                FileConfiguration current = YamlConfiguration.loadConfiguration(new File(dataFolder, "config.yml"));

                boolean changed = false;
                for (String key : old.getKeys(true)) {
                    if (!old.isConfigurationSection(key) && !current.contains(key)) {
                        current.set(key, old.get(key));
                        changed = true;
                    }
                }

                if (changed) {
                    current.save(new File(dataFolder, "config.yml"));
                }

                File oldLang = new File(oldFolder, "lang");
                File langDir = new File(dataFolder, "lang");
                if (oldLang.isDirectory() && !langDir.exists()) {
                    copyDir(oldLang, langDir);
                }

                File oldBak = new File(parent, OLD_NAME + "_old");
                if (!oldFolder.renameTo(oldBak)) {
                    plugin.getLogger().warning("Could not rename old vChatUtils folder to " + oldBak.getName());
                } else {
                    plugin.getLogger().info("Old config backed up to " + oldBak.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Config migration failed: " + e.getMessage());
            }
        }

        fillMissingDefaults(plugin);
    }

    private static void fillMissingDefaults(JavaPlugin plugin) {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) return;

            FileConfiguration current = YamlConfiguration.loadConfiguration(configFile);

            InputStream defStream = plugin.getClass().getClassLoader().getResourceAsStream("config.yml");
            if (defStream == null) return;

            FileConfiguration defaults;
            try (InputStreamReader reader = new InputStreamReader(defStream, StandardCharsets.UTF_8)) {
                defaults = YamlConfiguration.loadConfiguration(reader);
            }
            boolean changed = false;

            for (String key : defaults.getKeys(true)) {
                if (!defaults.isConfigurationSection(key) && !current.contains(key)) {
                    current.set(key, defaults.get(key));
                    changed = true;
                }
            }

            if (changed) {
                current.save(configFile);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to fill missing config defaults: " + e.getMessage());
        }
    }

    private static void copyDir(File src, File dst) {
        if (src.isDirectory()) {
            dst.mkdirs();
            File[] files = src.listFiles();
            if (files != null) {
                for (File f : files) {
                    copyDir(f, new File(dst, f.getName()));
                }
            }
        } else {
            try {
                Files.copy(src.toPath(), dst.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                System.err.println("[ChatControl] Failed to copy " + src.getName() + ": " + e.getMessage());
            }
        }
    }
}
