package cc.synkdev.nexusbans.object.punishment;

import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.manager.Utils;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentType;
import cc.synkdev.nexusbans.object.TargetType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class IpBan extends PunishmentType {
    @Override
    public Class<? extends Event> eventClass() {
        return PlayerJoinEvent.class;
    }

    @Override
    public Consumer<Event> handleEvent() {
        return event -> {
            PlayerJoinEvent joinEvent = (PlayerJoinEvent) event;
            if (isPunished(joinEvent.getPlayer())) {
                Punishment punish = Utils.getActivePunishment(joinEvent.getPlayer(), this);
                joinEvent.getPlayer().kick(Utils.translate(punish.getReason() == null ? "ipbanned" : "ipbanned-reason", punish.getReason()));
                PunishmentManager.alert(joinEvent.getPlayer(), "ban");
            }
        };
    }

    @Override
    public TargetType targetType() {
        return TargetType.IP;
    }

    @Override
    public Boolean isInstant() {
        return false;
    }

    @Override
    public Runnable execute(Punishment p) {
        return () -> {
           Bukkit.getOnlinePlayers().stream().filter(pl -> pl.getAddress().getHostName().equals(p.getTarget())).forEach(player -> {
               player.kick(Utils.translate(p.getReason() == null ? "ipbanned" : "ipbanned-reason", p.getReason()));
           });
        };
    }

    @Override
    public String getVerb() {
        return "ipbanned";
    }

    @Override
    public String getId() {
        return "ipban";
    }
}
