package pl.vprolabs.vchatutils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LuckPermsManager {

    private final vChatUtils plugin;
    private Object luckPerms;
    private boolean hooked = false;

    public LuckPermsManager(vChatUtils plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        if (!plugin.getConfigManager().isLuckPermsIntegration()) return;

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            try {
                Class<?> luckPermsProvider = Class.forName("net.luckperms.api.LuckPermsProvider");
                luckPerms = luckPermsProvider.getMethod("get").invoke(null);
                hooked = true;
                plugin.getLogger().info("[vChatUtils] LuckPerms hooked successfully!");
            } catch (Exception e) {
                plugin.getLogger().warning("[vChatUtils] Failed to hook LuckPerms: " + e.getMessage());
            }
        } else {
            plugin.getLogger().warning("[vChatUtils] LuckPerms integration enabled but LuckPerms not found!");
        }
    }

    public boolean isHooked() { return hooked; }

    public String getPrefix(Player player) {
        if (!hooked) return "";
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
            if (user != null) {
                Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
                Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
                String prefix = (String) metaData.getClass().getMethod("getPrefix").invoke(metaData);
                return prefix != null ? ChatColor.translateAlternateColorCodes('&', prefix) : "";
            }
        } catch (Exception e) {}
        return "";
    }

    public String getSuffix(Player player) {
        if (!hooked) return "";
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
            if (user != null) {
                Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
                Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
                String suffix = (String) metaData.getClass().getMethod("getSuffix").invoke(metaData);
                return suffix != null ? ChatColor.translateAlternateColorCodes('&', suffix) : "";
            }
        } catch (Exception e) {}
        return "";
    }

    public boolean hasChatBypass(Player player) {
        if (player.hasPermission("vchatutils.bypass")) return true;
        if (!hooked) return false;

        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
            if (user != null) {
                Object queryOptions = user.getClass().getMethod("getQueryOptions").invoke(user);
                Object inheritedGroups = user.getClass().getMethod("getInheritedGroups", queryOptions.getClass()).invoke(user, queryOptions);

                for (Object group : (Iterable<?>) inheritedGroups) {
                    Object groupCachedData = group.getClass().getMethod("getCachedData").invoke(group);
                    Object permissionData = groupCachedData.getClass().getMethod("getPermissionData").invoke(groupCachedData);
                    Object checkResult = permissionData.getClass().getMethod("checkPermission", String.class).invoke(permissionData, "vchatutils.bypass");
                    boolean hasPerm = (Boolean) checkResult.getClass().getMethod("asBoolean").invoke(checkResult);
                    if (hasPerm) return true;
                }
            }
        } catch (Exception e) {}
        return false;
    }

    public String getBypassGroups() {
        if (!hooked) return "LuckPerms not enabled";
        try {
            Object groupManager = luckPerms.getClass().getMethod("getGroupManager").invoke(luckPerms);
            java.util.Collection<?> loadedGroups = (java.util.Collection<?>) groupManager.getClass().getMethod("getLoadedGroups").invoke(groupManager);

            StringBuilder result = new StringBuilder();
            for (Object group : loadedGroups) {
                Object groupCachedData = group.getClass().getMethod("getCachedData").invoke(group);
                Object permissionData = groupCachedData.getClass().getMethod("getPermissionData").invoke(groupCachedData);
                Object checkResult = permissionData.getClass().getMethod("checkPermission", String.class).invoke(permissionData, "vchatutils.bypass");
                boolean hasPerm = (Boolean) checkResult.getClass().getMethod("asBoolean").invoke(checkResult);

                if (hasPerm) {
                    if (result.length() > 0) result.append(", ");
                    String groupName = (String) group.getClass().getMethod("getName").invoke(group);
                    result.append(groupName);
                }
            }
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
