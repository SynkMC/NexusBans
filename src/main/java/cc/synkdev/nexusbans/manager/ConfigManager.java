package cc.synkdev.nexusbans.manager;

import cc.synkdev.nexusbans.NexusBans;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigManager {
    private ConfigManager() {
        /* This utility class should not be instantiated */
    }

    private static final NexusBans core = NexusBans.getInstance();
    private static final File configFile = new File(core.getDataFolder(), "config.yml");
    @Getter private static final List<String> skip = new ArrayList<>();
    private static YamlConfiguration config;
    @Getter private static ConfigurationSection messages;

    @Getter private static boolean debug;
    @Getter private static boolean alertAttempt;
    @Getter private static boolean banAllIps;
    @Getter private static String dateFormat;

    private static void updateConfig() throws IOException {
        if (!core.getDataFolder().exists()) core.getDataFolder().mkdirs();
        if (!configFile.exists()) {
            Files.copy(Objects.requireNonNull(core.getResource("config.yml")), configFile.toPath());
        } else {
            File temp = new File(core.getDataFolder(), "temp-config-" + System.currentTimeMillis() + ".yml");
            Files.copy(Objects.requireNonNull(core.getResource("config.yml")), temp.toPath());
            FileConfiguration tempConfig = YamlConfiguration.loadConfiguration(temp);
            config = YamlConfiguration.loadConfiguration(configFile);
            boolean changed = false;
            for (String key : tempConfig.getKeys(true)) {
                if (!config.contains(key) && ConfigManager.getSkip().stream().noneMatch(key::startsWith)) {
                    config.set(key, tempConfig.get(key));
                    changed = true;
                }
            }

            if (changed) {
                config.save(configFile);
            }

            if (!temp.delete()) {
                temp.deleteOnExit();
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);

    }

    public static void load() {
        try {
            updateConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        messages = config.getConfigurationSection("messages");

        debug = config.contains("debug") && config.getBoolean("debug");
        alertAttempt = config.getBoolean("alert-on-attempt");
        banAllIps = config.getBoolean("ban-all-ips");
        dateFormat = config.getString("date-format");
    }
}
