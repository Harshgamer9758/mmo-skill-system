package com.blockmart.mmoskillsystem.managers;

import com.blockmart.mmoskillsystem.MMOSkillSystem;
import com.blockmart.mmoskillsystem.models.PlayerSkillData;
import com.blockmart.mmoskillsystem.models.Skill;
import com.blockmart.mmoskillsystem.models.SkillType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkillManager {

    private final MMOSkillSystem plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerSkillData> playerSkillCache;

    public SkillManager(MMOSkillSystem plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerSkillCache = new HashMap<>();
    }

    public CompletableFuture<PlayerSkillData> getPlayerSkillData(UUID playerUUID) {
        if (playerSkillCache.containsKey(playerUUID)) {
            return CompletableFuture.completedFuture(playerSkillCache.get(playerUUID));
        }

        return databaseManager.executeQuery(conn -> {
            String selectSql = "SELECT mining_xp, mining_level, combat_xp, combat_level FROM player_skills WHERE uuid = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, playerUUID.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        PlayerSkillData data = new PlayerSkillData(
                                playerUUID,
                                new Skill(SkillType.MINING, rs.getInt("mining_xp"), rs.getInt("mining_level")),
                                new Skill(SkillType.COMBAT, rs.getInt("combat_xp"), rs.getInt("combat_level"))
                        );
                        playerSkillCache.put(playerUUID, data);
                        return data;
                    } else {
                        return createNewPlayerSkillData(conn, playerUUID);
                    }
                }
            }
        });
    }

    private PlayerSkillData createNewPlayerSkillData(Connection conn, UUID playerUUID) throws SQLException {
        String insertSql = "INSERT INTO player_skills (uuid) VALUES (?);";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.executeUpdate();
        }
        PlayerSkillData newData = new PlayerSkillData(playerUUID, new Skill(SkillType.MINING, 0, 1), new Skill(SkillType.COMBAT, 0, 1));
        playerSkillCache.put(playerUUID, newData);
        return newData;
    }

    public CompletableFuture<Void> setSkillXp(UUID playerUUID, SkillType skillType, int newXp) {
        return getPlayerSkillData(playerUUID).thenCompose(data -> {
            Skill skill = data.getSkill(skillType);
            if (skill == null) return CompletableFuture.completedFuture(null);

            skill.setXp(newXp);
            checkLevelUp(playerUUID, skillType, skill);
            return updateSkillInDatabase(playerUUID, skillType, skill.getXp(), skill.getLevel());
        });
    }

    public CompletableFuture<Integer> addSkillXp(UUID playerUUID, SkillType skillType, int xpToAdd) {
        return getPlayerSkillData(playerUUID).thenCompose(data -> {
            Skill skill = data.getSkill(skillType);
            if (skill == null) {
                plugin.getLogger().warning("Attempted to add XP to null skill for player " + playerUUID + ", skillType " + skillType);
                return CompletableFuture.completedFuture(0);
            }

            int currentXP = skill.getXp();
            skill.addXp(xpToAdd);
            checkLevelUp(playerUUID, skillType, skill);

            return updateSkillInDatabase(playerUUID, skillType, skill.getXp(), skill.getLevel())
                    .thenApply(v -> skill.getXp());
        });
    }

    private void checkLevelUp(UUID playerUUID, SkillType skillType, Skill skill) {
        int currentLevel = skill.getLevel();
        int xpNeededForNextLevel = calculateXpForNextLevel(currentLevel);

        while (skill.getXp() >= xpNeededForNextLevel) {
            skill.levelUp();
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player != null) {
                player.sendMessage(ChatColor.GOLD + "Congratulations! Your " + skillType.name().toLowerCase() + " skill increased to level " + skill.getLevel() + "!");
            }
            xpNeededForNextLevel = calculateXpForNextLevel(skill.getLevel());
        }
    }

    private int calculateXpForNextLevel(int currentLevel) {
        // Example: Simple linear progression, modify as needed
        return 100 + (currentLevel - 1) * 50;
    }

    private CompletableFuture<Void> updateSkillInDatabase(UUID playerUUID, SkillType skillType, int xp, int level) {
        return databaseManager.executeQuery(conn -> {
            String updateSql = "UPDATE player_skills SET " +
                    skillType.name().toLowerCase() + "_xp = ?, " +
                    skillType.name().toLowerCase() + "_level = ? " +
                    "WHERE uuid = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, xp);
                stmt.setInt(2, level);
                stmt.setString(3, playerUUID.toString());
                stmt.executeUpdate();
            }
            return null;
        });
    }

    public void removePlayerFromCache(UUID playerUUID) {
        playerSkillCache.remove(playerUUID);
    }
}