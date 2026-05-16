package com.blockmart.mmoskillsystem.commands;

import com.blockmart.mmoskillsystem.managers.SkillManager;
import com.blockmart.mmoskillsystem.models.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkillAdminCommand implements CommandExecutor {

    private final SkillManager skillManager;

    public SkillAdminCommand(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mmoskills.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /skilladmin <player> <skill> <setxp|addxp> <amount>");
            sender.sendMessage(ChatColor.YELLOW + "Skills: MINING, COMBAT");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }
        UUID targetUUID = targetPlayer.getUniqueId();

        SkillType skillType;
        try {
            skillType = SkillType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid skill type. Available: MINING, COMBAT.");
            return true;
        }

        String action = args[2].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Amount must be a number.");
            return true;
        }

        if (amount < 0) {
            sender.sendMessage(ChatColor.RED + "Amount cannot be negative.");
            return true;
        }

        CompletableFuture<Void> future = null;
        String successMessage = ChatColor.GREEN + "Successfully ";

        if (action.equals("setxp")) {
            future = skillManager.setSkillXp(targetUUID, skillType, amount);
            successMessage += "set " + skillType.name().toLowerCase() + " XP of " + targetPlayer.getName() + " to " + amount + ".";
        } else if (action.equals("addxp")) {
            future = skillManager.addSkillXp(targetUUID, skillType, amount);
            successMessage += "added " + amount + " XP to " + skillType.name().toLowerCase() + " for " + targetPlayer.getName() + ".";
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid action. Use 'setxp' or 'addxp'.");
            return true;
        }

        if (future != null) {
            String finalSuccessMessage = successMessage;
            future.whenComplete((v, throwable) -> {
                if (throwable != null) {
                    sender.sendMessage(ChatColor.RED + "An error occurred: " + throwable.getMessage());
                    throwable.printStackTrace();
                } else {
                    sender.sendMessage(finalSuccessMessage);
                }
            });
        }

        return true;
    }
}