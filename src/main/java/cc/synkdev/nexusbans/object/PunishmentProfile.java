package cc.synkdev.nexusbans.object;

import cc.synkdev.nexusbans.manager.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor
public class PunishmentProfile {
    private String target;
    private Map<UUID, Punishment> punishmentMap;

    public PunishmentProfile(String target) {
        this.target = target;
        this.punishmentMap = new HashMap<>();
    }

    public PunishmentProfile(JSONObject obj) {
        this.target = obj.getString("target");
        this.punishmentMap = new HashMap<>();
        for (Object o : obj.getJSONArray("punishments")) {
            JSONObject jObj = (JSONObject) o;
            Utils.debug("Storing punishment "+jObj.getString("uuid"));
            punishmentMap.put(UUID.fromString(jObj.getString("uuid")), new Punishment(jObj));
        }
    }

    public JSONObject export() {
        return new JSONObject(Map.of(
                "target", getTarget(),
                "punishments", new JSONArray(getPunishmentMap().values().stream().map(Punishment::export).toArray())
        ));
    }
}
