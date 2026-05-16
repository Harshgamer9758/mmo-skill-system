package com.blockmart.mmoskillsystem;

import com.blockmart.mmoskillsystem.commands.SkillAdminCommand;
import com.blockmart.mmoskillsystem.commands.SkillsCommand;
import com.blockmart.mmoskillsystem.listeners.PlayerSkillListener;
import com.blockmart.mmoskillsystem.managers.DatabaseManager;
import com.blockmart.mmoskillsystem.managers.SkillManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MMOSkillSystem extends JavaPlugin {

    private DatabaseManager databaseManager;
    private SkillManager skillManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.databaseManager = new DatabaseManager(this);
        this.skillManager = new SkillManager(this, databaseManager);

        registerCommands();
        registerListeners();

        getLogger().info("MMOSkillSystem has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().info("MMOSkillSystem has been disabled!");
    }

    private void registerCommands() {
        getCommand("skills").setExecutor(new SkillsCommand(skillManager));
        getCommand("skilladmin").setExecutor(new SkillAdminCommand(skillManager));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerSkillListener(skillManager), this);
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}