package kr.lunaf.achievement.API;

import kr.lunaf.achievement.AchievementManager;
import kr.lunaf.achievement.Classes.Achievement;
import kr.lunaf.achievement.Classes.PlayerAchievement;
import org.bukkit.entity.Player;

import java.util.Map;

public class AchievementAPI {

    public static void addValue(Player player, String type, int amount) {
        AchievementManager.addValue(player.getUniqueId(), type, amount);
    }

    public static Achievement getAchievement(String name) {
        return AchievementManager.getAchievement(name);
    }

    public static Map<String, PlayerAchievement> getPlayerInformation(Player player) {
        return AchievementManager.getPlayerAchievements(player.getUniqueId());
    }

    public static PlayerAchievement getPlayerProgress(Player player, String achievementName) {
        return AchievementManager.getPlayerProgress(player.getUniqueId(), achievementName);
    }
}
