package cc.synkdev.nexusbans.manager;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.object.PunishmentProfile;
import cc.synkdev.nexusbans.object.PunishmentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.util.UUID;

public class Utils {
    private Utils() {
        /* This utility class should not be instantiated */
    }

    public static Component translate(String key, String... placeholder) {
        String msg = ConfigManager.getMessages().getString(key);
        if (msg == null) {
            return MiniMessage.miniMessage().deserialize("<red>Invalid translation!");
        }
        if (placeholder.length > 0) {
            for (int i = 0; i < placeholder.length; i++) {
                msg = msg.replace("%s"+i+"%", placeholder[i]);
            }
        }
        return MiniMessage.miniMessage().deserialize(msg);
    }
    public static void log(Component message) {
        Bukkit.getConsoleSender().sendMessage(NexusBans.getInstance().getPrefix().append(message));
    }
    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(NexusBans.getInstance().getPrefix().append(MiniMessage.miniMessage().deserialize(message)));
    }

    public static void debug(String message) {
        Bukkit.getConsoleSender().sendMessage(NexusBans.getInstance().getPrefix().append(MiniMessage.miniMessage().deserialize("<dark_gray> "+message)));
    }

    public static boolean isPunished(String target, PunishmentType type) {
        return PunishmentManager.getPunishMap().getOrDefault(target, new PunishmentProfile(target))
                .getPunishmentMap().entrySet().stream().filter(entry -> entry.getValue().getType().equals(type))
                .anyMatch(entry -> entry.getValue().isActive());
    }
}
