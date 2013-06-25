package me.libraryaddict.librarys.Abilities;

import java.util.Iterator;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Fletcher extends AbilityListener implements Disableable {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (hasAbility(event.getPlayer()) && event.getBlock().getType() == Material.GRAVEL) {
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld()
                    .dropItemNaturally(event.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.FLINT));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null && hasAbility(event.getEntity().getKiller())) {
            Material mat = null;
            if (event.getEntityType() == EntityType.SKELETON)
                mat = Material.ARROW;
            else if (event.getEntityType() == EntityType.CHICKEN)
                mat = Material.FEATHER;
            else
                return;
            Iterator<ItemStack> itel = event.getDrops().iterator();
            while (itel.hasNext()) {
                ItemStack item = itel.next();
                if (item == null || item.getType() != mat)
                    continue;
                itel.remove();
            }
            event.getDrops().add(new ItemStack(mat, 2));
        }
    }
}
