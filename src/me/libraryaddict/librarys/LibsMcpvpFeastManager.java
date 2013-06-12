package me.libraryaddict.librarys;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import me.libraryaddict.Hungergames.Interfaces.ChestManager;
import me.libraryaddict.Hungergames.Managers.LibsFeastManager;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class LibsMcpvpFeastManager extends LibsFeastManager {
    public void generateChests(Location loc, int height) {
        height--;
        ChestManager cm = HungergamesApi.getChestManager();
        for (int x = -height; x <= height; x++) {
            for (int z = -height; z <= height; z++) {
                Block block = loc.clone().add(x, 0, z).getBlock();
                Block b = block;
                if (x == 0 && z == 0)
                    setBlockFast(b, Material.ENCHANTMENT_TABLE.getId(), (short) 0);
                else if (Math.abs(x + z) % 2 == 0) {
                    setBlockFast(block, Material.CHEST.getId(), (byte) 0);
                    Chest chest = (Chest) block.getState();
                    cm.fillChest(chest.getInventory());
                    chest.update();
                }
            }
        }
    }

    private boolean setBlockFast(Block b, int typeId, short s) {
        try {
            if (b.getTypeId() != typeId || b.getData() != s)
                return b.setTypeIdAndData(typeId, (byte) s, false);
            // return ((CraftChunk) b.getChunk()).getHandle().a(b.getX() & 15,
            // b.getY(), b.getZ() & 15, typeId, data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
