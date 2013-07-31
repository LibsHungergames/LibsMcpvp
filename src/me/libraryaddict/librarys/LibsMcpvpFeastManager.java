package me.libraryaddict.librarys;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.Hungergames.Interfaces.ChestManager;
import me.libraryaddict.Hungergames.Managers.LibsFeastManager;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class LibsMcpvpFeastManager extends LibsFeastManager {
    private BukkitRunnable chestGenerator;
    private BukkitRunnable runnable;
    private LinkedList<Block> processedBlocks = new LinkedList<Block>();

    public void generateChests(final Location loc, int height) {
        final int h = height - 1;
        chestGenerator = new BukkitRunnable() {
            public void run() {
                ChestManager cm = HungergamesApi.getChestManager();
                for (int x = -h; x <= h; x++) {
                    for (int z = -h; z <= h; z++) {
                        Block block = loc.clone().add(x, 0, z).getBlock();
                        Block b = block;
                        if (x == 0 && z == 0)
                            setBlockFast(b, Material.ENCHANTMENT_TABLE.getId(), (short) 0);
                        else if (Math.abs(x + z) % 2 == 0) {
                            block.setTypeIdAndData(Material.CHEST.getId(), (byte) 0, false);
                            processedBlocks.add(block);
                            Chest chest = (Chest) block.getState();
                            cm.fillChest(chest.getInventory());
                            chest.update();
                        }
                    }
                }
            }
        };
        if (runnable == null) {
            chestGenerator.runTask(HungergamesApi.getHungergames());
            chestGenerator = null;
        }
    }
}
