package me.libraryaddict.librarys.Abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Viking extends AbilityListener implements Disableable {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && hasAbility((Player) event.getDamager())) {
            ItemStack item = ((Player) event.getDamager()).getItemInHand();
            if (item != null && item.getType().name().contains("AXE")) {
                event.setDamage(event.getDamage() + 3);
            }
        }
    }
}
