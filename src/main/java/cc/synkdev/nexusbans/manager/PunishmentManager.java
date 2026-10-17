package cc.synkdev.nexusbans.manager;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.object.PunishmentProfile;
import cc.synkdev.nexusbans.object.PunishmentType;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PunishmentManager {
    @Getter
    private static final List<PunishmentType> pTypes = new ArrayList<>();
    @Getter
    private static final Map<String, PunishmentProfile> punishMap = new HashMap<>();
    private PunishmentManager() {
        /* This utility class should not be instantiated */
    }

    public static void registerTypes(NexusBans core) {
        try (ScanResult result = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("cc.synkdev")
                .addClassLoader(PunishmentType.class.getClassLoader()).scan()) {
            List<Class<?>> list = result.getSubclasses(PunishmentType.class.getName()).loadClasses();

            for (Class<?> clazz : list) {
                PunishmentType type = (PunishmentType) clazz.getDeclaredConstructor().newInstance();
                type.registerListener();
                core.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                    event.registrar().register(type.getId(), type.getCommandHandler());
                    if (!type.isInstant()) event.registrar().register("un" + type.getId(), type.getUnCommandHandler());

                });
                PunishmentManager.getPTypes().add(type);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void alert(Player p, PunishmentType type) {
        alert(p, type.getId());
    }

    public static void alert(Player p, String type) {
        if (!ConfigManager.isAlertAttempt()) return;

        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("nexusbans.alert")).forEach(player -> Utils.sendMessage(player, "alert-" + type, p.getName()));
    }

    public static void alert(String key, String... args) {
        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("nexusbans.alert")).forEach(player -> Utils.sendMessage(player, key, args));
    }
}
