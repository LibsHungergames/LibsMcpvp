package me.libraryaddict.librarys.Misc;

import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_6_R2.EntityCreature;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.PathfinderGoalTarget;

public class OwnerAttacks extends PathfinderGoalTarget {
    EntityCreature a;
    EntityLiving b;
    EntityLiving owner;

    public OwnerAttacks(EntityCreature paramEntityTameableAnimal, Player owner) {
        super(paramEntityTameableAnimal, false);
        this.a = paramEntityTameableAnimal;
        a(1);
        this.owner = ((CraftPlayer) owner).getHandle();
    }

    public boolean a() {
        this.b = owner.aF();
        return a(this.b, false);
    }

    public void c() {
        this.c.setGoalTarget(this.b);
        super.c();
    }
}