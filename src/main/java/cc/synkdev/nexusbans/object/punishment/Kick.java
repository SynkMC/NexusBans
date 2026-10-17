package cc.synkdev.nexusbans.object.punishment;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.Utils;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentType;
import cc.synkdev.nexusbans.object.TargetType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Kick extends PunishmentType {
    @Override
    public Class<? extends Event> eventClass() {
        return null;
    }

    @Override
    public Consumer<Event> handleEvent() {
        return null;
    }

    @Override
    public TargetType targetType() {
        return TargetType.PLAYER;
    }

    @Override
    public Boolean isInstant() {
        return true;
    }

    @Override
    public Runnable execute(Punishment p) {
        return () -> {
            OfflinePlayer oP = Bukkit.getOfflinePlayer(UUID.fromString(p.getTarget()));
            if (oP.isOnline()) {
                assert oP.getPlayer() != null;
                oP.getPlayer().kick(NexusBans.getInstance().getPrefix().append(Utils.translate(p.getReason() == null ? "kicked" : "kicked-reason", p.getReason())), PlayerKickEvent.Cause.KICKED);
                Utils.debug(Utils.translate(p.getReason() == null ? "kicked" : "kicked-reason", p.getReason()));
            }

        };
    }

    @Override
    public String getVerb() {
        return "kicked";
    }

    @Override
    public String getId() {
        return "kick";
    }
}
