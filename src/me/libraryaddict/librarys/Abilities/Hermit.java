package me.libraryaddict.librarys.Abilities;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import me.libraryaddict.Hungergames.Events.GameStartEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Hermit extends AbilityListener implements Disableable {
    public int timesToLoop = 100;
    public String failedToFindLocation = ChatColor.GREEN + "So sorry! I was unable to find a location for you to spawn at!";

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(HungergamesApi.getHungergames(), new Runnable() {
            public void run() {
                double borderSize = HungergamesApi.getConfigManager().getBorderSize();
                Location spawn = HungergamesApi.getHungergames().world.getSpawnLocation();
                for (Player p : getMyPlayers()) {
                    // Now lets make it so he never spawns less then half the border size
                    for (int i = 0; i < timesToLoop; i++) {
                        Location loc = new Location(spawn.getWorld(),
                                spawn.getX() + new Random().nextInt((int) (borderSize / 2)), 0, spawn.getZ()
                                        + new Random().nextInt((int) (borderSize / 2)));
                        loc = loc.getWorld().getHighestBlockAt(loc).getLocation();
                        if (loc.getBlock().isLiquid()) {
                            if (i + 1 == timesToLoop)
                                p.sendMessage(failedToFindLocation);
                            continue;
                        }
                        p.teleport(loc.add(0, 1, 0));
                        break;
                    }

                }
            }
        });
    }

}
