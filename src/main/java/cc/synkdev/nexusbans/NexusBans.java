package cc.synkdev.nexusbans;

import cc.synkdev.nexusbans.manager.ConfigManager;
import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.manager.Utils;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentProfile;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public final class NexusBans extends JavaPlugin {

    @Getter private static NexusBans instance;

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.load();
        PunishmentManager.registerTypes(this);
    }

    public Component getPrefix() {
        return Utils.translate("prefix");
    }
}
