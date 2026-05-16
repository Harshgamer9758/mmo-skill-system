package com.blockmart.mmoskillsystem.commands;

import com.blockmart.mmoskillsystem.managers.SkillManager;
import com.blockmart.mmoskillsystem.models.PlayerSkillData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class SkillsCommand implements CommandExecutor {

    private final SkillManager skillManager;

    public SkillsCommand(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        CompletableFuture<PlayerSkillData> futureData = skillManager.getPlayerSkillData(player.getUniqueId());

        futureData.whenComplete((data, throwable) -> {
            if (throwable != null) {
                player.sendMessage(ChatColor.RED + "An error occurred while fetching your skill data.");
                throwable.printStackTrace();
                return;
            }
            if (data == null) {
                player.sendMessage(ChatColor.YELLOW + "You don't have any skill data yet. Play to gain XP!");
                return;
            }

            player.sendMessage(ChatColor.GOLD + "--- Your Skills ---");
            player.sendMessage(ChatColor.AQUA + "Mining Level: " + data.getMiningSkill().getLevel() + " (XP: " + data.getMiningSkill().getXp() + ")");
            player.sendMessage(ChatColor.AQUA + "Combat Level: " + data.getCombatSkill().getLevel() + " (XP: " + data.getCombatSkill().getXp() + ")");
            // Add more skills as needed
            player.sendMessage(ChatColor.GOLD + "-------------------");
        });

        return true;
    }
}