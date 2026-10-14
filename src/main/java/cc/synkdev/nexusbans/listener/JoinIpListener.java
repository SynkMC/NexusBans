package cc.synkdev.nexusbans.listener;

import cc.synkdev.nexusbans.manager.IpManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinIpListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        IpManager.storeIP(event);
    }
}
