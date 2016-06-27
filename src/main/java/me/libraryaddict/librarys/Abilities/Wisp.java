package me.libraryaddict.librarys.Abilities;

import java.util.ArrayList;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;

public class Wisp extends AbilityListener implements Disableable {

    class WillOfWisp {
        Player caster;
        ArrayList<Ocelot> villagers = new ArrayList<Ocelot>();
    }

    public static Ocelot spawnVillager(Location loc) {
        Ocelot villager = (Ocelot) loc.getWorld().spawnEntity(loc, EntityType.OCELOT);
        villager.setNoDamageTicks(Integer.MAX_VALUE);
        villager.setBreed(false);
        return villager;
    }

    public String allWispsPopped = ChatColor.RED + "All your wisps popped!";
    public String aWispPopped = ChatColor.RED + "One of your wisps popped!";
    public int fakeOnesLastForHowManySeconds = 30;
    public String notRealOne = ChatColor.RED + "Guess that wasn't the real one :$";
    public boolean removeFakeOnesWhenRealFound = true;
    public String wispItemName = "Will of the wisp";

    // private HashMap<Player, ArrayList<Villager>> disguises = new HashMap<Player, ArrayList<Villager>>();

    private ArrayList<WillOfWisp> wisps = new ArrayList<WillOfWisp>();

    public int wispsToSpawn = 5;

    public Wisp() throws Exception {
        if (Bukkit.getPluginManager().getPlugin("LibsDisguises") == null)
            throw new Exception(String.format(HungergamesApi.getConfigManager().getLoggerConfig().getDependencyNotFound(),
                    "Plugin LibsDisguises"));
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null)
            throw new Exception(String.format(HungergamesApi.getConfigManager().getLoggerConfig().getDependencyNotFound(),
                    "Plugin ProtocolLib"));
    }

    @EventHandler
    public void onAttack(EntityDamageEvent event) {
        if (event.isCancelled())
            return;
        Iterator<WillOfWisp> itel = wisps.iterator();
        while (itel.hasNext()) {
            WillOfWisp wisp = itel.next();
            if (wisp.caster == event.getEntity()) {
                Player p = (Player) event.getEntity();
                p.sendMessage(allWispsPopped);
                for (Ocelot villager : wisp.villagers)
                    popWisp(villager);
                itel.remove();
                break;
            } else if (wisp.villagers.contains(event.getEntity())) {
                event.setCancelled(true);
                popWisp((Ocelot) event.getEntity());
                wisp.villagers.remove(event.getEntity());
                if (wisp.villagers.size() == 0) {
                    wisp.caster.sendMessage(allWispsPopped);
                    itel.remove();
                } else
                    wisp.caster.sendMessage(aWispPopped);
                if (event instanceof EntityDamageByEntityEvent) {
                    Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                    if (damager instanceof Player && wisp.caster != damager)
                        ((Player) damager).sendMessage(notRealOne);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().name().contains("RIGHT") && isSpecialItem(event.getItem(), wispItemName)) {
            Player p = event.getPlayer();
            ItemStack item = event.getItem();
            item.setAmount(item.getAmount() - 1);
            if (item.getAmount() == 0)
                p.setItemInHand(new ItemStack(0));
            final WillOfWisp wisp = new WillOfWisp();
            wisp.caster = p;
            p.getWorld().playSound(p.getLocation(), Sound.BAT_TAKEOFF, 1, 0);
            for (int i = 0; i < wispsToSpawn; i++) {
                Ocelot villager = spawnVillager(p.getLocation());
                wisp.villagers.add(villager);
                PlayerDisguise player = new PlayerDisguise(p.getName());
                player.getWatcher().setItemInHand(p.getItemInHand());
                player.getWatcher().setArmor(p.getInventory().getArmorContents());
                DisguiseAPI.disguiseToAll(villager, player);
                villager.setRemoveWhenFarAway(true);
            }
            wisps.add(wisp);
            Bukkit.getScheduler().scheduleSyncDelayedTask(HungergamesApi.getHungergames(), new Runnable() {
                public void run() {
                    for (Ocelot villager : wisp.villagers)
                        popWisp(villager);
                    wisps.remove(wisp);
                }
            }, fakeOnesLastForHowManySeconds * 20);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        for (WillOfWisp wisp : wisps)
            if (wisp.villagers.contains(event.getRightClicked()))
                event.setCancelled(true);
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        Iterator<WillOfWisp> itel = wisps.iterator();
        while (itel.hasNext()) {
            WillOfWisp wisp = itel.next();
            if (wisp.caster == event.getKilled().getPlayer()) {
                for (Ocelot villager : wisp.villagers)
                    popWisp(villager);
                itel.remove();
            }
        }
    }

    private void popWisp(Ocelot villager) {
        Location loc = villager.getLocation().clone();
        loc.getWorld().playSound(loc, Sound.FIZZ, 2, 0);
        for (int i = 0; i <= 9; i++)
            loc.getWorld().playEffect(loc, Effect.SMOKE, i);
        villager.remove();
        DisguiseAPI.undisguiseToAll(villager);
    }
}
