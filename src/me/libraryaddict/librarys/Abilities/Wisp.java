package me.libraryaddict.librarys.Abilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_5_R3.EntityHuman;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.EntityVillager;
import net.minecraft.server.v1_5_R3.PathfinderGoal;
import net.minecraft.server.v1_5_R3.PathfinderGoalAvoidPlayer;
import net.minecraft.server.v1_5_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_5_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_5_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_5_R3.PathfinderGoalSelector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.Hungergames.Hungergames;
import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Events.TimeSecondEvent;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;

public class Wisp extends AbilityListener {

    public String wispItemName = "Will of the wisp";
    public int wispsToSpawn = 5;
    public String notRealOne = ChatColor.RED + "Guess that wasn't the real one :$";
    public String aWispPopped = ChatColor.RED + "One of your wisps popped!";
    public String allWispsPopped = ChatColor.RED + "All your wisps popped!";
    public boolean removeFakeOnesWhenRealFound = true;
    public int fakeOnesLastForHowManySeconds = 30;
    private Hungergames hg = HungergamesApi.getHungergames();
    private ArrayList<WillOfWisp> wisps = new ArrayList<WillOfWisp>();

    // private HashMap<Player, ArrayList<Villager>> disguises = new HashMap<Player, ArrayList<Villager>>();

    class WillOfWisp {
        Player caster;
        ArrayList<Villager> villagers = new ArrayList<Villager>();
        int cast;
    }

    public Wisp() throws Exception {
        if (Bukkit.getPluginManager().getPlugin("LibsDisguises") == null)
            throw new Exception(String.format(HungergamesApi.getTranslationManager().getLoggerDependencyNotFound(),
                    "Plugin LibsDisguises"));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().name().contains("RIGHT") && isSpecialItem(event.getItem(), wispItemName)) {
            Player p = event.getPlayer();
            ItemStack item = event.getItem();
            item.setAmount(item.getAmount() - 1);
            if (item.getAmount() == 0)
                p.setItemInHand(new ItemStack(0));
            WillOfWisp wisp = new WillOfWisp();
            wisp.cast = hg.currentTime + this.fakeOnesLastForHowManySeconds;
            wisp.caster = p;
            p.getWorld().playSound(p.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
            for (int i = 0; i < wispsToSpawn; i++) {
                Villager villager = spawnVillager(p.getLocation());
                wisp.villagers.add(villager);
                PlayerDisguise player = new PlayerDisguise(p.getName());
                DisguiseAPI.disguiseToAll(villager, player);
                villager.setRemoveWhenFarAway(true);
                villager.getEquipment().setArmorContents(p.getInventory().getArmorContents());
                if (p.getItemInHand() != null) {
                    villager.getEquipment().setItemInHand(p.getItemInHand());
                    villager.getEquipment().setItemInHandDropChance(0F);
                }
                villager.getEquipment().setBootsDropChance(0F);
                villager.getEquipment().setLeggingsDropChance(0F);
                villager.getEquipment().setChestplateDropChance(0F);
                villager.getEquipment().setHelmetDropChance(0F);
            }
            wisps.add(wisp);
        }
    }

    public static Villager spawnVillager(Location loc) {
        Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        try {
            EntityVillager ent = ((CraftVillager) villager).getHandle();

            Field goalField = EntityLiving.class.getDeclaredField("goalSelector");
            goalField.setAccessible(true);
            PathfinderGoalSelector goalSelector = (PathfinderGoalSelector) goalField.get(ent);

            Field targetField = EntityLiving.class.getDeclaredField("targetSelector");
            targetField.setAccessible(true);

            Field aField = PathfinderGoalSelector.class.getDeclaredField("a");
            aField.setAccessible(true);
            ((List<PathfinderGoal>) aField.get(goalSelector)).clear();
            ((List<PathfinderGoal>) aField.get((PathfinderGoalSelector) targetField.get(ent))).clear();

            goalSelector.a(1, new PathfinderGoalAvoidPlayer(ent, EntityHuman.class, 6.0F, 0.23F, 0.4F));
            goalSelector.a(2, new PathfinderGoalAvoidPlayer(ent, EntityVillager.class, 6.0F, 0.23F, 0.4F));
            goalSelector.a(3, new PathfinderGoalRandomStroll(ent, 0.2F));
            goalSelector.a(4, new PathfinderGoalLookAtPlayer(ent, EntityHuman.class, 6.0F));
            goalSelector.a(5, new PathfinderGoalRandomLookaround(ent));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return villager;
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        Iterator<WillOfWisp> itel = wisps.iterator();
        while (itel.hasNext()) {
            WillOfWisp wisp = itel.next();
            if (wisp.caster == event.getKilled().getPlayer()) {
                for (Villager villager : wisp.villagers)
                    popWisp(villager);
                itel.remove();
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        for (WillOfWisp wisp : wisps)
            if (wisp.villagers.contains(event.getRightClicked()))
                event.setCancelled(true);
    }

    private void popWisp(Villager villager) {
        villager.getWorld().playEffect(villager.getLocation(), Effect.SMOKE, 9);
        villager.getWorld().playSound(villager.getLocation(), Sound.LAVA_POP, 1, 1);
        villager.remove();
        DisguiseAPI.undisguiseToAll(villager);
    }

    @EventHandler
    public void onSecond(TimeSecondEvent event) {
        Iterator<WillOfWisp> itel = wisps.iterator();
        while (itel.hasNext()) {
            WillOfWisp wisp = itel.next();
            if (wisp.cast < hg.currentTime) {
                for (Villager villager : wisp.villagers)
                    popWisp(villager);
                itel.remove();
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageEvent event) {
        Iterator<WillOfWisp> itel = wisps.iterator();
        while (itel.hasNext()) {
            WillOfWisp wisp = itel.next();
            if (wisp.caster == event.getEntity()) {
                Player p = (Player) event.getEntity();
                p.sendMessage(allWispsPopped);
                for (Villager villager : wisp.villagers)
                    popWisp(villager);
                itel.remove();
                break;
            } else if (wisp.villagers.contains(event.getEntity())) {
                event.setCancelled(true);
                popWisp((Villager) event.getEntity());
                wisp.villagers.remove(event.getEntity());
                if (wisp.villagers.size() == 0) {
                    wisp.caster.sendMessage(allWispsPopped);
                    itel.remove();
                } else
                    wisp.caster.sendMessage(aWispPopped);
                if (event instanceof EntityDamageByEntityEvent) {
                    Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                    if (damager instanceof Player)
                        ((Player) damager).sendMessage(notRealOne);
                }
                break;
            }
        }
    }
}
