package me.libraryaddict.librarys.Abilities;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Rider extends AbilityListener implements Disableable {
    private HashMap<Player, Horse> horses = new HashMap<Player, Horse>();
    public String saddleName = "Summon: Mighty Steed";
    public boolean nameHorse = true;
    public String horseName = "%s's mighty steed";
    public boolean preventOthersFromUsing = true;
    public boolean modifyHorseStats = true;
    public double jumpStrength = 1;
    public double runSpeed = 1;
    public int horseHealth = 50;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().name().contains("RIGHT") && isSpecialItem(event.getItem(), saddleName)) {
            Player p = event.getPlayer();
            if (hasAbility(p)) {
                if (horses.containsKey(p)) {
                    Horse horse = horses.remove(p);
                    if (!horse.isDead()) {
                        horse.eject();
                        horse.leaveVehicle();
                        horse.remove();
                    }
                } else {
                    Horse horse = (Horse) p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);
                    horses.put(p, horse);
                    if (nameHorse) {
                        horse.setCustomName(String.format(horseName, p.getName()));
                        horse.setCustomNameVisible(true);
                    }
                    horse.setBreed(false);
                    horse.setTamed(true);
                    horse.setDomestication(horse.getMaxDomestication());
                    horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                    // if (preventOthersFromUsing)
                    horse.setOwner(p);
                    if (modifyHorseStats) {
                        horse.setJumpStrength(jumpStrength);
                        horse.setMaxHealth(horseHealth);
                        horse.setHealth(horseHealth);
                        horse.setColor(Color.WHITE);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (horses.containsValue(event.getEntity())) {
            event.setDroppedExp(0);
            event.getDrops().clear();
            Iterator<Player> itel = horses.keySet().iterator();
            while (itel.hasNext()) {
                if (horses.get(itel.next()) == event.getEntity()) {
                    itel.remove();
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (preventOthersFromUsing) {
            if (horses.containsValue(event.getRightClicked())) {
                for (Player p : horses.keySet()) {
                    if (horses.get(p) == event.getRightClicked()) {
                        if (event.getPlayer() != p) {
                            event.setCancelled(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.SADDLE) {
            if (event.getWhoClicked().getVehicle() != null) {
                if (horses.containsValue(event.getWhoClicked().getVehicle())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        if (horses.containsKey(event.getKilled().getPlayer())) {
            Horse horse = horses.remove(event.getKilled().getPlayer());
            if (!horse.isDead()) {
                horse.eject();
                horse.leaveVehicle();
                horse.remove();
            }
        }
    }
}
