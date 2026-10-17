package cc.synkdev.nexusbans.object;

import java.util.List;
import java.util.UUID;

public abstract class StorageType {
    public abstract String getId();

    public abstract void loadAll();

    public abstract void save(Punishment p);

    public abstract void save(UUID uuid, List<String> ipList);

    public abstract void init();
}
