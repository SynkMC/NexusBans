package cc.synkdev.nexusbans.object;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.ConfigManager;
import cc.synkdev.nexusbans.manager.IpManager;
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

import java.util.*;
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
            return Utils.isPunished(s, this) && (Utils.isOnline(UUID.fromString(s)) && !Bukkit.getPlayer(UUID.fromString(s)).hasPermission("nexusstaff.punishment.bypass"));
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
        if (doPunish(punishment.getTarget()) && execute(punishment) != null) execute(punishment).run();
    }

    public BasicCommand getCommandHandler() {
        return (commandSourceStack, args) -> {
            Utils.debug("Args: "+String.join(", ", args));
            boolean temp = getId().startsWith("temp");
            if ((temp && args.length >= 2) || (!temp && args.length >= 1)) {
                Utils.debug("Correct args count");
                List<String> target = new ArrayList<>();
                if (targetType() == TargetType.IP) {
                    if (Utils.isIp(args[0])) {
                        target.add(args[0]);
                    } else {
                        OfflinePlayer oP = Bukkit.getOfflinePlayer(args[0]);
                        if (ConfigManager.isBanAllIps()) {
                            target.addAll(IpManager.getIpMap().get(oP.getUniqueId()));
                        } else {
                            target.add(oP.getUniqueId().toString());
                        }
                    }
                } else {
                    target.add(Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString());
                }
                long duration;
                boolean console = !(commandSourceStack.getSender() instanceof Player);
                String reason;
                if (temp) {
                    duration = System.currentTimeMillis()+Utils.parseTime(args[1]);
                    if (args.length > 2) {
                        List<String> argsList = new ArrayList<>(Arrays.asList(args));
                        argsList.removeFirst();
                        argsList.removeFirst();

                        reason = Strings.join(argsList, ' ');
                    } else {
                        reason = null;
                    }
                } else {
                    duration = isInstant() ? System.currentTimeMillis()+1000L : -1L;
                    if (args.length > 1) {
                        List<String> argsList = new ArrayList<>(Arrays.asList(args));
                        argsList.removeFirst();

                        reason = Strings.join(argsList, ' ');
                    } else {
                        reason = null;
                    }
                    Utils.debug("Correct args count. Reason "+reason);
                }
                target.forEach(s -> {
                    try {
                        Utils.punish(s, this, duration, reason, console ? null : commandSourceStack.getPlayerOrThrow().getUniqueId());
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                if (temp) {
                    Utils.sendMessage(commandSourceStack.getSender(), "temp-punish-usage", getId());
                } else {
                    Utils.sendMessage(commandSourceStack.getSender(), "punish-usage", getId());
                }
            }
        };
    }

    public BasicCommand getUnCommandHandler() {
        return isInstant() ? null : (commandSourceStack, args) -> {
            if (args.length < 1) {
                Utils.sendMessage(commandSourceStack.getSender(), "un-usage");
                return;
            }

            String target = args[0];
            if (!isPunished(target)) {
                Utils.sendMessage(commandSourceStack.getSender(), "not-punished", target, getVerb());
            }

            if (targetType() == TargetType.IP) {
                if (Utils.isIp(target)) {
                    Utils.unpunish(target, this);
                } else {
                    for (String s : IpManager.getIpMap().getOrDefault(Bukkit.getOfflinePlayer(target).getUniqueId(), new ArrayList<>())) {
                        Utils.unpunish(s, this);
                    }
                }
            } else {
                Utils.unpunish(Bukkit.getOfflinePlayer(target).getUniqueId().toString(), this);
            }

        };
    }

    public boolean isPunished(String target) {
        return Utils.isPunished(target, this);
    }

    public boolean isPunished(Player p) {
        return (isPunished(p.getUniqueId().toString()) || isPunished(p.getAddress().getHostName())) && !p.hasPermission("nexusbans."+getId()+".exempt");
    }
}
