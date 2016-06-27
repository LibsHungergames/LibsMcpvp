package me.libraryaddict.librarys.Abilities;

import java.util.HashMap;

import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Events.PlayerKilledEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;

public class Berserker extends AbilityListener implements Disableable {
    private class DmgBoost {
        int expires;
        int extraDamage;
    }

    public int berserkerLength = 30;
    private HashMap<Player, DmgBoost> dmgBuff = new HashMap<Player, DmgBoost>();
    public int extraDamageMob = 2;
    public int extraDamagePlayer = 4;
    public boolean giveConfusion = true;

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (dmgBuff.containsKey(event.getDamager())) {
            DmgBoost boost = dmgBuff.get(event.getDamager());
            if (boost.expires < HungergamesApi.getHungergames().currentTime)
                dmgBuff.remove(event.getDamager());
            else
                event.setDamage(event.getDamage() + boost.extraDamage);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null && hasAbility(event.getEntity().getKiller())) {
            Player p = event.getEntity().getKiller();
            if (event.getEntity() instanceof Creature) {
                setDamage(p, extraDamageMob);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerKilledEvent event) {
        dmgBuff.remove(event.getKilled().getPlayer());
        if (event.getKillerPlayer() != null) {
            Player p = event.getKillerPlayer().getPlayer();
            if (hasAbility(p))
                setDamage(p, extraDamagePlayer);
        }
    }

    private void setDamage(Player p, int damage) {
        DmgBoost boost = new DmgBoost();
        boost.expires = HungergamesApi.getHungergames().currentTime + berserkerLength;
        boost.extraDamage = damage;
        dmgBuff.put(p, boost);
        if (giveConfusion)
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, berserkerLength * 20, 0), true);
    }

}
