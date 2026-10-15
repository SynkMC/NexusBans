package cc.synkdev.nexusbans.object;

import cc.synkdev.nexusbans.manager.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter @AllArgsConstructor
public class Punishment {
    private UUID uuid;
    private PunishmentType type;
    private String target;
    private UUID executor;
    private String reason;
    private long expiry;
    private long timestamp;

    public Punishment(PunishmentType type, String target, UUID executor, String reason, long expiry) {
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.target = target;
        this.executor = executor;
        this.reason = reason;
        this.expiry = expiry;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isActive() {
        return System.currentTimeMillis() < getExpiry() || getExpiry() == -1L;
    }
    public String getName() {
        return type.targetType() == TargetType.IP ? target : Utils.getName(UUID.fromString(getTarget()));
    }
}
