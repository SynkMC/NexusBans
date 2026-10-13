package cc.synkdev.nexusbans.manager;

import cc.synkdev.nexusbans.object.PunishmentProfile;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PunishmentManager {
    private PunishmentManager() {
        /* This utility class should not be instantiated */
    }

    @Getter private static final Map<String, PunishmentProfile> punishMap = new HashMap<>();
}
