package cc.synkdev.nexusbans.object.punishment;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.manager.Utils;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentType;
import cc.synkdev.nexusbans.object.TargetType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Ban extends PunishmentType {
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
                joinEvent.getPlayer().kick(NexusBans.getInstance().getPrefix().append(Utils.translate(punish.getReason() == null ? "banned" : "banned-reason", punish.getReason())));
                PunishmentManager.alert(joinEvent.getPlayer(), this);
            }
        };
    }

    @Override
    public TargetType targetType() {
        return TargetType.PLAYER;
    }

    @Override
    public Boolean isInstant() {
        return false;
    }

    @Override
    public Runnable execute(Punishment p) {
        return () -> {
            OfflinePlayer oP = Bukkit.getOfflinePlayer(UUID.fromString(p.getTarget()));
            Utils.debug("OfflinePlayer " + p.getTarget());
            if (oP.isOnline()) {
                Utils.debug("Online");
                assert oP.getPlayer() != null;
                oP.getPlayer().kick(NexusBans.getInstance().getPrefix().append(Utils.translate(p.getReason() == null ? "banned" : "banned-reason", p.getReason())), PlayerKickEvent.Cause.BANNED);
                Utils.debug(Utils.translate(p.getReason() == null ? "banned" : "banned-reason", p.getReason()));
            }

        };
    }

    @Override
    public String getVerb() {
        return "banned";
    }

    @Override
    public String getId() {
        return "ban";
    }
}
