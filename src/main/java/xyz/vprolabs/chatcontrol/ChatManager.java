package xyz.vprolabs.chatcontrol;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ChatManager implements Listener {

    private final ChatControl plugin;
    private final Map<UUID, Long> slowmodeCooldowns = new ConcurrentHashMap<>();

    private static final int CLEAR_LINES = 300;

    public ChatManager(ChatControl plugin) {
        this.plugin = plugin;
    }

    private void sendClear(Player target) {
        Component blank = Component.text(" ");
        for (int i = 0; i < CLEAR_LINES; i++) {
            target.sendMessage(blank);
        }
    }

    public void clearChat(Player sender) {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendClear(player);
            }
            plugin.getMessageManager().sendAll("commands.clear.success", "%player%", sender.getName());
            plugin.getLogger().info(sender.getName() + " cleared the chat.");
        } catch (Exception t) {
            BugReport.log(t, "clearChat", "sender=" + sender.getName());
        }
    }

    public void clearChat(Player sender, Player target) {
        try {
            sendClear(target);
            plugin.getMessageManager().send(sender, "commands.clear.player", "%player%", target.getName());
            plugin.getLogger().info(sender.getName() + " cleared " + target.getName() + "'s chat.");
        } catch (Exception t) {
            BugReport.log(t, "clearChat", "sender=" + sender.getName() + " target=" + target.getName());
        }
    }

    public void clearChatForConsole(Player target) {
        try {
            sendClear(target);
            plugin.getMessageManager().sendAll("commands.clear.success", "%player%", "Console");
            plugin.getLogger().info("Console cleared " + target.getName() + "'s chat.");
        } catch (Exception t) {
            BugReport.log(t, "clearChatForConsole", "target=" + target.getName());
        }
    }

    public void clearChatForConsole() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendClear(player);
            }
            plugin.getMessageManager().sendAll("commands.clear.success", "%player%", "Console");
            plugin.getLogger().info("Console cleared the chat.");
        } catch (Exception t) {
            BugReport.log(t, "clearChatForConsole");
        }
    }

    public void toggleChat(CommandSender sender, boolean enable) {
        try {
            plugin.getConfigManager().saveChatEnabled(enable);

            String path = enable ? "commands.toggle.enabled" : "commands.toggle.disabled";
            plugin.getMessageManager().sendAll(path, "%player%", sender.getName());
        } catch (Exception t) {
            BugReport.log(t, "toggleChat", "enable=" + enable + " sender=" + sender.getName());
        }
    }

    public void showStatus(CommandSender sender) {
        try {
            MessageManager mm = plugin.getMessageManager();
            ConfigManager cm = plugin.getConfigManager();
            LuckPermsManager lm = plugin.getLuckPermsManager();

            Component chatStatus = cm.isChatEnabled() ? mm.get("commands.status.enabled") : mm.get("commands.status.disabled");
            Component joinStatus = cm.isHideJoinMessages() ? mm.get("commands.status.hidden") : mm.get("commands.status.visible");
            Component leaveStatus = cm.isHideLeaveMessages() ? mm.get("commands.status.hidden") : mm.get("commands.status.visible");
            Component advStatus = cm.isHideAdvancements() ? mm.get("commands.status.hidden") : mm.get("commands.status.visible");
            Component lpStatus = (cm.isLuckPermsIntegration() && lm.isHooked()) ? mm.get("commands.status.enabled") : mm.get("commands.status.disabled");

            mm.send(sender, "commands.status.header");
            mm.send(sender, "commands.status.chat", "%status%", serialize(chatStatus));
            mm.send(sender, "commands.status.join-messages", "%status%", serialize(joinStatus));
            mm.send(sender, "commands.status.leave-messages", "%status%", serialize(leaveStatus));
            mm.send(sender, "commands.status.advancements", "%status%", serialize(advStatus));
            mm.send(sender, "commands.status.luckperms", "%status%", serialize(lpStatus));

            if (cm.isLuckPermsIntegration() && lm.isHooked() && sender.hasPermission("chatcontrol.admin")) {
                mm.send(sender, "commands.status.bypass-groups", "%groups%", lm.getBypassGroups());
            }

            mm.send(sender, "commands.status.footer");
        } catch (Exception t) {
            BugReport.log(t, "showStatus", "sender=" + sender.getName());
        }
    }

    private String serialize(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        try {
            Player player = event.getPlayer();
            String message = event.getMessage();
            ConfigManager cm = plugin.getConfigManager();
            MessageManager mm = plugin.getMessageManager();

            if (player.hasPermission("chatcontrol.admin")) return;

            if (!cm.isChatEnabled() &&
                !plugin.getLuckPermsManager().hasChatBypass(player)) {
                event.setCancelled(true);
                player.sendMessage(mm.getLegacy("chat.disabled"));
                return;
            }

            if (cm.isEnableChatSlowmode()) {
                int slowmodeSeconds = cm.getChatSlowmode();
                if (slowmodeSeconds > 0) {
                    long currentTime = System.currentTimeMillis();
                    Long previous = slowmodeCooldowns.put(player.getUniqueId(), currentTime);
                    if (previous != null && currentTime - previous < slowmodeSeconds * 1000L) {
                        slowmodeCooldowns.put(player.getUniqueId(), previous);
                        event.setCancelled(true);
                        long secondsLeft = (slowmodeSeconds * 1000L - (currentTime - previous)) / 1000L + 1;
                        player.sendMessage(mm.getLegacy("chat.cooldown", "%seconds%", String.valueOf(secondsLeft)));
                        return;
                    }
                }
            }

            if (cm.isEnableChatFilter()) {
                for (String pattern : cm.getChatFilter()) {
                    if (message.matches(".*" + Pattern.quote(pattern) + ".*")) {
                        event.setCancelled(true);
                        player.sendMessage(mm.getLegacy("chat.filtered"));
                        return;
                    }
                }
            }

            if (cm.isEnableAllowedCharacters()) {
                Pattern allowedPattern = cm.getAllowedCharactersPattern();
                if (allowedPattern != null && !allowedPattern.matcher(message).matches()) {
                    event.setCancelled(true);
                    player.sendMessage(mm.getLegacy("chat.allowed-characters"));
                    return;
                }
            }

            if (cm.isEnableChatFormat()) {
                applyChatFormat(event, player);
            }
        } catch (Exception t) {
            BugReport.log(t, "onPlayerChat", "player=" + event.getPlayer().getName());
        }
    }

    private void applyChatFormat(AsyncPlayerChatEvent event, Player player) {
        try {
            if (!plugin.getConfigManager().isEnableChatFormat()) return;
            String format = plugin.getConfigManager().getChatFormat();

            String prefix = "";
            String suffix = "";

            if (plugin.getConfigManager().isLuckPermsIntegration() && plugin.getLuckPermsManager().isHooked()) {
                prefix = plugin.getLuckPermsManager().getPrefix(player);
                suffix = plugin.getLuckPermsManager().getSuffix(player);
            }

            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                try {
                    Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                    format = (String) papi.getMethod("setPlaceholders", org.bukkit.entity.Player.class, String.class)
                        .invoke(null, player, format);
                } catch (Exception ignored) {}
            }

            format = format.replace("{prefix}", toMiniMessage(prefix))
                           .replace("{suffix}", toMiniMessage(suffix))
                           .replace("{username}", "%1$s")
                           .replace("{message}", "%2$s");

            event.setFormat(LegacyComponentSerializer.legacySection().serialize(
                MiniMessage.miniMessage().deserialize(format)));
        } catch (Exception t) {
            BugReport.log(t, "applyChatFormat", "player=" + player.getName());
        }
    }

    private String toMiniMessage(String input) {
        if (input == null || input.isEmpty()) return "";
        if (input.contains("<") && input.contains(">") && !input.contains("\u00a7")) return input;
        String normalized = input.replace('&', '\u00a7');
        try {
            Component component = LegacyComponentSerializer.legacySection().deserialize(normalized);
            return MiniMessage.miniMessage().serialize(component);
        } catch (Exception e) {
            return normalized;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            if (plugin.getConfigManager().isHideJoinMessages()) {
                event.setJoinMessage(null);
            }
        } catch (Exception t) {
            BugReport.log(t, "onPlayerJoin", "player=" + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            slowmodeCooldowns.remove(event.getPlayer().getUniqueId());
            if (plugin.getConfigManager().isHideLeaveMessages()) {
                event.setQuitMessage(null);
            }
        } catch (Exception t) {
            BugReport.log(t, "onPlayerQuit", "player=" + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        try {
            if (plugin.getConfigManager().isHideAdvancements()) {
                try {
                    event.message(null);
                } catch (NoSuchMethodError e) {
                    plugin.getLogger().warning("Advancement hiding requires Paper or fork.");
                }
            }
        } catch (Exception t) {
            BugReport.log(t, "onAdvancement", "player=" + event.getPlayer().getName());
        }
    }
}
