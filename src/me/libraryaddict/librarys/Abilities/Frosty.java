package me.libraryaddict.librarys.Abilities;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Events.GameStartEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Managers.ReflectionManager;

public class Frosty extends AbilityListener implements Disableable {
    private ArrayList<BlockFace> faces = new ArrayList<BlockFace>();
    public int iceHeight = 1;
    public int iceRadius = 3;
    private HashMap<Entity, Integer> ids = new HashMap<Entity, Integer>();
    public int potionMultiplier = 1;
    public boolean snowballsScheduler = true;

    public Frosty() {
        faces.add(BlockFace.SOUTH);
        faces.add(BlockFace.NORTH);
        faces.add(BlockFace.EAST);
        faces.add(BlockFace.WEST);
    }

    @EventHandler
    public void gameStart(GameStartEvent event) {
        if (!snowballsScheduler)
            ProjectileLaunchEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.SNOW
                && hasAbility(event.getPlayer())
                && (event.getPlayer().getItemInHand() == null || !event.getPlayer().getItemInHand().getType().name()
                        .contains("SPADE"))) {
            event.getBlock()
                    .getWorld()
                    .dropItemNaturally(event.getBlock().getLocation().clone().add(0.5, 0, 0.5), new ItemStack(Material.SNOW_BALL));
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if ((snowballsScheduler && ids.containsKey(event.getEntity()))
                || (event.getEntity().getType() == EntityType.SNOWBALL && event.getEntity().getShooter() != null
                        && event.getEntity().getShooter() instanceof Player && hasAbility((Player) event.getEntity().getShooter()))) {
            transform(event.getEntity());
            if (ids.containsKey(event.getEntity()))
                Bukkit.getScheduler().cancelTask(ids.get(event.getEntity()));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (hasAbility(event.getPlayer()))
            if (event.getPlayer().getLocation().getBlock().getType() == Material.SNOW)
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, potionMultiplier), true);
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        if (event.getEntity().getType() == EntityType.SNOWBALL && event.getEntity().getShooter() != null
                && event.getEntity().getShooter() instanceof Player && hasAbility((Player) event.getEntity().getShooter())) {
            final Entity snowball = event.getEntity();
            ids.put(snowball, Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), new Runnable() {
                public void run() {
                    Material type = snowball.getLocation().getBlock().getType();
                    if (snowball.isDead() || type == Material.WATER || type == Material.STATIONARY_WATER) {
                        transform(snowball);
                        if (!snowball.isDead())
                            snowball.remove();
                        Bukkit.getScheduler().cancelTask(ids.remove(snowball));
                    }
                }
            }, 0, 0));
        }
    }

    private boolean canPlace(Location loc) {
        try {
            ReflectionManager reflection = HungergamesApi.getReflectionManager();
            for (Method blockMethod : reflection.getNmsClass("Block").getMethods()) {
                if (Modifier.isStatic(blockMethod.getModifiers()) && blockMethod.getParameterTypes().length == 1
                        && blockMethod.getParameterTypes()[0] == int.class
                        && blockMethod.getReturnType().getSimpleName().equals("Block")) {
                    Object snowBlock = blockMethod.invoke(null, Material.SNOW.getId());
                    Method canPlace = snowBlock.getClass().getMethod("canPlace", reflection.getNmsClass("World"), int.class,
                            int.class, int.class);
                    return (Boolean) canPlace.invoke(snowBlock,
                            loc.getWorld().getClass().getMethod("getHandle").invoke(loc.getWorld()), loc.getBlockX(),
                            loc.getBlockY(), loc.getBlockZ());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void transform(Entity entity) {
        Location loc = entity.getLocation();
        if (loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
            loc = loc.getBlock().getRelative(BlockFace.DOWN).getLocation();
        if (loc.getBlock().getType() == Material.AIR && canPlace(loc))
            loc.getBlock().setType(Material.SNOW);
        else {
            Collections.shuffle(faces, new Random());
            for (BlockFace face : faces) {
                Block b = loc.getBlock().getRelative(face);
                if (b.getType() == Material.AIR && canPlace(b.getLocation())) {
                    b.setType(Material.SNOW);
                    break;
                }
            }
        }
        for (int x = -iceRadius; x <= iceRadius; x++) {
            for (int z = -iceRadius; z <= iceRadius; z++) {
                for (int y = -iceHeight; y <= iceHeight; y++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    if (b.getLocation().distance(loc) < iceRadius
                            && (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER)) {
                        b.setType(Material.ICE);
                    }
                }
            }
        }
    }

}
