package me.libraryaddict.librarys.nms;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface FakeFurnace {
    void tick();

    void showTo(Player player);

    List<ItemStack> getItems();
}
