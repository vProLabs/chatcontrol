package pl.vprolabs.vchatutils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    private final vChatUtils plugin;

    public CommandHandler(vChatUtils plugin) {
        this.plugin = plugin;
        plugin.getCommand("chat").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("chat")) return false;

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessageManager().get("commands.usage"));
            return true;
        }

        String sub = args[0].toLowerCase();
        boolean valid = false;

        if (plugin.getConfigManager().isEnglishAliases()) {
            switch (sub) {
                case "clear": case "on": case "off": case "status": case "info": case "reload":
                    valid = true; break;
            }
        }

        if (plugin.getConfigManager().isPolishAliases() && !valid) {
            switch (sub) {
                case "wyczysc": case "wlacz": case "wylacz": case "przeladuj":
                    valid = true; break;
            }
        }

        if (!valid) {
            sender.sendMessage(plugin.getMessageManager().get("commands.unknown"));
            return true;
        }

        switch (sub) {
            case "clear": case "wyczysc":
                if (sender instanceof Player) {
                    plugin.getChatManager().clearChat((Player) sender);
                } else {
                    plugin.getChatManager().clearChatForConsole();
                }
                return true;
            case "on": case "wlacz":
                plugin.getChatManager().toggleChat(sender, true);
                return true;
            case "off": case "wylacz":
                plugin.getChatManager().toggleChat(sender, false);
                return true;
            case "status": case "info":
                plugin.getChatManager().showStatus(sender);
                return true;
            case "reload": case "przeladuj":
                reload(sender);
                return true;
            default:
                sender.sendMessage(plugin.getMessageManager().get("commands.unknown"));
                return true;
        }
    }

    private void reload(CommandSender sender) {
        try {
            plugin.getConfigManager().reload();
            plugin.getMessageManager().load();
            plugin.getLuckPermsManager();

            sender.sendMessage(plugin.getMessageManager().get("other.reload-success"));
            plugin.getLogger().info("[vChatUtils] Config reloaded by " + sender.getName());
        } catch (Exception e) {
            sender.sendMessage(plugin.getMessageManager().get("other.reload-fail"));
            plugin.getLogger().severe("[vChatUtils] Failed to reload: " + e.getMessage());
        }
    }
}
