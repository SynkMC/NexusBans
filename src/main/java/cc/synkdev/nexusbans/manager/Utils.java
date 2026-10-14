package cc.synkdev.nexusbans.manager;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentProfile;
import cc.synkdev.nexusbans.object.PunishmentType;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
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
                msg = msg.replace("%s"+(i+1)+"%", placeholder[i]);
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
        if (ConfigManager.isDebug()) log(MiniMessage.miniMessage().deserialize("<dark_gray>[DEBUG] » "+message));
    }

    public static void debug(Component component) {
        if (ConfigManager.isDebug()) log(MiniMessage.miniMessage().deserialize("<dark_gray>[DEBUG] » ").append(component));
    }

    public static boolean isPunished(String target, PunishmentType type) {
        return PunishmentManager.getPunishMap().getOrDefault(target, new PunishmentProfile(target))
                .getPunishmentMap().entrySet().stream().filter(entry -> entry.getValue().getType().equals(type))
                .anyMatch(entry -> entry.getValue().isActive());
    }

    public static boolean isOnline(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).isOnline();
    }

    public static String parseTime(long time) {
        StringBuilder sb = new StringBuilder();
        if (time >= 365 * 24 * 60 * 60) {
            sb.append((int) time / (365 * 24 * 60 * 60)).append("y ");
            time %= 365 * 24 * 60 * 60;
        }
        if (time >= 30 * 24 * 60 * 60) {
            sb.append((int) time / (30 * 24 * 60 * 60)).append("mo ");
            time %= 30 * 24 * 60 * 60;
        }
        if (time >= 7 * 24 * 60 * 60) {
            sb.append((int) time / (7 * 24 * 60 * 60)).append("w ");
            time %= 7 * 24 * 60 * 60;
        }
        if (time >= 24 * 60 * 60) {
            sb.append((int) time / (24 * 60 * 60)).append("d ");
            time %= 24 * 60 * 60;
        }
        if (time >= 60 * 60) {
            sb.append((int) time / (60 * 60)).append("h ");
            time %= 60 * 60;
        }
        if (time >= 60) {
            sb.append((int) time / (60)).append("m ");
            time %= 60;
        }
        if (time > 0) {
            sb.append((int) time).append("s");
        }
        return sb.toString().trim();
    }
    public static Punishment getActivePunishment(String string, PunishmentType type) {
        Map.Entry<UUID, Punishment> entry = PunishmentManager.getPunishMap().getOrDefault(string, new PunishmentProfile(string)).getPunishmentMap().entrySet().stream().filter(mapEntry -> mapEntry.getValue().isActive() && mapEntry.getValue().getType().equals(type)).findFirst().orElse(null);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }
    public static Punishment getActivePunishment(Player player, PunishmentType type) {
        return getActivePunishment(player.getUniqueId().toString(), type);
    }
    public static long getTimeRemaining(String string, PunishmentType type) {
        Punishment punishment = getActivePunishment(string, type);
        if (punishment == null) return 0;
        return (punishment.getExpiry()-System.currentTimeMillis())/1000;
    }
    public static long getTimeRemaining(Player player, PunishmentType type) {
        return getTimeRemaining(player.getUniqueId().toString(), type);
    }

    public static void sendMessage(@NotNull Player player, String key, String... args) {
        player.sendMessage(NexusBans.getInstance().getPrefix().append(translate(key, args)));
    }

    public static void sendMessage(@NotNull CommandSender sender, String key, String... args) {
        sender.sendMessage(NexusBans.getInstance().getPrefix().append(translate(key, args)));
    }

    public static long parseTime(String time) {
        long totalMillis = 0;
            if (time.endsWith("h")) {
                totalMillis += Long.parseLong(time.replace("h", "")) * 60 * 60;
            } else if (time.endsWith("m")) {
                totalMillis += Long.parseLong(time.replace("m", "")) * 60;
            } else if (time.endsWith("s")) {
                totalMillis += Long.parseLong(time.replace("s", ""));
            } else if (time.endsWith("d")) {
                totalMillis += Long.parseLong(time.replace("d", "")) * 24 * 60 * 60;
            } else if (time.endsWith("w")) {
                totalMillis += Long.parseLong(time.replace("w", "")) * 7 * 24 * 60 * 60;
            } else if (time.endsWith("mo")) {
                totalMillis += Long.parseLong(time.replace("mo", "")) * 30 * 24 * 60 * 60;
            } else if (time.endsWith("y")) {
                totalMillis += Long.parseLong(time.replace("y", "")) * 365 * 24 * 60 * 60;
            }

        return totalMillis;
    }

    public static PunishmentType getPunishmentType(String id) {
        return PunishmentManager.getPTypes().stream().filter(pType -> pType.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
    public static void punish(String target, PunishmentType type, long expiry, String reason, UUID executor) {
        Punishment punishment = new Punishment(type, target, executor, reason, expiry);
        PunishmentProfile profile = PunishmentManager.getPunishMap().getOrDefault(target, new PunishmentProfile(target));
        profile.getPunishmentMap().put(punishment.getUuid(), punishment);
        PunishmentManager.getPunishMap().put(target, profile);

        boolean temp = expiry != -1L;
        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("nexusbans.alert")).forEach(player -> sendMessage(player, temp ? "temp-punishment-success" : "punishment-success", executor == null ? "Console" : getName(executor), type.getVerb(), Utils.getName(target), type.isInstant() ? reason : parseTime(getTimeRemaining(target, type))));
        getOnlinePlayers(target).forEach(player -> sendMessage(player, temp ? "temp-punished-notify" : "punished-notify", type.getVerb(), parseTime(getTimeRemaining(target, type))));
        type.executePunishment(punishment);
    }

    private static String getName(String target) {
        try {
            UUID uuid = UUID.fromString(target);
            return Bukkit.getOfflinePlayer(uuid).getName();
        } catch (Exception _) {
            return target;
        }
    }

    public static String getName(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }
    public static boolean isIp(String s) {
        return s.split("\\.").length == 4;
    }
    public static List<Player> getOnlinePlayers(String target) {
        if (isIp(target)) {
            Bukkit.getOnlinePlayers().forEach(player -> log(player.getAddress().getHostName()+", "+player.getAddress().toString()));
            return Bukkit.getOnlinePlayers().stream().filter(player -> player.getAddress().getHostName().equals(target))
                    .map(OfflinePlayer::getPlayer).toList();
        } else {
            OfflinePlayer oP = Bukkit.getOfflinePlayer(UUID.fromString(target));
            if (oP.isOnline()) return Collections.singletonList(oP.getPlayer());
            else return Collections.emptyList();
        }
    }
    public static void updatePunishment(Punishment p) {
        PunishmentProfile profile = PunishmentManager.getPunishMap().getOrDefault(p.getTarget(), new PunishmentProfile(p.getTarget()));
        profile.getPunishmentMap().put(p.getUuid(), p);

        PunishmentManager.getPunishMap().put(p.getTarget(), profile);
    }

    public static void unpunish(String target, PunishmentType pt) {
        Punishment punishment = getActivePunishment(target, pt);
        if (punishment == null) return;
        punishment.setExpiry(System.currentTimeMillis());
        updatePunishment(punishment);
    }
}
