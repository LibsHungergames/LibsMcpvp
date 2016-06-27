package me.libraryaddict.librarys.Abilities;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class Turtle extends AbilityListener implements Disableable {
    public boolean needToBlock = true;

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && hasAbility((Player) event.getDamager())) {
            Player p = (Player) event.getDamager();
            if (p.isSneaking() && (!needToBlock || p.isBlocking())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && hasAbility((Player) event.getEntity())) {
            Player p = (Player) event.getEntity();
            if (p.isSneaking() && (!needToBlock || p.isBlocking()) && p.getHealth() > 1) {
                event.setCancelled(true);
                p.damage(0);
                p.setHealth(p.getHealth() - 1);
            }
        }
    }

}
