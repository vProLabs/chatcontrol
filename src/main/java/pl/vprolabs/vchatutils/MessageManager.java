package pl.vprolabs.vchatutils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final vChatUtils plugin;
    private FileConfiguration messages;
    private Map<String, FileConfiguration> messageFiles = new HashMap<>();

    public MessageManager(vChatUtils plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveResource("lang/messages-en.yml", false);
        plugin.saveResource("lang/messages-pl.yml", false);

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (langFolder.exists()) {
            for (File file : langFolder.listFiles()) {
                if (file.getName().startsWith("messages-") && file.getName().endsWith(".yml")) {
                    String lang = file.getName().replace("messages-", "").replace(".yml", "");
                    messageFiles.put(lang, YamlConfiguration.loadConfiguration(file));
                }
            }
        }

        String currentLang = plugin.getConfigManager().getCurrentLang();
        if (messageFiles.containsKey(currentLang)) {
            messages = messageFiles.get(currentLang);
        } else {
            messages = messageFiles.getOrDefault("en", new YamlConfiguration());
            plugin.getLogger().warning("[vChatUtils] Language '" + currentLang + "' not found! Using English.");
        }
    }

    public String get(String path) {
        String msg = messages.getString(path, "&cMissing message: " + path);
        return ChatColor.translateAlternateColorCodes('&', msg.replace("%prefix%", plugin.getConfigManager().getPrefix()));
    }

    public String get(String path, String... placeholders) {
        String msg = get(path);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return msg;
    }
}
