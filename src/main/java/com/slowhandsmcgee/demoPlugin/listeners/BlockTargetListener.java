package com.slowhandsmcgee.demoPlugin.listeners;

import com.slowhandsmcgee.demoPlugin.MinigameManager;
import org.bukkit.*;
import org.bukkit.boss.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.UUID;

public class BlockTargetListener implements Listener {

    private final Plugin plugin;
    private final MinigameManager minigameManager;

    private final HashMap<UUID, Integer> lookTicks = new HashMap<>();
    private final HashMap<UUID, BossBar> bossBars = new HashMap<>();

    private static final int LOOK_TICKS_REQUIRED = 60;
    private static final int UPDATE_INTERVAL_TICKS = 2;
    private static final double MAX_DISTANCE = 15.0;

    private boolean enabled = false;
    private Material targetBlockType = null;

    public BlockTargetListener(Plugin plugin, MinigameManager minigameManager) {
        this.plugin = plugin;
        this.minigameManager = minigameManager;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkLookTarget(player);
                }
            }
        }.runTaskTimer(plugin, 0L, UPDATE_INTERVAL_TICKS);
    }

    public void startListening(Material targetBlockType) {
        this.targetBlockType = targetBlockType;
        this.enabled = true;
    }

    public void stopListening() {
        this.enabled = false;
        this.targetBlockType = null;
        clearAllBossBars();
    }

    private void checkLookTarget(Player player) {
        if (!enabled || targetBlockType == null || !minigameManager.isInGame(player)) {
            return;
        }

        boolean lookingAtTarget = isLookingAtBlock(player, targetBlockType);
        UUID id = player.getUniqueId();

        if (lookingAtTarget) {
            int ticks = lookTicks.getOrDefault(id, 0) + UPDATE_INTERVAL_TICKS;
            lookTicks.put(id, ticks);

            double progress = Math.min(1.0, (double) ticks / LOOK_TICKS_REQUIRED);
            getOrCreateBar(player).setProgress(progress);

            if (ticks >= LOOK_TICKS_REQUIRED) {
                lookTicks.put(id, 0);
                removeBar(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

                minigameManager.foundTarget(player);
            }
        } else {
            lookTicks.put(id, 0);
            removeBar(player);
        }
    }

    private boolean isLookingAtBlock(Player player, Material targetType) {
        RayTraceResult result = player.rayTraceBlocks(MAX_DISTANCE);

        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock().getType() == targetType;
        }

        return false;
    }

    private BossBar getOrCreateBar(Player player) {
        return bossBars.computeIfAbsent(player.getUniqueId(), uuid -> {
            BossBar bar = Bukkit.createBossBar("Looking at target...", BarColor.BLUE, BarStyle.SEGMENTED_10);
            bar.addPlayer(player);
            bar.setProgress(0);
            return bar;
        });
    }

    private void removeBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    private void clearAllBossBars() {
        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }
        bossBars.clear();
        lookTicks.clear();
    }
}
