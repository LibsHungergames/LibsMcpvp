package me.libraryaddict.librarys.Abilities;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Worm extends AbilityListener implements Disableable {

    @EventHandler
    public void onDamage(BlockDamageEvent event) {
        if (hasAbility(event.getPlayer()) && event.getBlock().getType() == Material.DIRT) {
            Player p = event.getPlayer();
            boolean drop = true;
            if (p.getHealth() < 20) {
                p.setHealth(p.getHealth() + 1);
                drop = false;
            } else if (p.getFoodLevel() < 20) {
                p.setFoodLevel(p.getFoodLevel() + 1);
                drop = false;
            }
            event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, Material.DIRT.getId());
            event.getBlock().setType(Material.AIR);
            if (drop)
                event.getBlock().getWorld()
                        .dropItemNaturally(event.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.DIRT));
        }
    }

}
