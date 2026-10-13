package cc.synkdev.nexusbans;

import cc.synkdev.nexusbans.manager.ConfigManager;
import cc.synkdev.nexusbans.manager.Utils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class NexusBans extends JavaPlugin {

    @Getter private static NexusBans instance;

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.load();


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Component getPrefix() {
        return Utils.translate("prefix");
    }
}
