package kr.lunaf.achievement.commands;

import kr.lunaf.achievement.Classes.Achievement;
import kr.lunaf.achievement.Classes.PlayerAchievement;
import kr.lunaf.achievement.AchievementManager;
import kr.lunaf.achievement.LunaAchievement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AchievementGUI implements Listener {

    private static final String PAGE_METADATA = "achievements_page";
    private static final String TARGET_PLAYER_METADATA = "target_player";

    public static void showAchievementList(Player player, int page) {
        List<Achievement> achievements = new ArrayList<>(AchievementManager.getAllAchievements());
        int totalAchievements = achievements.size();
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) totalAchievements / itemsPerPage);

        if (page > totalPages || page < 1) {
            player.sendMessage("Invalid page number.");
            return;
        }

        LunaAchievement.setMetadata(player, PAGE_METADATA, page);

        Inventory gui = Bukkit.createInventory(null, 54, "Achievements List - Page " + page);

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalAchievements);

        for (int i = startIndex; i < endIndex; i++) {
            Achievement achievement = achievements.get(i);
            ItemStack item = achievement.getIcon();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f" + achievement.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f타입: " + achievement.getType()));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f수량: " + achievement.getAmount()));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f" + achievement.getDisplay()));
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        if (page > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prevPage.setItemMeta(prevMeta);
            gui.setItem(48, prevPage);
        }

        ItemStack currentPage = new ItemStack(Material.PAPER);
        ItemMeta currentMeta = currentPage.getItemMeta();
        currentMeta.setDisplayName(ChatColor.GREEN + "Page " + page + " of " + totalPages);
        currentPage.setItemMeta(currentMeta);
        gui.setItem(49, currentPage);

        if (page < totalPages) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
            nextPage.setItemMeta(nextMeta);
            gui.setItem(50, nextPage);
        }

        player.openInventory(gui);
    }

    public static void showPlayerAchievements(Player player, Player targetPlayer, int page) {
        List<Achievement> achievements = new ArrayList<>(AchievementManager.getAllAchievements());
        int totalAchievements = achievements.size();
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) totalAchievements / itemsPerPage);

        if (page > totalPages || page < 1) {
            player.sendMessage("Invalid page number.");
            return;
        }

        LunaAchievement.setMetadata(player, PAGE_METADATA, page);
        LunaAchievement.setMetadata(player, TARGET_PLAYER_METADATA, targetPlayer.getUniqueId().toString());

        Inventory gui = Bukkit.createInventory(null, 54, targetPlayer.getName() + "'s Achievements - Page " + page);

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalAchievements);

        for (int i = startIndex; i < endIndex; i++) {
            Achievement achievement = achievements.get(i);
            PlayerAchievement playerAchievement = AchievementManager.getPlayerProgress(targetPlayer.getUniqueId(), achievement.getName());

            ItemStack item = achievement.getIcon();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f" + achievement.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f업적: " + achievement.getDisplay()));
            if (playerAchievement != null) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "&f진행도: " + playerAchievement.getProgress() + "/" + achievement.getAmount() + " (" + (int)((double)playerAchievement.getProgress() / achievement.getAmount() * 100) + "%)"));
                if (playerAchievement.isComplete()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&f완료일: " + sdf.format(playerAchievement.getCompleteDate()) + " (#" + playerAchievement.getCompleteOrder() + ")"));
                } else {
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&f완료일: 없음"));
                }
            } else {
                lore.add(ChatColor.translateAlternateColorCodes('&', "&f진행도: 0/" + achievement.getAmount() + " (0%)"));
                lore.add(ChatColor.translateAlternateColorCodes('&', "&f완료일: 없음"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        if (page > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prevPage.setItemMeta(prevMeta);
            gui.setItem(48, prevPage);
        }

        ItemStack currentPage = new ItemStack(Material.PAPER);
        ItemMeta currentMeta = currentPage.getItemMeta();
        currentMeta.setDisplayName(ChatColor.GREEN + "Page " + page + " of " + totalPages);
        currentPage.setItemMeta(currentMeta);
        gui.setItem(49, currentPage);

        if (page < totalPages) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
            nextPage.setItemMeta(nextMeta);
            gui.setItem(50, nextPage);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Achievements List") || event.getView().getTitle().contains("'s Achievements")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            int clickedSlot = event.getRawSlot();
            int currentPage = (int) LunaAchievement.getMetadata(player, PAGE_METADATA);

            if (clickedSlot == 48) {
                if (event.getView().getTitle().startsWith("Achievements List")) {
                    showAchievementList(player, currentPage - 1);
                } else {
                    String targetUUID = (String) LunaAchievement.getMetadata(player, TARGET_PLAYER_METADATA);
                    Player targetPlayer = Bukkit.getPlayer(UUID.fromString(targetUUID));
                    if (targetPlayer != null) {
                        showPlayerAchievements(player, targetPlayer, currentPage - 1);
                    }
                }
            } else if (clickedSlot == 50) {
                if (event.getView().getTitle().startsWith("Achievements List")) {
                    showAchievementList(player, currentPage + 1);
                } else {
                    String targetUUID = (String) LunaAchievement.getMetadata(player, TARGET_PLAYER_METADATA);
                    Player targetPlayer = Bukkit.getPlayer(UUID.fromString(targetUUID));
                    if (targetPlayer != null) {
                        showPlayerAchievements(player, targetPlayer, currentPage + 1);
                    }
                }
            }
        }
    }
}
