package me.libraryaddict.librarys.Abilities;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class CookieMonster extends AbilityListener implements Disableable {
    public int oneChanceInWhatOfCookies = 4;

    @EventHandler
    public void onChomp(PlayerInteractEvent event) {
        if (event.getAction().name().contains("RIGHT")) {
            Player p = event.getPlayer();
            if (hasAbility(p) && event.getItem() != null && event.getItem().getType() == Material.COOKIE) {
                event.setCancelled(true);
                if (p.getHealth() < 20) {
                    p.setHealth(p.getHealth() + 1);
                } else if (p.getFoodLevel() < 20) {
                    p.setFoodLevel(p.getFoodLevel() + 1);
                } else {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1), true);
                }
                event.getItem().setAmount(event.getItem().getAmount() - 1);
                if (event.getItem().getAmount() == 0)
                    p.setItemInHand(new ItemStack(0));
            }
        }
    }

    @EventHandler
    public void onDamage(BlockDamageEvent event) {
        if (hasAbility(event.getPlayer())) {
            if (event.getBlock().getType() == Material.LONG_GRASS && new Random().nextInt(oneChanceInWhatOfCookies) == 0) {
                Location loc = event.getBlock().getLocation().clone();
                loc.getWorld().dropItemNaturally(loc.add(0.5, 0, 0.5), new ItemStack(Material.COOKIE));
            }
        }
    }
}
