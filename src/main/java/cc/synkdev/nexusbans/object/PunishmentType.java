package cc.synkdev.nexusbans.object;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.Utils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.BasicCommand;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class PunishmentType implements Listener {
    public abstract String getId();
    public abstract Class<? extends Event> eventClass();
    public abstract Consumer<Event> handleEvent();
    public abstract TargetType targetType();
    public abstract Boolean isInstant();
    public abstract Runnable execute(Punishment p);
    public abstract String getVerb();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PunishmentType type && type.getId().equalsIgnoreCase(getId());
    }

    public Boolean doPunish(OfflinePlayer p) {
        return (targetType() == TargetType.PLAYER ? Utils.isPunished(p.getUniqueId().toString(), this) : (!p.isOnline() || Utils.isPunished(p.getPlayer().getAddress().getHostName(), this))) && (!p.isOnline() || !((Player) p).hasPermission("nexusstaff.punishment.bypass"));
    }

    public Boolean doPunish(String s) {
        if (targetType() == TargetType.IP) {
            return Utils.isPunished(s, this);
        } else {
            return Utils.isPunished(s, this) && (Utils.isOnline(UUID.fromString(s)) && Bukkit.getPlayer(s).hasPermission("nexusstaff.punishment.bypass"));
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

    public BasicCommand getCommandHandler() {
        return (commandSourceStack, args) -> {
            Utils.debug("Args: "+String.join(", ", args));
            boolean temp = getId().startsWith("temp");
            if ((temp && args.length >= 2) || (!temp && args.length >= 1)) {
                Utils.debug("Correct args count");
                String target = targetType() == TargetType.IP && Utils.isIp(args[0]) ? args[0] : Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
                long duration;
                boolean console = !(commandSourceStack.getSender() instanceof Player);
                String reason = null;
                if (temp) {
                    duration = Utils.parseTime(args[1]);
                    if (args.length > 2) {
                        List<String> argsList = new ArrayList<>(Arrays.asList(args));
                        argsList.removeFirst();
                        argsList.removeFirst();

                        reason = Strings.join(argsList, ' ');
                    }
                } else {
                    duration = -1L;
                    if (args.length > 1) {
                        List<String> argsList = new ArrayList<>(Arrays.asList(args));
                        argsList.removeFirst();

                        reason = Strings.join(argsList, ' ');
                    }
                    Utils.debug("Correct args count");
                }
                try {
                    Utils.punish(target, this, duration, reason, console ? null : commandSourceStack.getPlayerOrThrow().getUniqueId());
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (temp) {
                    Utils.sendMessage(commandSourceStack.getSender(), "temp-punish-usage", getId());
                } else {
                    Utils.sendMessage(commandSourceStack.getSender(), "punish-usage", getId());
                }
            }
        };
    }

    public boolean isPunished(String target) {
        return Utils.isPunished(target, this);
    }

    public boolean isPunished(Player p) {
        return isPunished(p.getUniqueId().toString()) && !p.hasPermission("nexusbans."+getId()+".exempt");
    }
}
