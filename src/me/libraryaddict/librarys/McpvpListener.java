package me.libraryaddict.librarys;

import me.libraryaddict.Hungergames.Hungergames;
import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Managers.KitManager;
import me.libraryaddict.Hungergames.Types.Gamer;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class McpvpListener implements Listener {
    private Hungergames hg = HungergamesApi.getHungergames();
    private int respawnUntil;
    // private int joinUtil;
    private McPvP mcpvp;
    private boolean respawnItems;
    private String chocolateMilkName;
    private String cactusJuiceName;
    private boolean chocolateMilk;
    private boolean cactusJuice;

    public McpvpListener(McPvP mcpvp) {
        this.mcpvp = mcpvp;
        FileConfiguration config = mcpvp.getConfig();
        this.respawnUntil = config.getInt("RespawnDuration");
        this.respawnItems = config.getBoolean("RespawnItems");
        cactusJuice = config.getBoolean("CactusJuice");
        chocolateMilk = config.getBoolean("ChocolateMilk");
        chocolateMilkName = ChatColor.translateAlternateColorCodes('&', config.getString("ChocolateMilkName"));
        cactusJuiceName = ChatColor.translateAlternateColorCodes('&', config.getString("CactusJuiceName"));
        if (cactusJuice) {
            ItemStack item = new ItemStack(Material.MUSHROOM_SOUP);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(cactusJuiceName);
            item.setItemMeta(meta);
            ShapelessRecipe recipe = new ShapelessRecipe(item);
            recipe.addIngredient(Material.BOWL);
            recipe.addIngredient(1, Material.CACTUS, 0);
            recipe.addIngredient(1, Material.CACTUS, 0);
            Bukkit.addRecipe(recipe);
        }
        if (chocolateMilk) {
            ItemStack item = new ItemStack(Material.MUSHROOM_SOUP);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(chocolateMilkName);
            item.setItemMeta(meta);
            ShapelessRecipe recipe = new ShapelessRecipe(item);
            recipe.addIngredient(Material.BOWL);
            recipe.addIngredient(1, Material.INK_SACK, 3);
            Bukkit.addRecipe(recipe);
        }
        // this.joinUtil = mcpvp.getConfig().getInt("JoinDuration");

    }

    /*@EventHandler(priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event) {
        if (hg.currentTime >= 0 && hg.currentTime < joinUtil && event.getPlayer().hasPermission("hungergames.vip.rejoin")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(mcpvp, new Runnable() {
                public void run() {
                    Gamer gamer = HungergamesApi.getPlayerManager().getGamer(event.getPlayer());
                    if (gamer != null)
                        gamer.setAlive(true);
                }
            }, 2);
        }
    }*/

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.MUSHROOM_SOUP && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName();
            int restores = 0;
            if (chocolateMilk && name.equals(chocolateMilkName))
                restores = mcpvp.getConfig().getInt("ChocolateMilkRestores");
            else if (cactusJuice && name.equals(cactusJuiceName))
                restores = mcpvp.getConfig().getInt("CactusJuiceRestores");
            if (restores != 0) {
                if (p.getHealth() < 20 || p.getFoodLevel() < 19) {
                    event.setCancelled(true);
                    if (p.getHealth() < 20)
                        if (p.getHealth() + restores <= 20)
                            p.setHealth(p.getHealth() + restores);
                        else
                            p.setHealth(20);
                    else if (p.getFoodLevel() < 20)
                        if (p.getFoodLevel() + restores <= 20)
                            p.setFoodLevel(p.getFoodLevel() + restores);
                        else
                            p.setFoodLevel(20);
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                        HungergamesApi.getKitManager().addItem(p, new ItemStack(Material.BOWL));
                    } else
                        item = new ItemStack(Material.BOWL);
                    p.setItemInHand(item);
                }
            }
        }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        if (event.getKilled().getPlayer().hasPermission("hungergames.vip.respawn") && hg.currentTime < respawnUntil) {
            final String killedName = event.getKilled().getName();
            Bukkit.getScheduler().scheduleSyncDelayedTask(mcpvp, new Runnable() {
                public void run() {
                    Gamer gamer = HungergamesApi.getPlayerManager().getGamer(killedName);
                    if (gamer != null) {
                        HungergamesApi.getPlayerManager().sendToSpawn(gamer);
                        gamer.setAlive(true);
                        KitManager kits = HungergamesApi.getKitManager();
                        Player p = gamer.getPlayer();
                        p.getInventory().addItem(new ItemStack(Material.COMPASS));
                        kits.setKit(p, kits.getKitByPlayer(p).getName());
                        if (respawnItems)
                            kits.getKitByPlayer(p).giveKit(p);
                    }
                }
            });
        }
    }
}
