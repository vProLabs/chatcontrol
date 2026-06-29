package xyz.vprolabs.chatcontrol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatControl extends JavaPlugin {

    private static ChatControl instance;
    private ChatManager chatManager;
    private LuckPermsManager luckPermsManager;
    private MessageManager messageManager;
    private ConfigManager configManager;
    private CommandHandler commandHandler;
    private boolean vapiInstalled = false;

    @Override
    public void onLoad() {
        File vapiFile = new File("plugins", "vAPI.jar");
        if (!vapiFile.exists()) {
            getLogger().info("╔══════════════════════════════════════════╗");
            getLogger().info("║  vAPI not found! Auto-downloading...    ║");
            getLogger().info("╚══════════════════════════════════════════╝");
            try {
                HttpURLConnection conn = (HttpURLConnection) URI.create("https://www.vprolabs.xyz/api/download").toURL().openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);
                int responseCode = conn.getResponseCode();
                if (responseCode == 200 || responseCode == 302) {
                    try (InputStream in = conn.getInputStream();
                         FileOutputStream out = new FileOutputStream(vapiFile)) {
                        in.transferTo(out);
                    }
                    vapiInstalled = true;
                    getLogger().info("╔══════════════════════════════════════════╗");
                    getLogger().info("║  vAPI downloaded to plugins/vAPI.jar    ║");
                    getLogger().info("║  Server restart required to activate!   ║");
                    getLogger().info("╚══════════════════════════════════════════╝");
                } else {
                    getLogger().warning("vAPI download returned HTTP " + responseCode);
                }
            } catch (Exception e) {
                getLogger().severe("Failed to download vAPI: " + e.getMessage());
                getLogger().severe("Download manually: https://www.vprolabs.xyz/api/download");
            }
        }
    }

    @Override
    public void onEnable() {
        // === vAPI dependency check ===
        if (Bukkit.getPluginManager().getPlugin("vAPI") == null) {
            getLogger().severe("╔══════════════════════════════════════════╗");
            getLogger().severe("║  vAPI IS REQUIRED but not loaded!      ║");
            getLogger().severe("╚══════════════════════════════════════════╝");
            if (vapiInstalled) {
                startRestartReminder();
            } else {
                getLogger().severe("Download: https://www.vprolabs.xyz/api/download");
                getLogger().severe("Place vAPI.jar in plugins/ folder and restart.");
            }
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        // === existing onEnable code follows ===
        try {
            instance = this;

            saveDefaultConfig();
            ConfigMigrator.migrate(this);
            reloadConfig();

            configManager = new ConfigManager(this);
            messageManager = new MessageManager(this);
            luckPermsManager = new LuckPermsManager(this);
            chatManager = new ChatManager(this);
            commandHandler = new CommandHandler(this);

            getServer().getPluginManager().registerEvents(chatManager, this);

            getLogger().info("[ChatControl] Plugin enabled! Version " + getDescription().getVersion() + " by vProLabs");
            getLogger().info("[ChatControl] Language: " + configManager.getCurrentLang().toUpperCase());
        } catch (Throwable t) {
            BugReport.log(t, "Plugin enable");
            getLogger().severe("[ChatControl] Failed to enable plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("[ChatControl] Plugin disabled.");
    }

    public static ChatControl getInstance() { return instance; }
    public ChatManager getChatManager() { return chatManager; }
    public LuckPermsManager getLuckPermsManager() { return luckPermsManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public CommandHandler getCommandHandler() { return commandHandler; }

    private void startRestartReminder() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            String msg = "§8[§cvPlugins§8] §evAPI has been installed. §cServer requires a restart §eto initialize it.";
            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.isOp() || p.hasPermission("vplugind.admin"))
                .forEach(p -> p.sendMessage(msg));
            getLogger().warning("vAPI has been installed. Server requires a restart to initialize it.");
        }, 0L, 6000L);
    }
}
