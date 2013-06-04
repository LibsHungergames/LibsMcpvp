package me.libraryaddict.librarys.Abilities;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Types.AbilityListener;

public class Reaper extends AbilityListener {
    public String reaperItemName = "Death Scythe";
    public int reaperEffectLastsSeconds = 6;

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && isSpecialItem(((Player) event.getDamager()).getItemInHand(), reaperItemName)) {
            if (event.getEntity() instanceof LivingEntity) {
                ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WITHER,
                        reaperEffectLastsSeconds * 20, 0), true);
            }
        }
    }
}
