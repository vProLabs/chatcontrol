package pl.vprolabs.vchatutils;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final vChatUtils plugin;
    private boolean chatEnabled;
    private boolean hideJoinMessages;
    private boolean hideLeaveMessages;
    private boolean hideAdvancements;
    private boolean polishAliases;
    private boolean englishAliases;
    private boolean luckPermsIntegration;
    private String prefix;
    private String currentLang;

    public ConfigManager(vChatUtils plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();
        chatEnabled = config.getBoolean("chat-enabled", true);
        hideJoinMessages = config.getBoolean("hide-join-messages", false);
        hideLeaveMessages = config.getBoolean("hide-leave-messages", false);
        hideAdvancements = config.getBoolean("hide-advancements", false);
        polishAliases = config.getBoolean("polish-aliases", true);
        englishAliases = config.getBoolean("english-aliases", true);
        luckPermsIntegration = config.getBoolean("luckperms-integration", false);
        prefix = config.getString("prefix", "&8[&cvProLabs&8] ");
        currentLang = config.getString("language", "en");
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    public void saveChatEnabled(boolean enabled) {
        chatEnabled = enabled;
        plugin.getConfig().set("chat-enabled", enabled);
        plugin.saveConfig();
    }

    public boolean isChatEnabled() { return chatEnabled; }
    public boolean isHideJoinMessages() { return hideJoinMessages; }
    public boolean isHideLeaveMessages() { return hideLeaveMessages; }
    public boolean isHideAdvancements() { return hideAdvancements; }
    public boolean isPolishAliases() { return polishAliases; }
    public boolean isEnglishAliases() { return englishAliases; }
    public boolean isLuckPermsIntegration() { return luckPermsIntegration; }
    public String getPrefix() { return prefix; }
    public String getCurrentLang() { return currentLang; }
}
