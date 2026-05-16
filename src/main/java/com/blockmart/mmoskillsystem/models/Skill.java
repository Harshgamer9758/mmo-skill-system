package com.blockmart.mmoskillsystem.models;

public class Skill {
    private final SkillType type;
    private int xp;
    private int level;

    public Skill(SkillType type, int xp, int level) {
        this.type = type;
        this.xp = xp;
        this.level = level;
    }

    public SkillType getType() {
        return type;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        this.level++;
    }

    public void addXp(int amount) {
        if (amount > 0) {
            this.xp += amount;
        }
    }
}