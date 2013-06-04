package me.libraryaddict.librarys;

import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.Hungergames.Types.Kit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class McPvP extends JavaPlugin implements Listener {
    private String latestVersion;
    private String currentVersion;

    public void onEnable() {
        saveDefaultConfig();
        HungergamesApi.getAbilityManager().initializeAllAbilitiesInPackage(this, "me.libraryaddict.librarys.Abilities");
        for (String string : getConfig().getConfigurationSection("Kits").getKeys(false)) {
            if (getConfig().contains("BadKits") && getConfig().getStringList("BadKits").contains(string))
                continue;
            Kit kit = HungergamesApi.getKitManager().parseKit(getConfig().getConfigurationSection("Kits." + string));
            HungergamesApi.getKitManager().addKit(kit);
        }
        if (HungergamesApi.getHungergames().getConfig().getBoolean("CheckUpdates"))
            Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                public void run() {
                    try {
                        UpdateChecker updateChecker = new UpdateChecker();
                        updateChecker.checkUpdate("v"
                                + Bukkit.getPluginManager().getPlugin("LibsMcpvp").getDescription().getVersion());
                        latestVersion = updateChecker.getLatestVersion();
                        if (latestVersion != null) {
                            latestVersion = "v" + latestVersion;
                            for (Player p : Bukkit.getOnlinePlayers())
                                if (p.hasPermission("hungergames.update"))
                                    p.sendMessage(String.format(ChatColor.GOLD + "[Libs MCPVP] " + ChatColor.DARK_GREEN
                                            + "There is a update ready to be downloaded! You are using " + ChatColor.GREEN + "%s"
                                            + ChatColor.DARK_GREEN + ", the new version is " + ChatColor.GREEN + "%s"
                                            + ChatColor.DARK_GREEN + "!", currentVersion, latestVersion));
                        }
                    } catch (Exception ex) {
                        System.out.print(String.format("[Libs MCPVP] Failed to check for update: %s", ex.getMessage()));
                    }
                }
            });
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new McpvpListener(this), this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (latestVersion != null && p.hasPermission("hungergames.update"))
            p.sendMessage(String.format(ChatColor.GOLD + "[Libs MCPVP] " + ChatColor.DARK_GREEN
                    + "There is a update ready to be downloaded! You are using " + ChatColor.GREEN + "%s" + ChatColor.DARK_GREEN
                    + ", the new version is " + ChatColor.GREEN + "%s" + ChatColor.DARK_GREEN + "!", currentVersion,
                    latestVersion));
    }
}
