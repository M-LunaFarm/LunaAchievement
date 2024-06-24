package kr.lunaf.achievement.Classes;

import java.sql.Timestamp;
import java.util.UUID;

public class PlayerAchievement {
    private UUID playerUUID;
    private String achievementName;
    private int progress;
    private boolean isComplete;
    private Timestamp completeDate;
    private int completeOrder;

    public PlayerAchievement(UUID playerUUID, String achievementName) {
        this.playerUUID = playerUUID;
        this.achievementName = achievementName;
        this.progress = 0;
        this.isComplete = false;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getAchievementName() {
        return achievementName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public Timestamp getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Timestamp completeDate) {
        this.completeDate = completeDate;
    }

    public int getCompleteOrder() {
        return completeOrder;
    }

    public void setCompleteOrder(int completeOrder) {
        this.completeOrder = completeOrder;
    }
}
