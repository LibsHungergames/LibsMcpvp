package me.libraryaddict.librarys.Abilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Managers.KitManager;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Gambler extends AbilityListener implements Disableable {
    private class WonItem {
        private int chance;
        private boolean isHealth;
        private boolean isHunger;
        private String message;
        private Object wonObject;

        public WonItem(int newChance, String message) {
            this.message = message;
            chance = newChance;
        }

        public WonItem(int newChance, String message, Object wonObject) {
            this.wonObject = wonObject;
            this.message = message;
            chance = newChance;
        }

        public int getChance() {
            return chance;
        }

        public ItemStack[] getItemStack() {
            return (ItemStack[]) wonObject;
        }

        public String getMessage() {
            return message;
        }

        public PotionEffect getPotionEffect() {
            return (PotionEffect) wonObject;
        }

        public boolean isHealth() {
            return isHealth;
        }

        public boolean isHunger() {
            return isHunger;
        }

        public boolean isItemStack() {
            return wonObject instanceof ItemStack[];
        }

        public boolean isPotionEffect() {
            return wonObject instanceof PotionEffect;
        }

        public void setHealth(boolean setHealth) {
            isHealth = setHealth;
        }

        public void setHunger(boolean setHunger) {
            isHunger = setHunger;
        }
    }

    public int chanceHeal = 100;
    public int chanceHunger = 100;
    public String foodName = "&2Full hunger";
    public String healthName = "&2Full health";
    private transient ArrayList<WonItem> itemsToWin = new ArrayList<WonItem>();
    private transient KitManager kits = HungergamesApi.getKitManager();
    public String[] potionEffects = new String[] { "1000 &cInstant_Death HARM 2 1000", "1 &cSlowness_Effect SLOW 1200 1",
            "1 &2Speed_Effect SPEED 1200 1", "1 &cPoison_Effect POISON 200 0", "1 &cHunger_Effect HUNGER 1200 1",
            "1 &2Strength_Boost INCREASE_DAMAGE 1200 0", "1 &cWeakness_Effect WEAKNESS 1200 1",
            "1 &2Regeneration_Effect REGENERATION 1200 0" };
    public boolean potionEffectsDurationsStack = true;
    public String[] randomItems = new String[] { "250 &2Diamond_Helmet DIAMOND_HELMET 0 1",
            "250 &2Diamond_Chestplate DIAMOND_CHESTPLATE 0 1", "250 &2Diamond_Leggings DIAMOND_LEGGINGS 0 1",
            "250 &2Diamond_Boots DIAMOND_BOOTS 0 1", "250 &2Diamond_Sword DIAMOND_SWORD 0 1" };

    public String whatYouWon = ChatColor.BLUE + "You won: " + ChatColor.AQUA + "%s";

    public WonItem getRandom() {
        Collections.shuffle(itemsToWin, new Random());
        if (itemsToWin.size() == 0)
            return null;
        while (true) {
            Iterator<WonItem> itel = itemsToWin.iterator();
            while (itel.hasNext()) {
                WonItem item = itel.next();
                if (item.getChance() != 0)
                    if (new Random().nextInt(item.getChance()) == 0)
                        return item;
            }
        }
    }

    public boolean load(ConfigurationSection section, boolean isNewFile) {
        boolean returns = super.load(section, isNewFile);
        WonItem health = new WonItem(chanceHeal, healthName);
        health.setHealth(true);
        if (health.getChance() != 0)
            itemsToWin.add(health);
        WonItem hunger = new WonItem(chanceHunger, foodName);
        hunger.setHunger(true);
        if (hunger.getChance() != 0)
            itemsToWin.add(hunger);
        for (String string : potionEffects) {
            String[] split = string.split(" ");
            int chance = Integer.parseInt(split[0]);
            String name = ChatColor.translateAlternateColorCodes('&', split[1]);
            PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(split[2].toUpperCase()),
                    Integer.parseInt(split[3]), Integer.parseInt(split[4]));
            WonItem potionPrize = new WonItem(chance, name, potionEffect);
            if (potionPrize.getChance() != 0)
                itemsToWin.add(potionPrize);
        }
        for (String string : randomItems) {
            String[] split = string.split(" ");
            int chance = Integer.parseInt(split[0]);
            String name = ChatColor.translateAlternateColorCodes('&', split[1]);
            WonItem itemPrize = new WonItem(chance, name, kits.parseItem(string.substring(split[0].length() + split[1].length()
                    + 2)));
            if (itemPrize.getChance() != 0)
                itemsToWin.add(itemPrize);
        }
        return returns;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && hasAbility(event.getPlayer()) && !event.getPlayer().isSneaking()
                && event.getClickedBlock().getType() == Material.STONE_BUTTON && event.getClickedBlock().getData() <= (byte) 4
                && event.getClickedBlock().getData() > (byte) 0) {
            Player p = event.getPlayer();
            WonItem wonItem = getRandom();
            if (wonItem.isHealth())
                p.setHealth(p.getMaxHealth());
            else if (wonItem.isHunger()) {
                p.setFoodLevel(20);
                p.setSaturation(5.0F);
                p.setExhaustion(0F);
            } else if (wonItem.isItemStack()) {
                for (ItemStack item : wonItem.getItemStack())
                    kits.addItem(p, item);
                p.updateInventory();
            } else if (wonItem.isPotionEffect()) {
                PotionEffect effect = wonItem.getPotionEffect();
                if (potionEffectsDurationsStack)
                    for (PotionEffect e : p.getActivePotionEffects()) {
                        if (e.getType() == effect.getType()) {
                            effect = new PotionEffect(e.getType(), e.getDuration() + effect.getDuration(), effect.getAmplifier());
                        }
                    }
                p.addPotionEffect(effect, true);
            }
            p.sendMessage(String.format(whatYouWon, wonItem.getMessage()));
        }
    }
}
