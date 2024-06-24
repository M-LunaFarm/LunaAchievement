package kr.lunaf.achievement.API;

import kr.lunaf.achievement.Classes.Achievement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class AchievementCompleteEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final UUID playerUUID;
    private final Achievement achievement;

    public AchievementCompleteEvent(UUID playerUUID, Achievement achievement) {
        this.playerUUID = playerUUID;
        this.achievement = achievement;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Achievement getAchievement() {
        return achievement;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
