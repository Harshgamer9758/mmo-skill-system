package com.blockmart.mmoskillsystem.listeners;

import com.blockmart.mmoskillsystem.managers.SkillManager;
import com.blockmart.mmoskillsystem.models.SkillType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;

public class PlayerSkillListener implements Listener {

    private final SkillManager skillManager;

    public PlayerSkillListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        int xp = 0;
        if (block.getType() == Material.STONE || block.getType() == Material.COBBLESTONE) {
            xp = 1;
        } else if (block.getType().name().endsWith("_ORE")) {
            xp = 5;
        }

        if (xp > 0) {
            skillManager.addSkillXp(player.getUniqueId(), SkillType.MINING, xp)
                    .whenComplete((totalXp, throwable) -> {
                        if (throwable != null) {
                            player.sendMessage(ChatColor.RED + "Error gaining mining XP.");
                            throwable.printStackTrace();
                        }
                    });
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && victim != null && victim.getType() != EntityType.PLAYER) {
            int xp = 0;
            switch (victim.getType()) {
                case ZOMBIE: case SKELETON: xp = 5; break;
                case SPIDER: case CAVE_SPIDER: xp = 7; break;
                case CREEPER: xp = 10; break;
                case ENDERMAN: xp = 15; break;
                case WITHER: xp = 100; break;
                default: break;
            }

            if (xp > 0) {
                skillManager.addSkillXp(killer.getUniqueId(), SkillType.COMBAT, xp)
                        .whenComplete((totalXp, throwable) -> {
                            if (throwable != null) {
                                killer.sendMessage(ChatColor.RED + "Error gaining combat XP.");
                                throwable.printStackTrace();
                            }
                        });
            }
        }
    }
}