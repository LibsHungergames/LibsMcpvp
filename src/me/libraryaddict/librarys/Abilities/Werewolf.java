package me.libraryaddict.librarys.Abilities;

import me.libraryaddict.Hungergames.Events.GameStartEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.Hungergames.Types.HungergamesApi;

public class Werewolf extends AbilityListener implements Disableable {
    public String[] potionEffectsDuringDay = new String[] { "WEAKNESS 0" };
    public String[] potionEffectsDuringNight = new String[] { "SPEED 0", "INCREASE_DAMAGE 0" };
    private int scheduler = -1;

    @EventHandler
    public void gameStartEvent(GameStartEvent event) {
        getRunnable().run();
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), getRunnable(),
                12000 - (HungergamesApi.getHungergames().world.getTime() % 12000), 12000);
    }

    private Runnable getRunnable() {
        return new Runnable() {
            public void run() {
                World world = HungergamesApi.getHungergames().world;
                for (Player p : getMyPlayers()) {
                    if (HungergamesApi.getHungergames().world.getTime() >= 0
                            && HungergamesApi.getHungergames().world.getTime() < 12000) {
                        for (String string : potionEffectsDuringDay) {
                            String[] effect = string.split(" ");
                            PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(effect[0].toUpperCase()),
                                    (int) (12000 - (world.getTime() % 12000)), Integer.parseInt(effect[1]));
                            p.addPotionEffect(potionEffect, true);
                        }
                    } else {
                        for (String string : potionEffectsDuringNight) {
                            String[] effect = string.split(" ");
                            PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(effect[0].toUpperCase()),
                                    (int) (12000 - (world.getTime() % 12000)), Integer.parseInt(effect[1]));
                            p.addPotionEffect(potionEffect, true);
                        }
                    }
                }
            }
        };
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player && hasAbility((Player) event.getTarget())
                && event.getEntityType() == EntityType.WOLF)
            event.setCancelled(true);
    }

    public void registerPlayer(Player player) {
        super.registerPlayer(player);
        if (scheduler < 0 && HungergamesApi.getHungergames().currentTime >= 0)
            scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(HungergamesApi.getHungergames(), getRunnable(), 0,
                    12000 - (HungergamesApi.getHungergames().world.getTime() % 12000));
    }

}
