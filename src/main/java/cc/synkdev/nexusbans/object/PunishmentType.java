package cc.synkdev.nexusbans.object;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.ConfigManager;
import cc.synkdev.nexusbans.manager.IpManager;
import cc.synkdev.nexusbans.manager.Utils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jspecify.annotations.NonNull;

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

    public Boolean doPunish(String s) {
        if (targetType() == TargetType.IP) {
            return Utils.isPunished(s, this);
        } else {
            return Utils.isPunished(s, this) && (Utils.isOnline(UUID.fromString(s)) && !Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(s))).hasPermission("nexusstaff.punishment.bypass"));
        }
    }

    public void registerListener() {
        if (eventClass() == null || handleEvent() == null) return;
        EventExecutor executor = (_, event) -> {
            if (eventClass().isInstance(event)) {
                handleEvent().accept(event);
            }
        };
        Utils.debug("Registered pType " + getId());
        Bukkit.getPluginManager().registerEvent(eventClass(), this, EventPriority.HIGH, executor, NexusBans.getInstance());
    }

    public void executePunishment(Punishment punishment) {
        if (doPunish(punishment.getTarget()) && execute(punishment) != null) execute(punishment).run();
    }

    public BasicCommand getCommandHandler() {
        return new BasicCommand() {
            @Override
            public void execute(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {
                Utils.debug("Args: " + String.join(", ", args));
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
                                target.addAll(IpManager.getIpMap().getOrDefault(oP.getUniqueId(), new ArrayList<>()));
                            } else {
                                target.add(oP.getUniqueId().toString());
                            }
                        }

                        if (target.isEmpty()) {
                            Utils.sendMessage(commandSourceStack.getSender(), "no-ip", args[0]);
                        }
                    } else {
                        target.add(Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString());
                    }
                    long duration;
                    boolean console = !(commandSourceStack.getSender() instanceof Player);
                    String reason;
                    if (temp) {
                        duration = System.currentTimeMillis() + Utils.parseTime(args[1]);
                        if (args.length > 2) {
                            List<String> argsList = new ArrayList<>(Arrays.asList(args));
                            argsList.removeFirst();
                            argsList.removeFirst();

                            reason = Strings.join(argsList, ' ');
                        } else {
                            reason = null;
                        }
                    } else {
                        duration = isInstant() ? System.currentTimeMillis() + 1000L : -1L;
                        if (args.length > 1) {
                            List<String> argsList = new ArrayList<>(Arrays.asList(args));
                            argsList.removeFirst();

                            reason = Strings.join(argsList, ' ');
                        } else {
                            reason = null;
                        }
                        Utils.debug("Correct args count. Reason " + reason);
                    }
                    target.forEach(s -> {
                        try {
                            Utils.punish(s, PunishmentType.this, duration, reason, console ? null : commandSourceStack.getPlayerOrThrow().getUniqueId());
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
            }

            @Override
            public @NonNull Collection<String> suggest(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {
                return args.length < 1 ? Bukkit.getOnlinePlayers().stream().map(Player::getName).toList() : new ArrayList<>();
            }
        };
    }

    public BasicCommand getUnCommandHandler() {
        return isInstant() ? null : new BasicCommand() {
            @Override
            public void execute(CommandSourceStack commandSourceStack, String[] args) {
                if (args.length < 1) {
                    Utils.sendMessage(commandSourceStack.getSender(), "un-usage");
                    return;
                }

                String target = args[0];
                if (!isPunished(Utils.isIp(target) ? target : Bukkit.getOfflinePlayer(target).getUniqueId().toString())) {
                    Utils.sendMessage(commandSourceStack.getSender(), "not-punished", target, getVerb());
                    return;
                }

                if (targetType() == TargetType.IP) {
                    if (Utils.isIp(target)) {
                        Utils.unpunish(target, PunishmentType.this);
                    } else {
                        for (String s : IpManager.getIpMap().getOrDefault(Bukkit.getOfflinePlayer(target).getUniqueId(), new ArrayList<>())) {
                            Utils.unpunish(s, PunishmentType.this);
                        }
                    }
                } else {
                    Utils.unpunish(Bukkit.getOfflinePlayer(target).getUniqueId().toString(), PunishmentType.this);
                }
            }

            @Override
            public @NonNull Collection<String> suggest(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {
                return args.length < 1 ? Bukkit.getOnlinePlayers().stream().map(Player::getName).toList() : new ArrayList<>();
            }
        };
    }

    public boolean isPunished(String target) {
        boolean isUUID = true;
        UUID uuid = null;
        try {
            uuid = UUID.fromString(target);
        } catch (Exception _) {
            isUUID = false;
        }
        return Utils.isPunished(target, this) || (isUUID && IpManager.getIpMap().getOrDefault(uuid, new ArrayList<>()).stream().anyMatch(this::isPunished));
    }

    public boolean isPunished(Player p) {
        return (isPunished(p.getUniqueId().toString()) || isPunished(Objects.requireNonNull(p.getAddress()).getHostName())) && !p.hasPermission("nexusbans." + getId() + ".exempt");
    }
}
