package me.libraryaddict.librarys;

import me.libraryaddict.Hungergames.Hungergames;
import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Types.Gamer;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McpvpListener implements Listener {
    private Hungergames hg = HungergamesApi.getHungergames();
    private int respawnUntil;
    // private int joinUtil;
    private McPvP mcpvp;

    public McpvpListener(McPvP mcpvp) {
        this.mcpvp = mcpvp;
        this.respawnUntil = mcpvp.getConfig().getInt("RespawnDuration");
        // this.joinUtil = mcpvp.getConfig().getInt("JoinDuration");

    }

    /*@EventHandler(priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event) {
        if (hg.currentTime >= 0 && hg.currentTime < joinUtil && event.getPlayer().hasPermission("hungergames.vip.rejoin")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(mcpvp, new Runnable() {
                public void run() {
                    Gamer gamer = HungergamesApi.getPlayerManager().getGamer(event.getPlayer());
                    if (gamer != null)
                        gamer.setAlive(true);
                }
            }, 2);
        }
    }*/

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        if (event.getKilled().getPlayer().hasPermission("hungergames.vip.respawn") && hg.currentTime < respawnUntil) {
            final String killedName = event.getKilled().getName();
            Bukkit.getScheduler().scheduleSyncDelayedTask(mcpvp, new Runnable() {
                public void run() {
                    Gamer gamer = HungergamesApi.getPlayerManager().getGamer(killedName);
                    if (gamer != null) {
                        HungergamesApi.getPlayerManager().sendToSpawn(gamer);
                        gamer.setAlive(true);
                        HungergamesApi.getKitManager().getKitByPlayer(gamer.getPlayer()).addPlayer(gamer.getPlayer());
                    }
                }
            });
        }
    }
}
