package cc.synkdev.nexusbans.object.storage;

import cc.synkdev.nexusbans.manager.ConfigManager;
import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.manager.Utils;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentProfile;
import cc.synkdev.nexusbans.object.StorageType;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.*;

public class MongoDB extends StorageType {
    @Override
    public String getId() {
        return "MONGODB";
    }

    @Override
    public void loadAll() {
        MongoCollection<Document> collection = getDb().getCollection("nb_punishments");

        Map<String, PunishmentProfile> profileMap = new HashMap<>();
        collection.find().forEach(document -> {
            String target = document.getString("target");
            PunishmentProfile prof = profileMap.getOrDefault(target, new PunishmentProfile(target));
            UUID uuid = UUID.fromString(document.getString("_id"));
            prof.getPunishmentMap().put(uuid, new Punishment(uuid,
                    Utils.getPunishmentType(document.getString("type")),
                    target,
                    document.getString("executor").isEmpty() ? null : UUID.fromString(document.getString("executor")),
                    document.getString("reason").isEmpty() ? null : document.getString("reason"),
                    document.getLong("expiry"),
                    document.getLong("timestamp")));

            profileMap.put(target, prof);
        });

        PunishmentManager.getPunishMap().clear();
        PunishmentManager.getPunishMap().putAll(profileMap);

        collection = getDb().getCollection("nb_ips");
        Map<UUID, List<String>> ipMap = new HashMap<>();
        collection.find().forEach(document -> {
            UUID uuid = UUID.fromString(document.getString("uuid"));
            List<String> list = ipMap.getOrDefault(uuid, new ArrayList<>());
            list.add(document.getString("ip"));

            ipMap.put(uuid, list);
        });
    }

    @Override
    public void save(Punishment p) {
        MongoCollection<Document> collection = getDb().getCollection("nb_punishments");
        Document doc = p.exportDocument();
        collection.replaceOne(Filters.eq("_id", p.getUuid().toString()), doc, new ReplaceOptions().upsert(true));
    }

    @Override
    public void save(UUID uuid, List<String> ipList) {
        MongoCollection<Document> collection = getDb().getCollection("nb_ips");
        for (String ip : ipList) {
            Document doc = new Document("uuid", uuid.toString())
                    .append("ip", ip);
            collection.replaceOne(Filters.and(Filters.eq("uuid", uuid.toString()), Filters.eq("ip", ip)), doc, new ReplaceOptions().upsert(true));
        }
    }

    @Override
    public void init() {
        MongoDatabase db = getDb();
        db.createCollection("nb_punishments");
        db.createCollection("nb_ips");
    }

    private String getConnString() {
        return "mongodb+srv://" + ConfigManager.getDb().getString("user") + ":" + ConfigManager.getDb().getString("password") + "@" + ConfigManager.getDb().getString("host") + "/";
    }

    private MongoDatabase getDb() {
        return MongoClients.create(getConnString()).getDatabase(Objects.requireNonNull(ConfigManager.getDb().getString("database")));
    }
}
