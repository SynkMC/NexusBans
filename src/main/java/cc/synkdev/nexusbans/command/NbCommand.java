package cc.synkdev.nexusbans.command;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.ConfigManager;
import cc.synkdev.nexusbans.manager.GuiManager;
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
        Utils.sendMessage(sender, "reloaded", (System.currentTimeMillis()-time)+"");
    }
}
