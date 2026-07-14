package xyz.vprolabs.chatcontrol;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.regex.Pattern;

public class ConfigManager {

    private final ChatControl plugin;
    private boolean chatEnabled;
    private boolean hideJoinMessages;
    private boolean hideLeaveMessages;
    private boolean hideAdvancements;
    private boolean polishAliases;
    private boolean englishAliases;
    private boolean shortAlias;
    private boolean luckPermsIntegration;
    private String prefix;
    private String currentLang;
    private int chatSlowmode;
    private boolean enableChatSlowmode;
    private List<String> chatFilter;
    private boolean enableChatFilter;
    private String chatFormat;
    private boolean enableChatFormat;
    private boolean enableAllowedCharacters;
    private Pattern allowedCharactersPattern;

    public ConfigManager(ChatControl plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        try {
            FileConfiguration config = plugin.getConfig();
            chatEnabled = config.getBoolean("chat-enabled", true);
            hideJoinMessages = config.getBoolean("hide-join-messages", false);
            hideLeaveMessages = config.getBoolean("hide-leave-messages", false);
            hideAdvancements = config.getBoolean("hide-advancements", false);
            polishAliases = config.getBoolean("polish-aliases", false);
            englishAliases = config.getBoolean("english-aliases", true);
            shortAlias = config.getBoolean("short-alias", true);
            luckPermsIntegration = config.getBoolean("luckperms-integration", false);
            prefix = config.getString("prefix");
            if (prefix == null || prefix.isEmpty()) {
                prefix = "<dark_gray>[<red>vProLabs<dark_gray>] ";
            }
            currentLang = config.getString("language", "en");
            chatSlowmode = config.getInt("chat-slowmode", 3);
            enableChatSlowmode = config.getBoolean("enable-chat-slowmode", true);
            chatFilter = config.getStringList("chat-filter");
            enableChatFilter = config.getBoolean("enable-chat-filter", true);
            chatFormat = config.getString("chat-format", "{prefix}{suffix}<white>{username}</white> <dark_gray>\u00bb</dark_gray> <white>{message}</white>");
            enableChatFormat = config.getBoolean("enable-chat-format", true);
            enableAllowedCharacters = config.getBoolean("enable-allowed-characters", true);
            String patternStr = config.getString("allowed-characters-regex");
            if (patternStr == null || patternStr.isEmpty()) {
                patternStr = "^[a-zA-Z0-9\\s\\-_/\\\\.,!?;:'\"()\\[\\]{}@#$%^&*+=<>~`]+$";
            }
            try {
                allowedCharactersPattern = Pattern.compile(patternStr);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid allowed-characters-regex, using default: " + e.getMessage());
                allowedCharactersPattern = Pattern.compile("^[a-zA-Z0-9\\s\\-_/\\\\.,!?;:'\"()\\[\\]{}@#$%^&*+=<>~`]+$");
            }
        } catch (Exception t) {
            BugReport.log(t, "ConfigManager.load");
        }
    }

    public void reload() {
        try {
            plugin.reloadConfig();
            load();
        } catch (Exception t) {
            BugReport.log(t, "ConfigManager.reload");
        }
    }

    public void saveChatEnabled(boolean enabled) {
        try {
            chatEnabled = enabled;
            plugin.getConfig().set("chat-enabled", enabled);
            plugin.saveConfig();
        } catch (Exception t) {
            BugReport.log(t, "saveChatEnabled", "enabled=" + enabled);
        }
    }

    public boolean isChatEnabled() { return chatEnabled; }
    public boolean isHideJoinMessages() { return hideJoinMessages; }
    public boolean isHideLeaveMessages() { return hideLeaveMessages; }
    public boolean isHideAdvancements() { return hideAdvancements; }
    public boolean isPolishAliases() { return polishAliases; }
    public boolean isEnglishAliases() { return englishAliases; }
    public boolean isShortAlias() { return shortAlias; }
    public boolean isLuckPermsIntegration() { return luckPermsIntegration; }
    public String getPrefix() { return prefix; }
    public String getCurrentLang() { return currentLang; }
    public int getChatSlowmode() { return chatSlowmode; }
    public boolean isEnableChatSlowmode() { return enableChatSlowmode; }
    public List<String> getChatFilter() { return chatFilter; }
    public boolean isEnableChatFilter() { return enableChatFilter; }
    public String getChatFormat() { return chatFormat; }
    public boolean isEnableChatFormat() { return enableChatFormat; }
    public boolean isEnableAllowedCharacters() { return enableAllowedCharacters; }
    public Pattern getAllowedCharactersPattern() { return allowedCharactersPattern; }
}
