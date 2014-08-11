package me.libraryaddict.librarys.Abilities;

import org.bukkit.event.EventHandler;

import me.libraryaddict.Hungergames.Abilities.Ninja;
import me.libraryaddict.Hungergames.Events.GameStartEvent;

public class Werewolf extends Ninja {
    public String[] potionEffectsDuringDay = new String[] { "WEAKNESS 0" };
    public String[] potionEffectsDuringNight = new String[] { "SPEED 0", "INCREASE_DAMAGE 0" };

    @EventHandler
    public void gameStartEvent(GameStartEvent event) {
        super.potionEffectsDuringDay = potionEffectsDuringDay;
        super.potionEffectsDuringNight = potionEffectsDuringNight;
        super.gameStartEvent(event);
    }
}
