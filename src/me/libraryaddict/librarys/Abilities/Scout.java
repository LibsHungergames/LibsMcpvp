package me.libraryaddict.librarys.Abilities;

import me.libraryaddict.Hungergames.Events.GameStartEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.HungergamesApi;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class Scout extends AbilityListener implements Disableable {
    public boolean cancelFall = true;
    public int givePotionsEverySoSeconds = 600;
    private int scheduler = -1;

    @EventHandler
    public void gameStartEvent(GameStartEvent event) {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), getRunnable(),
                givePotionsEverySoSeconds * 20, givePotionsEverySoSeconds * 20);
    }

    private Runnable getRunnable() {
        return new Runnable() {
            public void run() {
                for (Player p : getMyPlayers())
                    HungergamesApi.getKitManager().addItem(p, new ItemStack(Material.POTION, 2, (short) 16418));
            }
        };
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (cancelFall)
            if (event.getCause() == DamageCause.FALL && event.getEntity() instanceof Player
                    && hasAbility(((Player) event.getEntity()).getName())
                    && ((Player) event.getEntity()).hasPotionEffect(PotionEffectType.SPEED))
                event.setCancelled(true);
    }

    public void registerPlayer(Player player) {
        super.registerPlayer(player);
        if (scheduler < 0 && HungergamesApi.getHungergames().currentTime >= 0) {
            int toTake = HungergamesApi.getHungergames().currentTime;
            if (toTake > givePotionsEverySoSeconds * 20)
                toTake = givePotionsEverySoSeconds * 20;
            scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), getRunnable(),
                    Math.abs(givePotionsEverySoSeconds * 20) - toTake, givePotionsEverySoSeconds * 20);
        }
    }
}
