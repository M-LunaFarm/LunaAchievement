package kr.lunaf.achievement.commands;

import kr.lunaf.achievement.AchievementManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AchievementsCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;

    public AchievementsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("achievementplugin.use")) {
            sender.sendMessage("You do not have permission to execute this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /achievements <reload|list|player>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            handleReload(sender);
        } else if (args[0].equalsIgnoreCase("list")) {
            handleList(sender, args);
        } else if (args[0].equalsIgnoreCase("player")) {
            handlePlayer(sender, args);
        } else {
            sender.sendMessage("Usage: /achievements <reload|list|player>");
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("achievementplugin.reload")) {
            sender.sendMessage("You do not have permission to execute this command.");
            return;
        }

        AchievementManager.saveChangedAchievements();
        AchievementManager.loadAchievements(plugin);
        sender.sendMessage(plugin.getDescription().getName() + ": Achievements reloaded from achievements.yml");
    }

    private void handleList(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return;
        }

        Player player = (Player) sender;
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid page number.");
                return;
            }
        }

        AchievementGUI.showAchievementList(player, page);
    }

    private void handlePlayer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /achievements player <playername>");
            return;
        }

        Player player = (Player) sender;
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        int page = 1;
        if (args.length > 2) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid page number.");
                return;
            }
        }

        AchievementGUI.showPlayerAchievements(player, targetPlayer, page);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("achievementplugin.use")) {
            return null;
        }

        if (args.length == 1) {
            return Arrays.asList("reload", "list", "player");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("player")) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }

        return null;
    }
}
