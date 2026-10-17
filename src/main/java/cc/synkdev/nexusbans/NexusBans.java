package cc.synkdev.nexusbans;

import cc.synkdev.nexusbans.command.HistoryCommand;
import cc.synkdev.nexusbans.command.NbCommand;
import cc.synkdev.nexusbans.listener.JoinIpListener;
import cc.synkdev.nexusbans.manager.ConfigManager;
import cc.synkdev.nexusbans.manager.GuiManager;
import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.manager.Utils;
import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class NexusBans extends JavaPlugin {

    @Getter
    private static NexusBans instance;

    @Override
    public void onEnable() {
        instance = this;
        //MongoDB gets really loud with it
        Configurator.setLevel("org.mongodb.driver", Level.WARN);
        ConfigManager.load();
        PunishmentManager.registerTypes(this);
        GuiManager.reloadGuis(this);
        Bukkit.getPluginManager().registerEvents(new JoinIpListener(), this);


        PaperCommandManager pcm = new PaperCommandManager(this);
        pcm.registerCommand(new NbCommand());
        pcm.registerCommand(new HistoryCommand());
        ConfigManager.getStorage().init();
        ConfigManager.getStorage().loadAll();
    }

    public Component getPrefix() {
        return Utils.translate("prefix");
    }
}
