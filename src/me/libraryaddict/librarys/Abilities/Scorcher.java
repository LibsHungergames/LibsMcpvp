package me.libraryaddict.librarys.Abilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Scorcher extends AbilityListener {
    public String bootsName = "Scorchers Boots";
    public String blazeName = "Scorchers Igniter";

    @EventHandler
    public void onSecond(PlayerMoveEvent event) {
        if (HungergamesApi.getHungergames().currentTime <= HungergamesApi.getConfigManager().getInvincibilityTime())
            return;
        Player p = event.getPlayer();
        if (HungergamesApi.getPlayerManager().getGamer(p).isAlive()) {
            ItemStack boots = p.getInventory().getBoots();
            if (isSpecialItem(boots, bootsName) && isSpecialItem(p.getItemInHand(), blazeName)) {
                final Block b = event.getFrom().getBlock();
                Material type = b.getType();
                if ((type == Material.AIR || type == Material.SNOW || type == Material.LONG_GRASS)
                        && b.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                    if (p.hasMetadata("Scorching")
                            && p.getMetadata("Scorching").get(0).asInt() != HungergamesApi.getHungergames().currentTime) {
                        boots.setDurability((short) (boots.getDurability() + 1));
                        if (boots.getDurability() >= boots.getType().getMaxDurability())
                            p.getInventory().setBoots(new ItemStack(0));
                    }
                    p.setMetadata("Scorching",
                            new FixedMetadataValue(HungergamesApi.getHungergames(), HungergamesApi.getHungergames().currentTime));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(HungergamesApi.getHungergames(), new Runnable() {
                        public void run() {
                            b.setType(Material.FIRE);
                        }
                    }, 10);
                }
            }
        }
    }

}
