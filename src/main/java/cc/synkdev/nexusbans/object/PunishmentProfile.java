package cc.synkdev.nexusbans.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
}
