package cc.synkdev.nexusbans.object.storage;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.IpManager;
import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentProfile;
import cc.synkdev.nexusbans.object.StorageType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileStorage extends StorageType {
    @Override
    public String getId() {
        return "file";
    }

    private final File dataFolder = new File(NexusBans.getInstance().getDataFolder(), "data");
    private final File punishFile = new File(dataFolder, "punishments.json");
    private final File ipFile = new File(dataFolder, "ips.json");

    @Override
    public void init() {
        if (!punishFile.exists()) {
            dataFolder.mkdirs();
            try {
                Files.writeString(punishFile.toPath(), "[]");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!ipFile.exists()) {
            dataFolder.mkdirs();
            try {
                Files.writeString(ipFile.toPath(), "[]");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void loadAll() {
        try {
            PunishmentManager.getPunishMap().clear();
            JSONArray array = new JSONArray(Files.readString(punishFile.toPath()));
            for (Object o : array) {
                if (!(o instanceof JSONObject obj)) {
                    continue;
                }
                PunishmentManager.getPunishMap().put(obj.getString("target"), new PunishmentProfile(obj));
            }

            IpManager.getIpMap().clear();
            JSONArray ipArray = new JSONArray(Files.readString(ipFile.toPath()));
            for (Object o : ipArray) {
                if (!(o instanceof JSONObject obj)) continue;
                UUID uuid = UUID.fromString(obj.getString("uuid"));
                List<String> ips = new ArrayList<>();
                obj.getJSONArray("ips").forEach(o1 -> ips.add((String) o1));
                IpManager.getIpMap().put(uuid, ips);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Punishment p) {
        JSONArray array = new JSONArray();
        PunishmentManager.getPunishMap().values().forEach(pp -> array.put(pp.export()));
        try {
            Files.writeString(punishFile.toPath(), array.toString(2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(UUID uuid, List<String> ipList) {
        JSONArray array = new JSONArray();
        IpManager.getIpMap().forEach((uuid1, strings) -> {
            JSONObject obj = new JSONObject();
            obj.put("uuid", uuid1.toString());
            obj.put("ips", new JSONArray(strings));
            array.put(obj);
        });
        try {
            Files.writeString(ipFile.toPath(), array.toString(2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
