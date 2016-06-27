package me.libraryaddict.librarys.Abilities;

import java.util.Collection;

import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class Doctor extends AbilityListener implements Disableable {
    public boolean heal = false;
    public int pairOfForcepsItemId = Material.SHEARS.getId();
    public String pairOfForcepsItemName = ChatColor.WHITE + "Pair of Forceps";
    public double toHeal = 5;

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getItemInHand();
        if (event.getRightClicked() instanceof LivingEntity && isSpecialItem(item, pairOfForcepsItemName)
                && pairOfForcepsItemId == item.getTypeId() && hasAbility(event.getPlayer())) {
            LivingEntity lEntity = (LivingEntity) event.getRightClicked();
            Collection<PotionEffect> effects = lEntity.getActivePotionEffects();
            for (PotionEffect effect : effects)
                lEntity.removePotionEffect(effect.getType());
            if (heal) {
                double health = lEntity.getHealth();
                health += toHeal;
                if (health > lEntity.getMaxHealth())
                    health = lEntity.getMaxHealth();
                lEntity.setHealth(health);
            }
        }
    }

}
