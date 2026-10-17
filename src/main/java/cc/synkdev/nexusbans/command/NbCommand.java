package cc.synkdev.nexusbans.command;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.ConfigManager;
import cc.synkdev.nexusbans.manager.GuiManager;
import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.manager.Utils;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;

@CommandAlias("nb|nexusbans|bans")
public class NbCommand extends BaseCommand {
    @Subcommand("reload")
    @CommandPermission("nexusbans.command.reload")
    public void onReload(CommandSender sender) {
        long time = System.currentTimeMillis();
        ConfigManager.load();
        GuiManager.reloadGuis(NexusBans.getInstance());
        Utils.sendMessage(sender, "reloaded", (System.currentTimeMillis() - time) + "");
    }

    @Subcommand("debug")
    @CommandPermission("nexusbans.command.debug")
    public void onDebug(CommandSender sender) {
        if (!ConfigManager.isDebug()) {
            Utils.sendMessageLiteral(sender, "<dark_red>Debug mode is disabled on the server, therefore you cannot use this command.");
            return;
        }

        Utils.sendMessageLiteral(sender, "<green>The debug report has been pasted in the console.");
        Utils.debug("Punishment map:");
        PunishmentManager.getPunishMap().values().forEach(punishmentProfile -> {
            Utils.debug("Target: " + punishmentProfile.getTarget());
            Utils.debug("  " + String.join(" | ", punishmentProfile.getPunishmentMap().values().stream()
                    .map(punishment -> "UUID: " + punishment.getUuid().toString() + ", Type: " + punishment.getType().getId() + ", Executor: " + punishment.getExecutor() + ", Reason: " + punishment.getReason() + ", Expiry: " + punishment.getExpiry() + ", Timestamp: " + punishment.getTimestamp()).toList()));
        });
    }
}
