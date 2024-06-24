package kr.lunaf.achievement.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static Connection connection;

    public static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/LunaAchievement/achievements.db");
            Statement stmt = connection.createStatement();
            String createAchievementsTable = "CREATE TABLE IF NOT EXISTS player_achievements (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "achievement_name TEXT NOT NULL," +
                    "value INTEGER DEFAULT 0," +
                    "is_complete BOOLEAN DEFAULT FALSE," +
                    "complete_date TIMESTAMP NULL," +
                    "complete_order INTEGER DEFAULT 0" +
                    ");";
            stmt.execute(createAchievementsTable);

            String createLogTable = "CREATE TABLE IF NOT EXISTS achievement_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "achievement_name TEXT NOT NULL," +
                    "achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(createLogTable);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            initializeDatabase();
        }
        return connection;
    }
}
