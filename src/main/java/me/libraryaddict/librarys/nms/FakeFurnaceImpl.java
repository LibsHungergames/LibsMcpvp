package me.libraryaddict.librarys.nms;

import lombok.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.RecipesFurnace;
import net.minecraft.server.v1_8_R3.TileEntityFurnace;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

/* package */ class FakeFurnaceImpl extends TileEntityFurnace implements FakeFurnace {
    private double burnSpeed;
    private int lastID;
    // To access the chests
    public int link;
    private double meltSpeed;
    /*
     * I'm internally using "myCookTime" to not lose any precision, but for
     * displaying the progress I still have to use "cookTime"
     */
    private double myCookTime;

    @SneakyThrows(IllegalAccessException.class) // Shouldn't happen
    public FakeFurnaceImpl() {
        link = 0;
        burnSpeed = 1.0D;
        meltSpeed = 1.0D;
        myCookTime = 0.0D;
        COOK_TIME_FIELD.setInt(this, 0);
        BURN_TIME_FIELD.setInt(this, 0);
        TICKS_FOR_CURRENT_FUEL_FIELD.setInt(this, 0);
        lastID = 0;
    }

    private static final Field COOK_TIME_FIELD, BURN_TIME_FIELD, TICKS_FOR_CURRENT_FUEL_FIELD;

    static {
        try {
            COOK_TIME_FIELD = TileEntityFurnace.class.getDeclaredField("cookTime");
            BURN_TIME_FIELD = TileEntityFurnace.class.getDeclaredField("burnTime");
            TICKS_FOR_CURRENT_FUEL_FIELD = TileEntityFurnace.class.getDeclaredField("ticksForCurrentFuel");
            COOK_TIME_FIELD.setAccessible(true);
            BURN_TIME_FIELD.setAccessible(true);
            TICKS_FOR_CURRENT_FUEL_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Can't find a necessary field for the fake furnace!");
        }
    }

    // Read from save

    @Override
    public boolean a(EntityHuman entityhuman) { // canUse
        return true;
    }

    @Override
    public void burn() {
        // Can't burn? Goodbye
        if (!canBurn()) {
            return;
        }
        ItemStack itemstack = getContents()[0] != null ? RecipesFurnace.getInstance().getResult(getContents()[0]) : null;
        // Nothing in there? Then put something there.
        if (getContents()[2] == null) {
            getContents()[2] = itemstack.cloneItemStack();
        }
        // Burn ahead
        else if (getContents()[2].doMaterialsMatch(itemstack)) {
            getContents()[2].count += itemstack.count;
        }
        // Consume the ingredient item
        Item craftingResult = getContents()[0].getItem().q();
        if (craftingResult != null) {
            getContents()[0] = new ItemStack(craftingResult);
        } else {
            getContents()[0].count--;
            // Let 0 be null
            if (getContents()[0].count <= 0) {
                getContents()[0] = null;
            }
        }
    }

    private boolean canBurn() {
        // No ingredient, no recipe
        if (getContents()[0] == null) {
            return false;
        }
        ItemStack itemstack = RecipesFurnace.getInstance().getResult(getContents()[0]);
        // No recipe, no burning
        if (itemstack == null) {
            return false;
        }
        // Free space? Let's burn!
        else if (getContents()[2] == null) {
            return true;
        }
        // Materials don't match? Too bad.
        else if (!getContents()[2].doMaterialsMatch(itemstack)) {
            return false;
        }
        // As long as there is space, we can burn
        else if ((getContents()[2].count + itemstack.count <= getMaxStackSize())
                && (getContents()[2].count + itemstack.count <= getContents()[2].getMaxStackSize())) {
            return true;
        }
        return false;
    }

    private double getBurnSpeed(ItemStack item) {
        if (item == null) {
            return 0.0D;
        }
        // CUSTOM FUEL HERE
        return 1.0D;
    }

    private int getFuelTime(ItemStack item) {
        if (item == null) {
            return 0;
        }
        // CUSTOM FUEL HERE
        // Lava should melt 128 items, not 100
        if (Item.getId(item.getItem()) == org.bukkit.Material.LAVA_BUCKET.getId()) {
            return 25600;
        } else {
            return fuelTime(item);
        }
    }

    public final void c() {
        tick();
    }

    @SneakyThrows(IllegalAccessException.class)
    public void tick() {
        int newID = getContents()[0] == null ? 0 : Item.getId(getContents()[0].getItem());
        // Has the item been changed?
        if (newID != lastID) {
            // Then reset the progress!
            myCookTime = 0.0D;
            lastID = newID;
            // And, most important: change the melt speed
            meltSpeed = getContents()[0] != null ? 1 : 0;
        }
        // So, can we now finally burn?
        if (canBurn() && !isBurning() && (getFuelTime(getContents()[1]) > 0)) {
            // I have no idea what "ticksForCurrentFuel" is good for, but it
            // works fine like this
            TICKS_FOR_CURRENT_FUEL_FIELD.setInt(this, burnTime = getFuelTime(getContents()[1]));
            // Before we remove the item: how fast does it burn?
            burnSpeed = getBurnSpeed(getContents()[1]);
            // If it's a container item (lava bucket), we only consume its
            // getContents() (not like evil Notch!)

            // If it's not a container, consume it! Om nom nom nom!
            {
                getContents()[1].count--;
                // Let 0 be null
                if (getContents()[1].count <= 0) {
                    getContents()[1] = null;
                }
            }
        }
        // Now, burning?
        if (isBurning()) {
            // Then move on
            burnTime--;
            // I'm using a double here because of the custom recipes.
            // The faster this fuel burns and the faster the recipe melts, the
            // faster we're done
            myCookTime += burnSpeed * meltSpeed;
            // Finished burning?
            if (myCookTime >= 200.0D) {
                myCookTime -= 200.0D;
                burn();
            }
        }
        // If it's not burning, we reset the burning progress!
        else {
            myCookTime = 0.0D;
        }
        // And for the display (I'm using floor rather than round to not cause
        // the client to do shit when we not really reached 200):
        cookTime = (int) Math.floor(myCookTime);
    }

    @Override
    public void showTo(Player player) {
        ((CraftPlayer) player).getHandle().openTileEntity(this);
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getItems() {
        return Arrays.stream(getContents())
                .map(CraftItemStack::asBukkitCopy)
                .collect(Collectors.toList());
    }
}