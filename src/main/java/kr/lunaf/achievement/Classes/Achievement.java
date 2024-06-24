package kr.lunaf.achievement.Classes;

import java.util.HashSet;
import java.util.Set;

public class Achievement {
    private String name;
    private String type;
    private int amount;
    private String display;
    private Set<String> achievedPlayers;

    public Achievement(String name, String type, int amount, String display) {
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.display = display;
        this.achievedPlayers = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public String getDisplay() {
        return display;
    }

    public Set<String> getAchievedPlayers() {
        return achievedPlayers;
    }

    public int getAchievedPlayerCount() {
        return achievedPlayers.size();
    }

    public void addAchievedPlayer(String playerUUID) {
        achievedPlayers.add(playerUUID);
    }
}
