package com.blockmart.mmoskillsystem.managers;

import com.blockmart.mmoskillsystem.MMOSkillSystem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final MMOSkillSystem plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(MMOSkillSystem plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        HikariConfig config = new HikariConfig();
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File databaseFile = new File(dataFolder, "mmo_skills.db");
        try {
            if (!databaseFile.exists()) {
                databaseFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create database file: " + e.getMessage());
            return;
        }

        config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        config.setPoolName("MMOSkillSystem-Pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        CompletableFuture.runAsync(() -> {
            String createPlayerSkillsTable = "CREATE TABLE IF NOT EXISTS player_skills (" +
                    "uuid TEXT PRIMARY KEY," +
                    "mining_xp INTEGER DEFAULT 0," +
                    "mining_level INTEGER DEFAULT 1," +
                    "combat_xp INTEGER DEFAULT 0," +
                    "combat_level INTEGER DEFAULT 1" +
                    ");";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(createPlayerSkillsTable)) {
                stmt.execute();
                plugin.getLogger().info("player_skills table checked/created.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
            }
        }, Bukkit.getScheduler().getAsyncScheduler());
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public <T> CompletableFuture<T> executeQuery(DatabaseQuery<T> query) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection()) {
                return query.execute(conn);
            } catch (SQLException e) {
                plugin.getLogger().severe("Database query failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, Bukkit.getScheduler().getAsyncScheduler());
    }

    @FunctionalInterface
    public interface DatabaseQuery<T> {
        T execute(Connection connection) throws SQLException;
    }
}