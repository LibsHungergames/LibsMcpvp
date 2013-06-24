package me.libraryaddict.librarys.Abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Tank extends AbilityListener implements Disableable {
    public float explosionSize = 2F;

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION)
            if (event.getEntity() instanceof Player && hasAbility((Player) event.getEntity())) {
                event.setCancelled(true);
            }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        if (event.getKillerPlayer() != null && hasAbility(event.getKillerPlayer().getPlayer())) {
            event.getKilled().getPlayer().getWorld().createExplosion(event.getKilled().getPlayer().getLocation(), explosionSize);
        }
    }
}
