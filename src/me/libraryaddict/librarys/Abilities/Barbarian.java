package me.libraryaddict.librarys.Abilities;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Barbarian extends AbilityListener {
    private HashMap<ItemStack, Integer> kills = new HashMap<ItemStack, Integer>();
    public int killsPerLevel = 3;
    public String swordName = "Bloody Bane";
    public String[] weaponTypeUpgrades = new String[] { "STONE_SWORD", "IRON_SWORD", "DIAMOND_SWORD" };

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        if (event.getKillerPlayer() != null) {
            ItemStack item = event.getKillerPlayer().getPlayer().getItemInHand();
            if (isSpecialItem(item, swordName) && hasAbility(event.getKillerPlayer().getPlayer())) {
                if (!kills.containsKey(item))
                    kills.put(item, 0);
                kills.put(item, kills.get(item) + 1);
                if (kills.get(item) % killsPerLevel == 0) {
                    int level = (kills.get(item) / killsPerLevel) - 1;
                    if (level < weaponTypeUpgrades.length)
                        item.setType(Material.valueOf(weaponTypeUpgrades[level]));
                }
            }
        }
    }
}
