package me.libraryaddict.librarys.Abilities;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.Gamer;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Vulture extends AbilityListener {
    private class KillInfo {
        private String cause;
        private String deathLoc;
        private String killedName;
        private String killerName;
        private int time;

        public KillInfo(Location loc, Gamer killed, Gamer killer) {
            time = HungergamesApi.getHungergames().currentTime;
            killedName = killed.getName();
            if (killer == null)
                killerName = noKiller;
            else
                killerName = killer.getName();
            deathLoc = String.format(locationLayout, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            cause = killed.getPlayer().getLastDamageCause().getCause().name();
            if (deathCauses.containsKey(cause))
                cause = deathCauses.get(cause);
        }

        public String getCause() {
            return cause;
        }

        public String getKilled() {
            return killedName;
        }

        public String getKiller() {
            return killerName;
        }

        public String getLocation() {
            return deathLoc;
        }

        public String getTime() {
            return HungergamesApi.getHungergames().returnTime(time);
        }
    }

    public String bookName = "Death Note";
    public String[] damageCauses = new String[] { "BLOCK_EXPLOSION Block explosion", "CONTACT Cactus", "CUSTOM Unknown",
            "DROWNING Drowning", "ENTITY_ATTACK Entity Attack", "ENTITY_EXPLOSION Explosion", "FALL Fall",
            "FALLING_BLOCK Falling Block", "FIRE fire", "FIRE_TICK Fire", "LAVA Lava", "LIGHTNING Lightning", "MAGIC Magic",
            "MELTING Melting", "POISON Poison", "PROJECTILE Projectile", "STARVATION Starvation", "SUFFOCATION Suffocation",
            "SUICIDE Suicide", "THORNS Thorns", "VOID Void", "WITHER Wither" };
    private HashMap<String, String> deathCauses = new HashMap<String, String>();
    private ArrayList<KillInfo> kills = new ArrayList<KillInfo>();
    public String killString = "Time " + ChatColor.WHITE + "-" + ChatColor.RED + "Killed " + ChatColor.WHITE + "-"
            + ChatColor.BLUE + "Cause " + ChatColor.WHITE + "-" + ChatColor.GREEN + "Killer " + ChatColor.WHITE + "-"
            + ChatColor.YELLOW + "Location";
    public String killStringLayout = "%Time% " + ChatColor.WHITE + "-" + ChatColor.RED + "%Killed% " + ChatColor.WHITE + "-"
            + ChatColor.BLUE + "%Cause% " + ChatColor.WHITE + "-" + ChatColor.GREEN + "%Killer% " + ChatColor.WHITE + "-"
            + ChatColor.YELLOW + "%Location%";
    public String locationLayout = "(%s, %s, %s)";
    public String noKiller = "None";
    public String noKillsYet = ChatColor.BLUE + "There are no kills yet! Care to be the first?";

    public void init() {
        for (String s : damageCauses) {
            String[] split = s.split(" ");
            deathCauses.put(split[0], s.substring(1 + split[0].length()));
        }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        KillInfo killInfo = new KillInfo(event.getDropsLocation(), event.getKilled(), event.getKillerPlayer());
        kills.add(killInfo);
        if (kills.size() > 10)
            kills.remove(0);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction().name().contains("RIGHT")) {
            if (isSpecialItem(event.getItem(), bookName)) {
                Player p = event.getPlayer();
                if (kills.size() == 0)
                    p.sendMessage(noKillsYet);
                else {
                    p.sendMessage(killString);
                    for (KillInfo info : kills) {
                        p.sendMessage(String.format(killStringLayout, info.getTime(), info.getKilled(), info.getCause(),
                                info.getKiller(), info.getLocation()));
                    }
                }
            }
        }
    }
}
