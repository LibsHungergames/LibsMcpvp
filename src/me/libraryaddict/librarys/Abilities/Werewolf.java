package me.libraryaddict.librarys.Abilities;

import me.libraryaddict.Hungergames.Abilities.Ninja;
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

public class Werewolf extends Ninja {
    public String[] potionEffectsDuringDay = new String[] { "WEAKNESS 0" };
    public String[] potionEffectsDuringNight = new String[] { "SPEED 0", "INCREASE_DAMAGE 0" };
}
