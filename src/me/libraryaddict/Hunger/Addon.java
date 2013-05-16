package me.libraryaddict.Hunger;

import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.Hungergames.Types.Kit;

import org.bukkit.plugin.java.JavaPlugin;

public class Addon extends JavaPlugin {

    public void onEnable() {
        saveDefaultConfig();
        HungergamesApi.getAbilityManager().initializeAllAbilitiesInPackage(this, "me.libraryaddict.Hunger.Abilities");
        for (String string : getConfig().getConfigurationSection("Kits").getKeys(false)) {
            Kit kit = HungergamesApi.getKitManager().parseKit(getConfig().getConfigurationSection("Kits." + string));
            HungergamesApi.getKitManager().addKit(kit);
        }
    }

}
