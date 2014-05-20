package me.libraryaddict.librarys.Abilities;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Barbarian extends AbilityListener {
    public int killsPerLevel = 3;
    public String swordName = "Bloody Bane";
    private Material[] updates = new Material[] { Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD };
    public String swordLore = "%Kills% kills";

    public Barbarian() {
        if (!swordLore.contains("%Kills%")) {
            swordLore = "%Kills% kills";
        }
    }

    @EventHandler
    public void onKilled(PlayerKilledEvent event) {
        if (event.getKillerPlayer() != null) {
            ItemStack item = event.getKillerPlayer().getPlayer().getItemInHand();
            if (isSpecialItem(item, swordName) && hasAbility(event.getKillerPlayer().getPlayer())) {
                ItemMeta meta = item.getItemMeta();
                int kills = 0;
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore.size() > 0) {
                        String str = lore.get(0);
                        str = str.substring(swordLore.indexOf("%Kills%"));
                        str = str.substring(0, str.length() - (swordLore.length() - swordLore.lastIndexOf("%Kills%")));
                        kills = Integer.parseInt(str);
                    }
                }
                kills++;
                meta.setLore(Arrays.asList(swordLore.replace("%Kills%", "" + kills)));
                item.setItemMeta(meta);
                if (kills % killsPerLevel == 0) {
                    int level = kills / killsPerLevel;
                    if (level < updates.length)
                        item.setType(updates[level]);
                }
            }
        }
    }
}
