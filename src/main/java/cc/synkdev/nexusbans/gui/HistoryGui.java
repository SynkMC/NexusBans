package cc.synkdev.nexusbans.gui;

import cc.synkdev.gui.builder.item.ItemBuilder;
import cc.synkdev.gui.guis.Gui;
import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.manager.Utils;
import cc.synkdev.nexusbans.object.PluginGui;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class HistoryGui extends PluginGui {
    public void open(Player p, String target, int page) {
        YamlConfiguration config = getConfig();
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .rows(config.getInt("rows"))
                .title(Utils.minify(config.getString("title")))
                .create();

        Map<UUID, Punishment> map = PunishmentManager.getPunishMap().getOrDefault(target, new PunishmentProfile(target)).getPunishmentMap();

        int pageSize = 5*9;
        int min = ((page-1)*pageSize);
        int maxPage = (map.size()+pageSize-1) / pageSize;

        gui.getFiller().fill(ItemBuilder.from(Material.valueOf(config.getString("filler"))).name(Component.text(" ")).asGuiItem());

        if (page > 1) {
            ConfigurationSection prev = config.getConfigurationSection("items").getConfigurationSection("prevpage");
            gui.setItem(prev.getInt("row"), prev.getInt("col"), ItemBuilder.from(Material.valueOf(prev.getString("type")))
                    .name(Utils.minify(prev.getString("name"))).lore(Utils.componentifyList(prev.getStringList("lore")))
                    .asGuiItem(event -> open((Player) event.getWhoClicked(), target, page - 1)));
        }

        if (page < maxPage) {
            ConfigurationSection next = config.getConfigurationSection("items").getConfigurationSection("nextpage");
            gui.setItem(next.getInt("row"), next.getInt("col"), ItemBuilder.from(Material.valueOf(next.getString("type")))
                    .name(Utils.minify(next.getString("name"))).lore(Utils.componentifyList(next.getStringList("lore")))
                    .asGuiItem(event -> open((Player) event.getWhoClicked(), target, page + 1)));
        }

        ConfigurationSection close = config.getConfigurationSection("items").getConfigurationSection("close");
        gui.setItem(close.getInt("row"), close.getInt("col"), ItemBuilder.from(Material.valueOf(close.getString("type")))
                .name(Utils.minify(close.getString("name"))).lore(Utils.componentifyList(close.getStringList("lore")))
                .asGuiItem(event -> event.getWhoClicked().closeInventory()));

        if (map.isEmpty()) {
            ConfigurationSection empty = config.getConfigurationSection("items").getConfigurationSection("empty");
            gui.setItem(empty.getInt("row"), empty.getInt("col"), ItemBuilder.from(Material.valueOf(empty.getString("type")))
                    .name(Utils.minify(empty.getString("name"))).lore(Utils.translateLiteralList(empty.getStringList("lore"), Utils.getNameOrIP(target)))
                    .asGuiItem());
        } else {
            for (int i = 0; i < pageSize; i++) {
                if (map.size() <= i + min) break;
                Punishment punish = map.values().stream().toList().get(i + min);
                Utils.debug("Expiry: "+punish.getExpiry()+" "+Utils.formatTimestamp(punish.getExpiry()));
                Utils.debug("Timestamp: "+punish.getTimestamp()+" "+Utils.formatTimestamp(punish.getTimestamp()));
                gui.setItem(i, ItemBuilder.from(Material.valueOf(config.getString("items.punishment.type")))
                        .name(Utils.translateLiteral(config.getString("items.punishment.name"), punish.getName(), punish.getType().getId()))
                        .lore(Utils.translateLiteralList(
                                config.getStringList("items.punishment.lore"),
                                punish.getType().getId(),
                                punish.getExecutor() == null ? "Console" : Utils.getName(punish.getExecutor()),
                                punish.getReason() == null ? "None" : punish.getReason(),
                                Utils.formatTimestamp(punish.getExpiry()),
                                Utils.formatTimestamp(punish.getTimestamp())))
                        .asGuiItem());
            }
        }

        gui.open(p);
    }

    @Override
    public String getId() {
        return "history";
    }
}
