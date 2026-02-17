package pl.vprolabs.vchatutils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatManager implements Listener {

    private final vChatUtils plugin;

    public ChatManager(vChatUtils plugin) {
        this.plugin = plugin;
    }

    public void clearChat(Player sender) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
                player.sendMessage("");
            }
        }

        String playerName = sender.getName();
        String msg = plugin.getMessageManager().get("commands.clear.success", "%player%", playerName);
        Bukkit.broadcastMessage(msg);
        plugin.getLogger().info(plugin.getMessageManager().get("commands.clear.console", "%player%", playerName));
    }

    public void clearChatForConsole() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
                player.sendMessage("");
            }
        }
        String msg = plugin.getMessageManager().get("commands.clear.success", "%player%", "Console");
        Bukkit.broadcastMessage(msg);
        plugin.getLogger().info(plugin.getMessageManager().get("commands.clear.console", "%player%", "Console"));
    }

    public void toggleChat(CommandSender sender, boolean enable) {
        plugin.getConfigManager().saveChatEnabled(enable);

        String playerName = sender.getName();
        String msg;

        if (enable) {
            msg = plugin.getMessageManager().get("commands.toggle.enabled", "%player%", playerName);
            plugin.getLogger().info(plugin.getMessageManager().get("commands.toggle.console-enabled", "%player%", playerName));
        } else {
            msg = plugin.getMessageManager().get("commands.toggle.disabled", "%player%", playerName);
            plugin.getLogger().info(plugin.getMessageManager().get("commands.toggle.console-disabled", "%player%", playerName));
        }

        Bukkit.broadcastMessage(msg);
    }

    public void showStatus(CommandSender sender) {
        MessageManager mm = plugin.getMessageManager();
        ConfigManager cm = plugin.getConfigManager();
        LuckPermsManager lm = plugin.getLuckPermsManager();

        String chatStatus = cm.isChatEnabled() ? mm.get("commands.status.enabled") : mm.get("commands.status.disabled");
        String joinStatus = cm.isHideJoinMessages() ? mm.get("commands.status.hidden") : mm.get("commands.status.visible");
        String leaveStatus = cm.isHideLeaveMessages() ? mm.get("commands.status.hidden") : mm.get("commands.status.visible");
        String advStatus = cm.isHideAdvancements() ? mm.get("commands.status.hidden") : mm.get("commands.status.visible");
        String lpStatus = (cm.isLuckPermsIntegration() && lm.isHooked()) ? mm.get("commands.status.enabled") : mm.get("commands.status.disabled");

        sender.sendMessage(mm.get("commands.status.header"));
        sender.sendMessage(mm.get("commands.status.chat", "%status%", chatStatus));
        sender.sendMessage(mm.get("commands.status.join-messages", "%status%", joinStatus));
        sender.sendMessage(mm.get("commands.status.leave-messages", "%status%", leaveStatus));
        sender.sendMessage(mm.get("commands.status.advancements", "%status%", advStatus));
        sender.sendMessage(mm.get("commands.status.luckperms", "%status%", lpStatus));

        if (cm.isLuckPermsIntegration() && lm.isHooked() && sender.hasPermission("vchatutils.admin")) {
            sender.sendMessage(mm.get("commands.status.bypass-groups", "%groups%", lm.getBypassGroups()));
        }

        sender.sendMessage(mm.get("commands.status.footer"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getConfigManager().isChatEnabled() &&
            !plugin.getLuckPermsManager().hasChatBypass(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessageManager().get("chat.disabled"));
            return;
        }

        if (plugin.getConfigManager().isLuckPermsIntegration() &&
            plugin.getLuckPermsManager().isHooked()) {
            String prefix = plugin.getLuckPermsManager().getPrefix(player);
            String suffix = plugin.getLuckPermsManager().getSuffix(player);
            String format = prefix + player.getName() + suffix + " &8» &f%2$s";
            event.setFormat(ChatColor.translateAlternateColorCodes('&', format));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getConfigManager().isHideJoinMessages()) {
            event.joinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getConfigManager().isHideLeaveMessages()) {
            event.quitMessage(null);
        }
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (plugin.getConfigManager().isHideAdvancements()) {
            event.message(null);
        }
    }
}
