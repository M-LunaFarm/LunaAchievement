package kr.lunaf.achievement;

import kr.lunaf.achievement.API.AchievementCompleteEvent;
import kr.lunaf.achievement.Classes.Achievement;
import kr.lunaf.achievement.Classes.PlayerAchievement;
import kr.lunaf.achievement.Database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;

public class AchievementManager {

    private static final Map<String, Achievement> achievements = new LinkedHashMap<>();
    private static final Map<UUID, Map<String, PlayerAchievement>> playerAchievements = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> changedAchievements = new HashMap<>();

    public static void loadAchievements(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "achievements.yml");
        if (!file.exists()) {
            plugin.saveResource("achievements.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, Achievement> newAchievements = new LinkedHashMap<>();
        for (String key : config.getKeys(false)) {
            String type = config.getString(key + ".type");
            int amount = config.getInt(key + ".amount");
            String display = ChatColor.translateAlternateColorCodes('&', config.getString(key + ".display", "&f"));
            Material material = Material.matchMaterial(config.getString(key + ".icon.type", "BARRIER"));
            ItemStack icon = new ItemStack(material != null ? material : Material.BARRIER);
            if (config.contains(key + ".icon.model")) {
                ItemMeta meta = icon.getItemMeta();
                if (meta != null) {
                    meta.setCustomModelData(config.getInt(key + ".icon.model"));
                    icon.setItemMeta(meta);
                }
            }
            newAchievements.put(key, new Achievement(key, type, amount, display, icon));
        }

        if (newAchievements.isEmpty()) {
            newAchievements.put("apple-1", new Achievement("apple-1", "apple", 30, ChatColor.translateAlternateColorCodes('&', "&f사과 30개 먹기"), new ItemStack(Material.APPLE)));
            newAchievements.put("apple-2", new Achievement("apple-2", "apple", 50, ChatColor.translateAlternateColorCodes('&', "&f사과 50개 먹기"), new ItemStack(Material.APPLE)));
        }

        achievements.clear();
        achievements.putAll(newAchievements);

        loadPlayerAchievements();
    }

    private static void loadPlayerAchievements() {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM player_achievements";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                String achievementName = rs.getString("achievement_name");
                boolean isComplete = rs.getBoolean("is_complete");
                Timestamp completeDate = rs.getTimestamp("complete_date");
                int completeOrder = rs.getInt("complete_order");

                PlayerAchievement playerAchievement = new PlayerAchievement(playerUUID, achievementName);
                playerAchievement.setComplete(isComplete);
                playerAchievement.setCompleteDate(completeDate);
                playerAchievement.setCompleteOrder(completeOrder);

                playerAchievements.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(achievementName, playerAchievement);
                if (isComplete) {
                    achievements.get(achievementName).addAchievedPlayer(playerUUID.toString());
                }
            }
            ps.close();

            query = "SELECT * FROM player_achievements_data";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                String achievementType = rs.getString("achievement_type");
                int amount = rs.getInt("amount");

                for (Achievement achievement : achievements.values()) {
                    if (achievement.getType().equals(achievementType)) {
                        PlayerAchievement playerAchievement = playerAchievements.computeIfAbsent(playerUUID, k -> new HashMap<>()).computeIfAbsent(achievement.getName(), k -> new PlayerAchievement(playerUUID, achievement.getName()));
                        playerAchievement.setProgress(amount);

                        if (amount >= achievement.getAmount() && !playerAchievement.isComplete()) {
                            completeAchievement(playerUUID, achievement.getName(), amount);
                        }
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

        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "INSERT INTO achievement_log (player_uuid, achievement_name, achieved_at) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, playerUUID.toString());
            ps.setString(2, achievementName);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().callEvent(new AchievementCompleteEvent(playerUUID, achievement));

        changedAchievements.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(achievementName, value);
    }

    public static void saveChangedAchievements() {
        try {
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            String updateAchievementsQuery = "UPDATE player_achievements SET is_complete = ?, complete_date = ?, complete_order = ? WHERE player_uuid = ? AND achievement_name = ?";
            String insertAchievementsQuery = "INSERT INTO player_achievements (player_uuid, achievement_name, is_complete, complete_date, complete_order) VALUES (?, ?, ?, ?, ?)";

            String updateAchievementsDataQuery = "UPDATE player_achievements_data SET amount = ? WHERE player_uuid = ? AND achievement_type = ?";
            String insertAchievementsDataQuery = "INSERT INTO player_achievements_data (player_uuid, achievement_type, amount) VALUES (?, ?, ?)";

            for (UUID playerUUID : changedAchievements.keySet()) {
                for (Map.Entry<String, Integer> entry : changedAchievements.get(playerUUID).entrySet()) {
                    String achievementName = entry.getKey();
                    int value = entry.getValue();
                    PlayerAchievement playerAchievement = playerAchievements.get(playerUUID).get(achievementName);
                    Achievement achievement = achievements.get(achievementName);

                    String checkAchievementsQuery = "SELECT COUNT(*) FROM player_achievements WHERE player_uuid = ? AND achievement_name = ?";
                    PreparedStatement checkPs = conn.prepareStatement(checkAchievementsQuery);
                    checkPs.setString(1, playerUUID.toString());
                    checkPs.setString(2, achievementName);
                    ResultSet rs = checkPs.executeQuery();
                    rs.next();
                    int count = rs.getInt(1);
                    checkPs.close();

                    if (count > 0) {
                        PreparedStatement ps = conn.prepareStatement(updateAchievementsQuery);
                        ps.setBoolean(1, playerAchievement.isComplete());
                        ps.setTimestamp(2, playerAchievement.getCompleteDate());
                        ps.setInt(3, playerAchievement.getCompleteOrder());
                        ps.setString(4, playerUUID.toString());
                        ps.setString(5, achievementName);
                        ps.addBatch();
                    } else {
                        PreparedStatement ps = conn.prepareStatement(insertAchievementsQuery);
                        ps.setString(1, playerUUID.toString());
                        ps.setString(2, achievementName);
                        ps.setBoolean(3, playerAchievement.isComplete());
                        ps.setTimestamp(4, playerAchievement.getCompleteDate());
                        ps.setInt(5, playerAchievement.getCompleteOrder());
                        ps.addBatch();
                    }

                    String checkAchievementsDataQuery = "SELECT COUNT(*) FROM player_achievements_data WHERE player_uuid = ? AND achievement_type = ?";
                    checkPs = conn.prepareStatement(checkAchievementsDataQuery);
                    checkPs.setString(1, playerUUID.toString());
                    checkPs.setString(2, achievement.getType());
                    rs = checkPs.executeQuery();
                    rs.next();
                    count = rs.getInt(1);
                    checkPs.close();

                    if (count > 0) {
                        PreparedStatement ps = conn.prepareStatement(updateAchievementsDataQuery);
                        ps.setInt(1, value);
                        ps.setString(2, playerUUID.toString());
                        ps.setString(3, achievement.getType());
                        ps.addBatch();
                    } else {
                        PreparedStatement ps = conn.prepareStatement(insertAchievementsDataQuery);
                        ps.setInt(1, value);
                        ps.setString(2, playerUUID.toString());
                        ps.setString(3, achievement.getType());
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

    public static void addValue(UUID playerUUID, String achievementType, int amount) {
        try {
            Connection conn = DatabaseManager.getConnection();
            String query = "SELECT amount FROM player_achievements_data WHERE player_uuid = ? AND achievement_type = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, playerUUID.toString());
            ps.setString(2, achievementType);
            ResultSet rs = ps.executeQuery();
            int currentAmount = 0;
            if (rs.next()) {
                currentAmount = rs.getInt("amount");
            }
            rs.close();
            ps.close();

            int newAmount = currentAmount + amount;
            changedAchievements.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(achievementType, newAmount);

            for (Achievement achievement : achievements.values()) {
                if (achievement.getType().equals(achievementType)) {
                    PlayerAchievement playerAchievement = playerAchievements.computeIfAbsent(playerUUID, k -> new HashMap<>()).computeIfAbsent(achievement.getName(), k -> new PlayerAchievement(playerUUID, achievement.getName()));
                    playerAchievement.setProgress(newAmount);

                    if (newAmount >= achievement.getAmount() && !playerAchievement.isComplete()) {
                        completeAchievement(playerUUID, achievement.getName(), newAmount);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Achievement getAchievement(String name) {
        return achievements.get(name);
    }

    public static List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements.values());
    }

    public static Set<String> getAchievementNames() {
        return achievements.keySet();
    }

    public static Map<String, PlayerAchievement> getPlayerAchievements(UUID playerUUID) {
        return playerAchievements.getOrDefault(playerUUID, new HashMap<>());
    }

    public static PlayerAchievement getPlayerProgress(UUID playerUUID, String achievementName) {
        return playerAchievements.getOrDefault(playerUUID, new HashMap<>()).get(achievementName);
    }
}
