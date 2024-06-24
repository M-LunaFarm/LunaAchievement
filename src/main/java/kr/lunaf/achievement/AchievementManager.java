package kr.lunaf.achievement;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kr.lunaf.achievement.API.AchievementCompleteEvent;
import kr.lunaf.achievement.Classes.Achievement;
import kr.lunaf.achievement.Classes.PlayerAchievement;
import kr.lunaf.achievement.Database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class AchievementManager {

    private static final Map<String, Achievement> achievements = new HashMap<>();
    private static final Map<UUID, Map<String, PlayerAchievement>> playerAchievements = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> changedAchievements = new HashMap<>();

    public static void loadAchievements(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "achievements.yml");
        if (!file.exists()) {
            plugin.saveResource("achievements.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, Achievement> newAchievements = new HashMap<>();
        for (String key : config.getKeys(false)) {
            String type = config.getString(key + ".type");
            int amount = config.getInt(key + ".amount");
            String display = config.getString(key + ".display");
            newAchievements.put(key, new Achievement(key, type, amount, display));
        }

        if (newAchievements.isEmpty()) {
            newAchievements.put("apple-1", new Achievement("apple-1", "apple", 30, "사과 30개 먹기"));
            newAchievements.put("apple-2", new Achievement("apple-2", "apple", 50, "사과 50개 먹기"));
        }

        achievements.clear();
        achievements.putAll(newAchievements);

        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM player_achievements";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                String achievementName = rs.getString("achievement_name");
                int value = rs.getInt("value");
                boolean isComplete = rs.getBoolean("is_complete");
                Timestamp completeDate = rs.getTimestamp("complete_date");
                int completeOrder = rs.getInt("complete_order");

                PlayerAchievement playerAchievement = new PlayerAchievement(playerUUID, achievementName);
                playerAchievement.setProgress(value);
                playerAchievement.setComplete(isComplete);
                playerAchievement.setCompleteDate(completeDate);
                playerAchievement.setCompleteOrder(completeOrder);

                playerAchievements.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(achievementName, playerAchievement);
                if (isComplete) {
                    achievements.get(achievementName).addAchievedPlayer(playerUUID.toString());
                } else {
                    // 신규 업적 조건을 확인하고 자동으로 달성
                    Achievement achievement = achievements.get(achievementName);
                    if (achievement != null && value >= achievement.getAmount()) {
                        completeAchievement(playerUUID, achievementName, value);
                    }
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void completeAchievement(UUID playerUUID, String achievementName, int value) {
        Achievement achievement = achievements.get(achievementName);
        if (achievement == null) return;

        PlayerAchievement playerAchievement = playerAchievements.get(playerUUID).get(achievementName);
        if (playerAchievement == null) return;

        playerAchievement.setComplete(true);
        playerAchievement.setCompleteDate(new Timestamp(System.currentTimeMillis()));
        playerAchievement.setCompleteOrder(achievement.getAchievedPlayerCount() + 1);
        achievement.addAchievedPlayer(playerUUID.toString());

        Bukkit.getPluginManager().callEvent(new AchievementCompleteEvent(playerUUID, achievement));

        changedAchievements.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(achievementName, value);
    }

    public static void saveChangedAchievements() {
        try {
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            String updateQuery = "UPDATE player_achievements SET value = ?, is_complete = ?, complete_date = ?, complete_order = ? WHERE player_uuid = ? AND achievement_name = ?";
            String insertQuery = "INSERT INTO player_achievements (player_uuid, achievement_name, value, is_complete, complete_date, complete_order) VALUES (?, ?, ?, ?, ?, ?)";

            for (UUID playerUUID : changedAchievements.keySet()) {
                for (Map.Entry<String, Integer> entry : changedAchievements.get(playerUUID).entrySet()) {
                    String achievementName = entry.getKey();
                    int value = entry.getValue();
                    PlayerAchievement playerAchievement = playerAchievements.get(playerUUID).get(achievementName);

                    String checkQuery = "SELECT COUNT(*) FROM player_achievements WHERE player_uuid = ? AND achievement_name = ?";
                    PreparedStatement checkPs = conn.prepareStatement(checkQuery);
                    checkPs.setString(1, playerUUID.toString());
                    checkPs.setString(2, achievementName);
                    ResultSet rs = checkPs.executeQuery();
                    rs.next();
                    int count = rs.getInt(1);
                    checkPs.close();

                    if (count > 0) {
                        PreparedStatement ps = conn.prepareStatement(updateQuery);
                        ps.setInt(1, value);
                        ps.setBoolean(2, playerAchievement.isComplete());
                        ps.setTimestamp(3, playerAchievement.getCompleteDate());
                        ps.setInt(4, playerAchievement.getCompleteOrder());
                        ps.setString(5, playerUUID.toString());
                        ps.setString(6, achievementName);
                        ps.addBatch();
                    } else {
                        PreparedStatement ps = conn.prepareStatement(insertQuery);
                        ps.setString(1, playerUUID.toString());
                        ps.setString(2, achievementName);
                        ps.setInt(3, value);
                        ps.setBoolean(4, playerAchievement.isComplete());
                        ps.setTimestamp(5, playerAchievement.getCompleteDate());
                        ps.setInt(6, playerAchievement.getCompleteOrder());
                        ps.addBatch();
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addValue(UUID playerUUID, String achievementName, int amount) {
        Achievement achievement = achievements.get(achievementName);
        if (achievement == null) {
            achievement = new Achievement(achievementName, "unknown", amount, "Unknown Achievement");
            achievements.put(achievementName, achievement);
        }

        PlayerAchievement playerAchievement = playerAchievements.computeIfAbsent(playerUUID, k -> new HashMap<>()).computeIfAbsent(achievementName, k -> new PlayerAchievement(playerUUID, achievementName));
        int newValue = playerAchievement.getProgress() + amount;

        if (newValue >= achievement.getAmount() && !playerAchievement.isComplete()) {
            completeAchievement(playerUUID, achievementName, newValue);
        }

        playerAchievement.setProgress(newValue);

        changedAchievements.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(achievementName, newValue);
    }

    public static Achievement getAchievement(String name) {
        return achievements.get(name);
    }

    public static Map<String, PlayerAchievement> getPlayerAchievements(UUID playerUUID) {
        return playerAchievements.getOrDefault(playerUUID, new HashMap<>());
    }

    public static PlayerAchievement getPlayerProgress(UUID playerUUID, String achievementName) {
        return playerAchievements.getOrDefault(playerUUID, new HashMap<>()).get(achievementName);
    }
}
