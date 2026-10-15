package cc.synkdev.nexusbans.object;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.Utils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public abstract class PluginGui {
    public abstract String getId();
    public File getFile() {
        return new File(new File(NexusBans.getInstance().getDataFolder(), "gui"), getId()+".yml");
    }
    public YamlConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(getFile());
    }
    public void init(NexusBans core) {
        if (!getFile().exists()) {
            getFile().getParentFile().mkdirs();
            try {
                Utils.debug("File exists? "+(core.getResource("history.yml") == null) +" Path: "+getFile().toPath().toString());
                Files.copy(Objects.requireNonNull(core.getResource("history.yml")), getFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
