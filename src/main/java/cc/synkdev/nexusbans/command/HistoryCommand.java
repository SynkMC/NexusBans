package cc.synkdev.nexusbans.command;

import cc.synkdev.nexusbans.gui.HistoryGui;
import cc.synkdev.nexusbans.manager.Utils;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("history|nbhistory")
public class HistoryCommand extends BaseCommand {
    @Default
    @CommandPermission("nexusbans.command.history")
    @CommandCompletion("@players")
    public void onHistory(Player p, String[] args) {
        if (args.length == 0) {
            Utils.sendMessage(p, "history-usage");
            return;
        }
        String target = Utils.isIp(args[0]) ? args[0] : Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
        new HistoryGui().open(p, target, 1);
    }
}
