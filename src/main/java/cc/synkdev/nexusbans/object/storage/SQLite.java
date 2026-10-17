package cc.synkdev.nexusbans.object.storage;

import cc.synkdev.nexusbans.NexusBans;
import cc.synkdev.nexusbans.manager.PunishmentManager;
import cc.synkdev.nexusbans.manager.Utils;
import cc.synkdev.nexusbans.object.Punishment;
import cc.synkdev.nexusbans.object.PunishmentProfile;
import cc.synkdev.nexusbans.object.StorageType;

import java.io.File;
import java.sql.*;
import java.util.*;

public class SQLite extends StorageType {

    private final File dataFolder = new File(NexusBans.getInstance().getDataFolder(), "data");
    private final File dbFile = new File(dataFolder, "database.db");

    @Override
    public String getId() {
        return "SQLITE";
    }

    @Override
    public void loadAll() {
        try (Connection conn = getConnection()) {
            String selPunish = "SELECT * FROM `nb_punishments`";
            PreparedStatement prepPunish = conn.prepareStatement(selPunish);
            ResultSet resPunish = prepPunish.executeQuery();
            Map<String, PunishmentProfile> profileMap = new HashMap<>();
            while (resPunish.next()) {
                String target = resPunish.getString("target");
                PunishmentProfile prof = profileMap.getOrDefault(target, new PunishmentProfile(target));
                UUID uuid = UUID.fromString(resPunish.getString("uuid"));
                prof.getPunishmentMap().put(uuid, new Punishment(uuid,
                        Utils.getPunishmentType(resPunish.getString("type")),
                        target,
                        resPunish.getString("executor") == null ? null : UUID.fromString(resPunish.getString("executor")),
                        resPunish.getString("reason"),
                        resPunish.getLong("expiry"),
                        resPunish.getLong("timestamp")));

                profileMap.put(target, prof);
            }
            PunishmentManager.getPunishMap().clear();
            PunishmentManager.getPunishMap().putAll(profileMap);

            String selIP = "SELECT * FROM `nb_ips`";
            PreparedStatement prepIP = conn.prepareStatement(selIP);
            ResultSet resIP = prepIP.executeQuery();
            Map<UUID, List<String>> ipMap = new HashMap<>();
            while (resIP.next()) {
                UUID uuid = UUID.fromString(resIP.getString("uuid"));
                List<String> list = ipMap.getOrDefault(uuid, new ArrayList<>());
                list.add(resIP.getString("ip"));

                ipMap.put(uuid, list);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void save(Punishment p) {
        String insert = "INSERT INTO `nb_punishments` (`uuid`, `type`, `target`, `executor`, `reason`, `expiry`, `timestamp`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(uuid) DO " +
                "UPDATE SET `type` = excluded.`type`, `target` = excluded.`target`, `executor` = excluded.`executor`, " +
                "`reason` = excluded.`reason`, `expiry` = excluded.`expiry`, `timestamp` = excluded.`timestamp`";
        try (Connection conn = getConnection()) {
            PreparedStatement prep = conn.prepareStatement(insert);
            prep.setString(1, p.getUuid().toString());
            prep.setString(2, p.getType().getId());
            prep.setString(3, p.getTarget());
            prep.setString(4, p.getExecutor() == null ? null : p.getExecutor().toString());
            prep.setString(5, p.getReason());
            prep.setLong(6, p.getExpiry());
            prep.setLong(7, p.getTimestamp());

            prep.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(UUID uuid, List<String> ipList) {
        String insert = "INSERT INTO `nb_ips` (uuid, ip) VALUES (?, ?) " +
                "ON CONFLICT(uuid, ip) DO NOTHING";
        try (Connection conn = getConnection()) {
            PreparedStatement prep = conn.prepareStatement(insert);
            for (String ip : ipList) {
                prep.setString(1, uuid.toString());
                prep.setString(2, ip);
                prep.addBatch();
            }
            prep.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
        String punishments = "create table if not exists nb_punishments" +
                "(" +
                "    uuid      VARCHAR(36)  not null" +
                "        primary key," +
                "    type      VARCHAR(255) null," +
                "    target    VARCHAR(36)  null," +
                "    executor  VARCHAR(36)  null," +
                "    reason    VARCHAR(255) null," +
                "    expiry    BIGINT(13)         null," +
                "    timestamp BIGINT(13)         null," +
                "    constraint nb_punishments_pk" +
                "        unique (uuid)" +
                ");";


        String ips = "create table if not exists nb_ips" +
                "(" +
                "    id   int auto_increment" +
                "        primary key," +
                "    uuid VARCHAR(36) null," +
                "    ip   varchar(45) null," +
                "    constraint uniq_uuid_ip unique (uuid, ip)" +
                ");";

        try (Connection conn = getConnection()) {
            PreparedStatement prepPunish = conn.prepareStatement(punishments);
            prepPunish.execute();

            PreparedStatement prepIp = conn.prepareStatement(ips);
            prepIp.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getConnString() {
        return "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getConnString());
    }
}
