package kr.lunaf.achievement.commands;

import kr.lunaf.achievement.AchievementManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadAchievementsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ReloadAchievementsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("achievementplugin.reloadachievements")) {
            sender.sendMessage("권한 부족.");
            return true;
        }

        AchievementManager.loadAchievements(plugin);
        sender.sendMessage("로드 완료");
        return true;
    }
}
