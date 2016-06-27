package me.libraryaddict.librarys.Abilities;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Worm extends AbilityListener implements Disableable {
    public boolean addNoCheatPlusBypass = true;
    public boolean dirtPreventsFallDamage = true;
    public boolean canInstantSmashDirt = true;
    public boolean gainsHealthFromSmashingDirt = true;
    public boolean gainsHungerFromSmashingDirt = true;

    private boolean onSmash(Player p, Block block) {
        double dist = block.getLocation().distance(p.getWorld().getSpawnLocation());
        double borderSize = HungergamesApi.getConfigManager().getMainConfig().getBorderSize();
        if (!HungergamesApi.getConfigManager().getMainConfig().isRoundedBorder()) {
            double i = Math.abs(block.getX() - p.getWorld().getSpawnLocation().getBlockX());
            if (i >= borderSize)
                dist = i;
            i = Math.abs(block.getZ() - p.getWorld().getSpawnLocation().getBlockZ());
            if (i >= borderSize)
                dist += i;
        }
        if (dist < borderSize) {
            if (p.getHealth() < p.getMaxHealth() && gainsHealthFromSmashingDirt) {
                double hp = p.getHealth() + 1;
                if (hp > p.getMaxHealth())
                    hp = p.getMaxHealth();
                p.setHealth(hp);
            } else if (p.getFoodLevel() < 20 && gainsHungerFromSmashingDirt) {
                p.setFoodLevel(p.getFoodLevel() + 1);
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!this.canInstantSmashDirt && hasAbility(event.getPlayer()) && event.getBlock().getType() == Material.DIRT) {
            Player p = event.getPlayer();
            onSmash(p, event.getBlock());
        }
    }

    @EventHandler
    public void onDamage(BlockDamageEvent event) {
        if (this.canInstantSmashDirt && hasAbility(event.getPlayer()) && event.getBlock().getType() == Material.DIRT) {
            Player p = event.getPlayer();
            boolean drop = (this.gainsHealthFromSmashingDirt && p.getHealth() < p.getMaxHealth())
                    || (this.gainsHungerFromSmashingDirt && p.getFoodLevel() < 20);
            if (onSmash(p, event.getBlock())) {
                event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, Material.DIRT.getId());
                event.getBlock().setType(Material.AIR);
                if (!drop)
                    event.getBlock().getWorld()
                            .dropItemNaturally(event.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.DIRT));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (dirtPreventsFallDamage && event.getCause() == DamageCause.FALL && event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (hasAbility(p)) {
                Location loc = p.getLocation();
                boolean dirt = false;
                for (float x = -0.35F; x <= 0.35F && !dirt; x += 0.35F) {
                    for (float z = -0.35F; z <= 0.35F && !dirt; z += 0.35F) {
                        Block b = loc.clone().add(x, -1, z).getBlock();
                        if (b.getType() == Material.DIRT)
                            dirt = true;
                    }
                }
                if (dirt) {
                    event.setCancelled(true);
                    if (this.addNoCheatPlusBypass)
                        p.addAttachment(HungergamesApi.getHungergames(), "nocheatplus.checks.moving.nofall", true, 100);
                }
            }
        }
    }
}
