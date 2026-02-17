package pl.vprolabs.vchatutils;

import org.bukkit.plugin.java.JavaPlugin;

public class vChatUtils extends JavaPlugin {

    private static vChatUtils instance;
    private ChatManager chatManager;
    private LuckPermsManager luckPermsManager;
    private MessageManager messageManager;
    private ConfigManager configManager;
    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        luckPermsManager = new LuckPermsManager(this);
        chatManager = new ChatManager(this);
        commandHandler = new CommandHandler(this);

        getServer().getPluginManager().registerEvents(chatManager, this);

        getLogger().info("[vChatUtils] Plugin enabled! Version 1.0.2 by vProLabs");
        getLogger().info("[vChatUtils] Language: " + configManager.getCurrentLang().toUpperCase());

        if (configManager.isLuckPermsIntegration() && luckPermsManager.isHooked()) {
            getLogger().info("[vChatUtils] LuckPerms integration enabled!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("[vChatUtils] Plugin disabled.");
    }

    public static vChatUtils getInstance() { return instance; }
    public ChatManager getChatManager() { return chatManager; }
    public LuckPermsManager getLuckPermsManager() { return luckPermsManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public CommandHandler getCommandHandler() { return commandHandler; }
}
