package me.libraryaddict.librarys.Abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import me.libraryaddict.Hungergames.Types.AbilityListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R2.CraftWorld;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Events.TimeSecondEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.Gamer;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Flash extends AbilityListener implements Disableable {

    public boolean addMoreCooldownForLargeDistances = true;
    private transient HashMap<ItemStack, Integer> cooldown = new HashMap<ItemStack, Integer>();
    public String cooldownMessage = ChatColor.BLUE + "You can use this again in %s seconds!";
    public String flashItemName = ChatColor.WHITE + "Flash";
    public int flashOffItemId = Material.TORCH.getId();
    public int flashOnItemId = Material.REDSTONE_TORCH_ON.getId();
    public boolean giveWeakness = true;
    private HashSet<Byte> ignoreBlockTypes = new HashSet<Byte>();
    public int maxTeleportDistance = 200;
    public int normalCooldown = 30;

    public Flash() {
        ignoreBlockTypes.add((byte) 0);
        for (byte b = 8; b < 12; b++)
            ignoreBlockTypes.add(b);
        ignoreBlockTypes.add((byte) Material.SNOW.getId());
        ignoreBlockTypes.add((byte) Material.LONG_GRASS.getId());
        ignoreBlockTypes.add((byte) Material.RED_MUSHROOM.getId());
        ignoreBlockTypes.add((byte) Material.RED_ROSE.getId());
        ignoreBlockTypes.add((byte) Material.YELLOW_FLOWER.getId());
        ignoreBlockTypes.add((byte) Material.BROWN_MUSHROOM.getId());
        ignoreBlockTypes.add((byte) Material.SIGN_POST.getId());
        ignoreBlockTypes.add((byte) Material.WALL_SIGN.getId());
        ignoreBlockTypes.add((byte) Material.FIRE.getId());
        ignoreBlockTypes.add((byte) Material.TORCH.getId());
        ignoreBlockTypes.add((byte) Material.REDSTONE_WIRE.getId());
        ignoreBlockTypes.add((byte) Material.REDSTONE_TORCH_OFF.getId());
        ignoreBlockTypes.add((byte) Material.REDSTONE_TORCH_ON.getId());
        ignoreBlockTypes.add((byte) Material.VINE.getId());
        ignoreBlockTypes.add((byte) Material.WATER_LILY.getId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (event.getAction().name().contains("RIGHT") && hasAbility(event.getPlayer())) {
            if (isSpecialItem(item, flashItemName)) {
                event.setCancelled(true);
                event.getPlayer().updateInventory();
                if (item.getTypeId() == flashOffItemId) {
                    event.getPlayer()
                            .sendMessage(
                                    String.format(cooldownMessage,
                                            (-(HungergamesApi.getHungergames().currentTime - cooldown.get(item)))));
                } else if (item.getTypeId() == flashOnItemId) {
                    List<Block> b = event.getPlayer().getLastTwoTargetBlocks(ignoreBlockTypes, maxTeleportDistance);
                    if (b.size() > 1 && b.get(1).getType() != Material.AIR) {
                        double dist = event.getPlayer().getLocation().distance(b.get(0).getLocation());
                        if (dist > 2) {
                            Location loc = b.get(0).getLocation().clone().add(0.5, 0.5, 0.5);
                            item.setTypeId(flashOffItemId);
                            int hisCooldown = normalCooldown;
                            if (addMoreCooldownForLargeDistances && (dist / 2) > 30)
                                hisCooldown = (int) (dist / 2);
                            cooldown.put(item, hisCooldown + HungergamesApi.getHungergames().currentTime);
                            Location pLoc = event.getPlayer().getLocation();
                            loc.setPitch(pLoc.getPitch());
                            loc.setYaw(pLoc.getYaw());
                            event.getPlayer().eject();
                            event.getPlayer().teleport(loc);
                            pLoc.getWorld().playSound(pLoc, Sound.ENDERMAN_TELEPORT, 1, 1.2F);
                            pLoc.getWorld().playSound(loc, Sound.ENDERMAN_TELEPORT, 1, 1.2F);
                            ((CraftWorld) pLoc.getWorld()).getHandle().addParticle("portal", pLoc.getX(), pLoc.getY(),
                                    pLoc.getZ(), loc.getX(), loc.getY(), loc.getZ());
                            ((CraftWorld) pLoc.getWorld()).getHandle().addParticle("portal", loc.getX(), loc.getY(), loc.getZ(),
                                    pLoc.getX(), pLoc.getY(), pLoc.getZ());
                            if (giveWeakness)
                                event.getPlayer().addPotionEffect(
                                        new PotionEffect(PotionEffectType.WEAKNESS, (int) ((dist / 2) * 20), 1), true);
                            pLoc.getWorld().strikeLightningEffect(loc);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSecond(TimeSecondEvent event) {
        if (cooldown.containsValue(HungergamesApi.getHungergames().currentTime)) {
            Iterator<ItemStack> itel = cooldown.keySet().iterator();
            while (itel.hasNext()) {
                boolean carryOn = false;
                ItemStack item = itel.next();
                if (cooldown.get(item) == HungergamesApi.getHungergames().currentTime) {
                    for (Gamer gamer : HungergamesApi.getPlayerManager().getAliveGamers()) {
                        if (gamer.getPlayer().getInventory().contains(item)) {
                            itel.remove();
                            for (ItemStack i : gamer.getPlayer().getInventory().getContents()) {
                                if (i.equals(item)) {
                                    i.setTypeId(flashOnItemId);
                                    carryOn = true;
                                    break;
                                }
                            }
                            if (carryOn)
                                break;
                        }
                        if (gamer.getPlayer().getItemOnCursor() != null && gamer.getPlayer().getItemOnCursor().equals(item)) {
                            gamer.getPlayer().getItemOnCursor().setTypeId(flashOnItemId);
                            carryOn = true;
                            break;
                        }
                    }
                    if (carryOn)
                        continue;
                    for (Item itemEntity : HungergamesApi.getHungergames().world.getEntitiesByClass(Item.class)) {
                        if (itemEntity.getItemStack().equals(item)) {
                            itel.remove();
                            itemEntity.getItemStack().setTypeId(flashOnItemId);
                            carryOn = true;
                            break;
                        }
                    }
                }
            }
        }
    }
}
