package me.libraryaddict.librarys.Abilities;

import me.libraryaddict.Hungergames.Events.GameStartEvent;
import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.librarys.nms.NMS;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Analyzer extends AbilityListener implements Disableable {
    public boolean alwaysDisplayInfo = false;
    public boolean countSoup = false;
    private HashMap<Player, String> currentNames = new HashMap<Player, String>();
    public String informationItem = ChatColor.WHITE + "Itemstack " + ChatColor.YELLOW + "%s" + ChatColor.WHITE + " has "
            + ChatColor.YELLOW + "%s" + ChatColor.WHITE + " items";
    public String informationMob = ChatColor.WHITE + "Mob " + ChatColor.YELLOW + "%s" + ChatColor.WHITE + " has "
            + ChatColor.YELLOW + "%s" + ChatColor.WHITE + " health";
    public String informationPlayer = ChatColor.YELLOW + "%s" + ChatColor.WHITE + " has Health: " + ChatColor.YELLOW + "%s"
            + ChatColor.WHITE + ", Hunger:" + ChatColor.YELLOW + " %s" + ChatColor.WHITE + ", Armor: " + ChatColor.YELLOW + "%s"
            + ChatColor.WHITE + ", Kit: " + ChatColor.YELLOW + "%s";
    public String informationPlayerSoup = ChatColor.YELLOW + "%s" + ChatColor.WHITE + " has Health: " + ChatColor.YELLOW + "%s"
            + ChatColor.WHITE + ", Hunger:" + ChatColor.YELLOW + " %s" + ChatColor.WHITE + ", Armor: " + ChatColor.YELLOW + "%s"
            + ChatColor.WHITE + ", Kit: " + ChatColor.YELLOW + "%s" + ChatColor.WHITE + ", Soups: " + ChatColor.YELLOW + "%s";
    public int rangeToScan = 100;
    private int scheduler;
    public int ticksPerEntityCheck = 10;

    public Analyzer() throws Exception {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null)
            throw new Exception(String.format(HungergamesApi.getConfigManager().getLoggerConfig().getDependencyNotFound(),
                    "ProtocolLib"));
    }

    @EventHandler
    public void gameStartEvent(GameStartEvent event) {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), getRunnable(),
                ticksPerEntityCheck, ticksPerEntityCheck);
    }

    private int getArmorRating(ItemStack[] items) {
        int i = 0;
        for (ItemStack item : items)
            if (item != null)
                i += getArmorValue(item);
        return i;
    }

    private int getArmorValue(ItemStack armor) {
        Material mat = armor.getType();
        if (mat == Material.LEATHER_HELMET || mat == Material.LEATHER_BOOTS || mat == Material.GOLD_BOOTS
                || mat == Material.CHAINMAIL_BOOTS)
            return 1;
        if (mat == Material.LEATHER_LEGGINGS || mat == Material.GOLD_HELMET || mat == Material.CHAINMAIL_HELMET
                || mat == Material.IRON_HELMET || mat == Material.IRON_BOOTS)
            return 2;
        if (mat == Material.LEATHER_CHESTPLATE || mat == Material.GOLD_LEGGINGS || mat == Material.DIAMOND_BOOTS
                || mat == Material.DIAMOND_HELMET)
            return 3;
        if (mat == Material.CHAINMAIL_LEGGINGS)
            return 4;
        if (mat == Material.GOLD_CHESTPLATE || mat == Material.CHAINMAIL_CHESTPLATE || mat == Material.IRON_LEGGINGS)
            return 5;
        if (mat == Material.IRON_LEGGINGS || mat == Material.DIAMOND_LEGGINGS)
            return 6;
        if (mat == Material.DIAMOND_CHESTPLATE)
            return 8;
        return 0;
    }


    private String getInformation(Entity entity) {
        if (entity == null)
            return null;
        if (entity instanceof Player) {
            Player p = (Player) entity;
            if (countSoup) {
                int i = 0;
                for (ItemStack item : p.getInventory())
                    if (item != null && item.getType() == Material.MUSHROOM_SOUP)
                        i++;
                return String.format(informationPlayerSoup, p.getName(), p.getHealth(), p.getFoodLevel(), getArmorRating(p
                        .getInventory().getArmorContents()), HungergamesApi.getKitManager().getKitByPlayer(p).getName(), i);
            }
            return String.format(informationPlayer, p.getName(), p.getHealth(), p.getFoodLevel(), getArmorRating(p.getInventory()
                    .getArmorContents()), HungergamesApi.getKitManager().getKitByPlayer(p).getName());
        } else if (entity instanceof Item) {
            Item item = (Item) entity;
            return String.format(informationItem, HungergamesApi.getNameManager().getItemName(item.getItemStack()), item
                    .getItemStack().getAmount());
        } else if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            return String.format(informationMob, HungergamesApi.getNameManager().getName(living.getType().name()),
                    living.getHealth());
        }
        return null;
    }

    private Runnable getRunnable() {
        return new Runnable() {
            boolean zero;

            public void run() {
                zero = !zero;
                for (Player p : getMyPlayers()) {
                    ItemStack item = p.getItemInHand();
                    if (item == null || item.getType() == Material.AIR)
                        continue;
                    Entity entity = NMS.getEntityInSight(p, rangeToScan);
                    String info = getInformation(entity);
                    if (alwaysDisplayInfo) {
                        if (info != null)
                            info += ChatColor.COLOR_CHAR + (zero ? "a" : "b");
                    } else {
                        if (currentNames.containsKey(p)) {
                            if (info == null)
                                currentNames.remove(p);
                            else if (currentNames.get(p).equals(info)) {
                                continue;
                            }
                        } else if (info != null)
                            currentNames.put(p, info);
                        else
                            continue;
                    }
                    ItemMeta meta = item.getItemMeta();
                    if (info != null)
                        meta.setDisplayName(info);
                    item = item.clone();
                    item.setItemMeta(meta);
                    NMS.sendFakeItem(p, 44 - Math.abs(p.getInventory().getHeldItemSlot() - 8), item);
                }
            }
        };
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        currentNames.remove(event.getKilled().getPlayer());
    }

    public void registerPlayer(Player player) {
        super.registerPlayer(player);
        if (scheduler < 0 && HungergamesApi.getHungergames().currentTime >= 0) {
            scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), getRunnable(),
                    ticksPerEntityCheck, ticksPerEntityCheck);
        }
    }

    public void unregisterPlayer(Player player) {
        super.unregisterPlayer(player);
        if (HungergamesApi.getHungergames().currentTime >= 0 && this instanceof Disableable && getMyPlayers().size() == 0)
            Bukkit.getScheduler().cancelTask(scheduler = -0);
    }
}
