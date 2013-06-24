package me.libraryaddict.librarys.Abilities;

import java.util.HashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Dwarf extends AbilityListener implements Disableable {
    private HashMap<Player, Long> startedSneaking = new HashMap<Player, Long>();
    private HashMap<Player, Long> cooldownExpires = new HashMap<Player, Long>();
    public int cooldown = 30;

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        if (hasAbility(p)) {
            if (cooldownExpires.containsKey(p) && cooldownExpires.get(p) < System.currentTimeMillis())
                cooldownExpires.remove(p);
            if (event.isSneaking()) {
                startedSneaking.put(p, System.currentTimeMillis());
            } else if (startedSneaking.containsKey(p)) {
                cooldownExpires.put(p, System.currentTimeMillis() + (cooldown * 1000));
                long sneakingTime = System.currentTimeMillis() - startedSneaking.remove(p);
                double knockBack = 0.5 * (sneakingTime / 1000);
                for (Entity entity : p.getNearbyEntities(knockBack, knockBack, knockBack)) {
                    if (entity instanceof Player && ((Player) entity).isSneaking())
                        continue;
                    Vector vector = entity.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
                    entity.setVelocity(vector.multiply(knockBack));
                }
            }
        }
    }
}
