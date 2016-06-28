package me.libraryaddict.librarys.nms;

import me.libraryaddict.Hungergames.Types.Gamer;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

import java.lang.reflect.InvocationTargetException;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldEvent;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import static com.google.common.base.Preconditions.*;

public final class NMS {
    private NMS() {}

    public static FakeFurnace createFakeFurnace() {
        return new FakeFurnaceImpl();
    }

    //
    // Dark magic to attack hidden players
    //


    private static class Vector3D {

        // Use protected members, like Bukkit
        private final double x;
        private final double y;
        private final double z;

        private Vector3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private Vector3D(Location location) {
            this(location.toVector());
        }

        private Vector3D(Vector vector) {
            if (vector == null)
                throw new IllegalArgumentException("Vector cannot be NULL.");
            this.x = vector.getX();
            this.y = vector.getY();
            this.z = vector.getZ();
        }

        private Vector3D abs() {
            return new Vector3D(Math.abs(x), Math.abs(y), Math.abs(z));
        }

        private Vector3D add(double x, double y, double z) {
            return new Vector3D(this.x + x, this.y + y, this.z + z);
        }

        private Vector3D add(Vector3D other) {
            if (other == null)
                throw new IllegalArgumentException("other cannot be NULL");
            return new Vector3D(x + other.x, y + other.y, z + other.z);
        }

        private Vector3D multiply(double factor) {
            return new Vector3D(x * factor, y * factor, z * factor);
        }

        private Vector3D multiply(int factor) {
            return new Vector3D(x * factor, y * factor, z * factor);
        }

        private Vector3D subtract(Vector3D other) {
            if (other == null)
                throw new IllegalArgumentException("other cannot be NULL");
            return new Vector3D(x - other.x, y - other.y, z - other.z);
        }
    }

    // Some of this code is LGPL by 'Kristian S. Stangeland'

    public static void sendFakeItem(Player player, int slot, ItemStack stack) {
        checkNotNull(player, "Null player");
        checkNotNull(stack, "Null stack");
        checkArgument(slot >= 0, "Negative slot %s", slot);
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        packet.getIntegers().write(0, 0);
        packet.getIntegers().write(1, slot);
        packet.getItemModifier().write(0, stack);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Entity getEntityInSight(Player p, int rangeToScan) {
        Location observerPos = p.getEyeLocation();
        Vector3D observerDir = new Vector3D(observerPos.getDirection());
        Vector3D observerStart = new Vector3D(observerPos);
        Vector3D observerEnd = observerStart.add(observerDir.multiply(rangeToScan));

        Entity hit = null;

        // Get nearby entities
        for (Entity entity : p.getNearbyEntities(rangeToScan, rangeToScan, rangeToScan)) {
            // Bounding box of the given player
            if (entity instanceof Player) {
                Gamer gamer = HungergamesApi.getPlayerManager().getGamer(entity);
                if (gamer == null || !gamer.isAlive())
                    continue;
            }
            Vector3D targetPos = new Vector3D(entity.getLocation());
            Vector3D minimum = targetPos.add(-0.5, 0, -0.5);
            Vector3D maximum = targetPos.add(0.5, 1.67, 0.5);

            if (hasIntersection(observerStart, observerEnd, minimum, maximum)) {
                if (hit == null
                        || hit.getLocation().distanceSquared(observerPos) > entity.getLocation().distanceSquared(observerPos)) {
                    hit = entity;
                }
            }
        }
        return hit;
    }

    // Source:
    // [url]http://www.gamedev.net/topic/338987-aabb---line-segment-intersection-test/[/url]
    private static boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max) {
        final double epsilon = 0.0001f;

        Vector3D d = p2.subtract(p1).multiply(0.5);
        Vector3D e = max.subtract(min).multiply(0.5);
        Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
        Vector3D ad = d.abs();

        if (Math.abs(c.x) > e.x + ad.x)
            return false;
        if (Math.abs(c.y) > e.y + ad.y)
            return false;
        if (Math.abs(c.z) > e.z + ad.z)
            return false;

        if (Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon)
            return false;
        if (Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon)
            return false;
        if (Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon)
            return false;

        return true;
    }

    public static void showPortalEffect(Location l) {
        ((CraftWorld) l.getWorld()).getHandle().a( // showParticle (http://wiki.vg/index.php?title=Protocol&oldid=7368#Particle)
                EnumParticle.PORTAL,
                l.getX(),
                l.getY(),
                l.getZ(),
                // offsets (this is added to the X position after being multiplied by random.nextGaussian())
                0,
                0,
                0, // Particle 'data'
                1, // Particles to create
                32 // Apparently this is how many particles an enderpearl creates,
        );
    }
}
