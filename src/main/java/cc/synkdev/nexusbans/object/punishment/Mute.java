package cc.synkdev.nexusbans.object.punishment;

import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.manager.Utils;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentType;
import cc.synkdev.nexusbans.object.TargetType;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.Event;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Mute extends PunishmentType {
    @Override
    public Class<? extends Event> eventClass() {
        return AsyncChatEvent.class;
    }

    @Override
    public Consumer<Event> handleEvent() {
        return event -> {
            AsyncChatEvent chatEvent = (AsyncChatEvent) event;
            if (isPunished(chatEvent.getPlayer())) {
                chatEvent.setCancelled(true);
                Utils.sendMessage(chatEvent.getPlayer(), "muted");
                PunishmentManager.alert(chatEvent.getPlayer(), this);
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
        return null;
    }

    @Override
    public String getVerb() {
        return "muted";
    }

    @Override
    public String getId() {
        return "mute";
    }
}
