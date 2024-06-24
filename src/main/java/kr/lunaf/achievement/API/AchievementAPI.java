package kr.lunaf.achievement.API;

import kr.lunaf.achievement.AchievementManager;
import kr.lunaf.achievement.Classes.Achievement;
import kr.lunaf.achievement.Classes.PlayerAchievement;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AchievementAPI {

    public static void addValue(UUID playerUUID, String achievementType, int amount) {
        AchievementManager.addValue(playerUUID, achievementType, amount);
    }

    public static Achievement getAchievement(String name) {
        return AchievementManager.getAchievement(name);
    }

    public static PlayerAchievement getPlayerProgress(UUID playerUUID, String achievementName) {
        return AchievementManager.getPlayerProgress(playerUUID, achievementName);
    }

    public static Map<String, PlayerAchievement> getPlayerAchievements(UUID playerUUID) {
        return AchievementManager.getPlayerAchievements(playerUUID);
    }

    public static List<Achievement> getAllAchievements() {
        return AchievementManager.getAllAchievements();
    }
}
