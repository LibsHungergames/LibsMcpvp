package me.libraryaddict.librarys.Abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Spiderman extends AbilityListener implements Disableable {

    public int cooldown = 30;
    private HashMap<String, ArrayList<Long>> cooldownMap = new HashMap<String, ArrayList<Long>>();
    public String cooldownMessage = ChatColor.BLUE + "Your web shooters havn't refilled yet! Wait %s seconds!";
    public int maxBallsThrown = 3;

    private boolean isWeb(Location loc) {
        if (loc.getBlock().getType() == Material.WEB)
            return true;
        return loc.clone().add(0, 1, 0).getBlock().getType() == Material.WEB;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (hasAbility(event.getPlayer())) {
            if (isWeb(event.getFrom()) || isWeb(event.getTo())) {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1), true);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata("Spiderball")) {
            Location loc = event.getEntity().getLocation();
            int x = new Random().nextInt(2) - 1;
            int z = new Random().nextInt(2) - 1;
            for (int y = 0; y < 2; y++)
                for (int xx = 0; xx < 2; xx++)
                    for (int zz = 0; zz < 2; zz++) {
                        Block b = loc.clone().add(x + xx, y, z + zz).getBlock();
                        if (b.getType() == Material.AIR)
                            b.setType(Material.WEB);
                    }

            event.getEntity().remove();
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() == EntityType.SNOWBALL) {
            if (event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {
                Player p = (Player) event.getEntity().getShooter();
                if (!hasAbility(p))
                    return;
                ArrayList<Long> cooldowns;
                if (cooldownMap.containsKey(p.getName()))
                    cooldowns = cooldownMap.get(p.getName());
                else {
                    cooldowns = new ArrayList<Long>();
                    cooldownMap.put(p.getName(), cooldowns);
                }
                if (cooldowns.size() == maxBallsThrown) {
                    if (cooldowns.get(0) >= System.currentTimeMillis()) {
                        event.setCancelled(true);
                        p.updateInventory();
                        p.sendMessage(String.format(cooldownMessage,
                                (((cooldowns.get(0) - System.currentTimeMillis()) / 1000) + 1)));
                        HungergamesApi.getKitManager().addItem(p, new ItemStack(Material.SNOW_BALL));
                        return;
                    }
                    cooldowns.remove(0);
                }
                event.getEntity()
                        .setMetadata("Spiderball", new FixedMetadataValue(HungergamesApi.getHungergames(), "Spiderball"));
                cooldowns.add(System.currentTimeMillis() + (cooldown * 1000));
            }
        }
    }
}
