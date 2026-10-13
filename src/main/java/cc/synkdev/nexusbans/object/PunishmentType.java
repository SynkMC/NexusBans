package cc.synkdev.nexusbans.object;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class PunishmentType implements Listener {
    public abstract String getId();
    public abstract Class<? extends Event> eventClass();
    public abstract Consumer<Event> handleEvent();
    public abstract TargetType targetType();
    public abstract Boolean isInstant();
    public abstract Runnable execute(Punishment p);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PunishmentType && ((PunishmentType) obj).getId().equalsIgnoreCase(getId());
    }

    public Boolean doPunish(OfflinePlayer p) {
        return (targetType() == TargetType.PLAYER ? Utils.isPunished(p.getUniqueId(), this) : (!p.isOnline() || Utils.isPunished(p.getPlayer().getAddress().getHostName(), this))) && (!p.isOnline() || !((Player) p).hasPermission("nexusstaff.punishment.bypass"));
    }

    public Boolean doPunish(String s) {
        if (targetType() == TargetType.IP) {
            return Utils.isPunished(s, this);
        } else {
            return Utils.isPunished(UUID.fromString(s), this) && (Utils.isOnline(UUID.fromString(s)) && Bukkit.getPlayer(s).hasPermission("nexusstaff.punishment.bypass"));
        }
    }

    public void registerListener() {
        if (eventClass() == null || handleEvent() == null) return;
        EventExecutor executor = (_, event) -> {
            if (eventClass().isInstance(event)) {
                handleEvent().accept(event);
            }
        };
        Utils.debug("Registered pType "+getId());
        Bukkit.getPluginManager().registerEvent(eventClass(), this, EventPriority.HIGH, executor, NexusBans.getInstance());
    }

    public void executePunishment(Punishment punishment) {
        if (doPunish(punishment.getTarget())) {
            Utils.debug("Doing punish");
            execute(punishment).run();
        }
    }
}
