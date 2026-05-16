package com.blockmart.mmoskillsystem.models;

import java.util.UUID;

public class PlayerSkillData {
    private final UUID playerUUID;
    private final Skill miningSkill;
    private final Skill combatSkill;

    public PlayerSkillData(UUID playerUUID, Skill miningSkill, Skill combatSkill) {
        this.playerUUID = playerUUID;
        this.miningSkill = miningSkill;
        this.combatSkill = combatSkill;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Skill getMiningSkill() {
        return miningSkill;
    }

    public Skill getCombatSkill() {
        return combatSkill;
    }

    public Skill getSkill(SkillType type) {
        switch (type) {
            case MINING: return miningSkill;
            case COMBAT: return combatSkill;
            default: return null;
        }
    }
}