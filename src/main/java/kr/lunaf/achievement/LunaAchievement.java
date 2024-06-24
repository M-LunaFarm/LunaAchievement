package kr.lunaf.achievement;

import kr.lunaf.achievement.Database.DatabaseManager;
import kr.lunaf.achievement.commands.ReloadAchievementsCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class LunaAchievement extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DatabaseManager.initializeDatabase();
        AchievementManager.loadAchievements(this);

        getCommand("reloadachievements").setExecutor(new ReloadAchievementsCommand(this));

        new BukkitRunnable() {
            @Override
            public void run() {
                AchievementManager.saveChangedAchievements();
            }
        }.runTaskTimer(this, 6000L, 6000L); // 20*300
    }

    @Override
    public void onDisable() {
        AchievementManager.saveChangedAchievements();
        DatabaseManager.closeDatabase();
    }
}
