package me.libraryaddict.librarys.Abilities;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class Lumberjack extends AbilityListener implements Disableable {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if ((event.getBlock().getType() == Material.LOG || event.getBlock().getType() == Material.LOG_2)
                && hasAbility(event.getPlayer())) {
            Block b = event.getBlock().getRelative(BlockFace.UP);
            while (b.getType() == Material.LOG || b.getType() == Material.LOG_2) {
                b.breakNaturally();
                b = b.getRelative(BlockFace.UP);
            }
        }
    }

}
