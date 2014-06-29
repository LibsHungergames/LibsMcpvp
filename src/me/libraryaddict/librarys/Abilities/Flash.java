package me.libraryaddict.librarys.Abilities;

import java.util.HashSet;
import java.util.List;

import me.libraryaddict.Hungergames.Types.AbilityListener;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Events.TimeSecondEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Flash extends AbilityListener implements Disableable {

    public boolean addMoreCooldownForLargeDistances = true;
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

    public int getCooldown(Player p) {
        return (p.hasMetadata("FlashCooldown") ? p.getMetadata("FlashCooldown").get(0).asInt() : 0);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player p = event.getPlayer();
        if (event.getAction().name().contains("RIGHT") && hasAbility(p)) {
            if (isSpecialItem(item, flashItemName)) {
                event.setCancelled(true);
                p.updateInventory();
                if (getCooldown(p) > HungergamesApi.getHungergames().currentTime) {
                    p.sendMessage(String.format(cooldownMessage, getCooldown(p) - HungergamesApi.getHungergames().currentTime));
                    if (item.getTypeId() != flashOffItemId) {
                        item.setTypeId(flashOffItemId);
                    }
                } else {
                    List<Block> b = p.getLastTwoTargetBlocks(ignoreBlockTypes, maxTeleportDistance);
                    if (b.size() > 1 && b.get(1).getType() != Material.AIR) {
                        double dist = p.getLocation().distance(b.get(0).getLocation());
                        if (dist > 2) {
                            Location loc = b.get(0).getLocation().clone().add(0.5, 0.5, 0.5);
                            item.setTypeId(flashOffItemId);
                            int hisCooldown = normalCooldown;
                            if (addMoreCooldownForLargeDistances && (dist / 2) > 30)
                                hisCooldown += (int) (dist / 2);
                            setCooldown(p, hisCooldown + HungergamesApi.getHungergames().currentTime);
                            Location pLoc = p.getLocation();
                            loc.setPitch(pLoc.getPitch());
                            loc.setYaw(pLoc.getYaw());
                            p.eject();
                            p.teleport(loc);
                            pLoc.getWorld().playSound(pLoc, Sound.ENDERMAN_TELEPORT, 1, 1.2F);
                            pLoc.getWorld().playSound(loc, Sound.ENDERMAN_TELEPORT, 1, 1.2F);
                            pLoc.getWorld().playEffect(pLoc, Effect.PORTAL, 0);
                            pLoc.getWorld().playEffect(loc, Effect.PORTAL, 0);
                            if (giveWeakness)
                                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) ((dist / 2) * 20), 1), true);
                            pLoc.getWorld().strikeLightningEffect(loc);
                            return;
                        }
                    }
                    if (item.getTypeId() != flashOnItemId) {
                        item.setTypeId(flashOnItemId);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSecond(TimeSecondEvent event) {
        for (Player p : getMyPlayers()) {
            if (HungergamesApi.getHungergames().currentTime == getCooldown(p)) {
                for (ItemStack i : p.getInventory().getContents()) {
                    if (isSpecialItem(i, flashItemName)) {
                        i.setTypeId(flashOnItemId);
                    }
                }
            }
            if (isSpecialItem(p.getItemOnCursor(), flashItemName)) {
                p.getItemOnCursor().setTypeId(flashOnItemId);
            }
        }
    }

    private void setCooldown(Player p, int newCooldown) {
        p.setMetadata("FlashCooldown", new FixedMetadataValue(HungergamesApi.getHungergames(), newCooldown));
    }
}
