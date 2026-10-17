package cc.synkdev.nexusbans.manager;

import lombok.Getter;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class IpManager {
    @Getter
    private static final Map<UUID, List<String>> ipMap = new HashMap<>();

    private IpManager() {
        /* This utility class should not be instantiated */
    }

    public static void storeIP(PlayerJoinEvent event) {
        List<String> list = ipMap.getOrDefault(event.getPlayer().getUniqueId(), new ArrayList<>());
        String ip = Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().getHostAddress();
        if (!list.contains(ip)) list.add(ip);

        ipMap.put(event.getPlayer().getUniqueId(), list);
        ConfigManager.getStorage().save(event.getPlayer().getUniqueId(), list);
    }
}
