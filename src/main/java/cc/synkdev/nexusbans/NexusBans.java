package cc.synkdev.nexusbans;

import cc.synkdev.nexusbans.listener.JoinIpListener;
import cc.synkdev.nexusbans.manager.ConfigManager;
import cc.synkdev.nexusbans.manager.PunishmentManager;
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
        PunishmentManager.registerTypes(this);

        Bukkit.getPluginManager().registerEvents(new JoinIpListener(), this);
    }

    public Component getPrefix() {
        return Utils.translate("prefix");
    }
}
