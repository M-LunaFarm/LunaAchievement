package kr.lunaf.achievement;

import kr.lunaf.achievement.Database.DatabaseManager;
import kr.lunaf.achievement.commands.AchievementGUI;
import kr.lunaf.achievement.commands.AchievementsCommand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class LunaAchievement extends JavaPlugin {

    private static LunaAchievement instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        DatabaseManager.initializeDatabase();
        AchievementManager.loadAchievements(this);

        getServer().getPluginManager().registerEvents(new AchievementGUI(), this);
        getCommand("achievements").setExecutor(new AchievementsCommand(this));
        getCommand("achievements").setTabCompleter(new AchievementsCommand(this));

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
    public static LunaAchievement getInstance() {
        return instance;
    }

    public static void setMetadata(Player player, String key, Object value) {
        player.setMetadata(key, new FixedMetadataValue(instance, value));
    }

    public static Object getMetadata(Player player, String key) {
        if (player.hasMetadata(key)) {
            return player.getMetadata(key).get(0).value();
        }
        return null;
    }
}
