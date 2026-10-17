package cc.synkdev.nexusbans.object;

import cc.synkdev.nexusbans.manager.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
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

    public Punishment(JSONObject jObj) {
        setUuid(UUID.fromString(jObj.getString("uuid")));
        setType(Utils.getPunishmentType(jObj.getString("type")));
        setTarget(jObj.getString("target"));
        setExecutor(jObj.getString("executor").isEmpty() ? null : UUID.fromString(jObj.getString("executor")));
        setReason(jObj.has("reason") ? jObj.getString("reason") : null);
        setExpiry(jObj.getLong("expiry"));
        setTimestamp(jObj.getLong("timestamp"));
    }

    public boolean isActive() {
        return System.currentTimeMillis() < getExpiry() || getExpiry() == -1L;
    }

    public String getName() {
        return type.targetType() == TargetType.IP ? target : Utils.getName(UUID.fromString(getTarget()));
    }

    public JSONObject export() {
        return new JSONObject(Map.of(
                "uuid", getUuid().toString(),
                "type", getType().getId(),
                "target", getTarget(),
                "executor", getExecutor() == null ? "" : getExecutor().toString(),
                "reason", getReason() == null ? "" : getReason(),
                "expiry", getExpiry(),
                "timestamp", getTimestamp()
        ));
    }

    public Document exportDocument() {
        return new Document("_id", getUuid().toString())
                .append("type", getType().getId())
                .append("target", getTarget())
                .append("executor", getExecutor() == null ? "" : getExecutor().toString())
                .append("reason", getReason() == null ? "" : getReason())
                .append("expiry", getExpiry())
                .append("timestamp", getTimestamp());
    }
}
