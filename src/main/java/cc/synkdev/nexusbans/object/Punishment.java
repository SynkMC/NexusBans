package cc.synkdev.nexusbans.object;

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

    public Punishment(PunishmentType type, String target, UUID executor, String reason, long expiry) {
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.target = target;
        this.executor = executor;
        this.reason = reason;
        this.expiry = expiry;
    }

    public boolean isActive() {
        return System.currentTimeMillis() < getExpiry() || getExpiry() == -1L;
    }
}
