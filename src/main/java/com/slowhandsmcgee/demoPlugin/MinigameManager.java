package com.slowhandsmcgee.demoPlugin;

import com.slowhandsmcgee.demoPlugin.listeners.BlockTargetListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public class MinigameManager {

    private final Plugin plugin;
    private final Set<UUID> lobby = new HashSet<>();
    private final Map<UUID, Integer> scores = new HashMap<>();
    private final Set<UUID> foundPlayersThisRound = new HashSet<>();
    private BlockTargetListener blockTargetListener;

    private boolean gameRunning = false;
    private int currentRound = 0;
    private int roundTimeLeft = 0;
    private Material currentTargetBlock;
    private boolean roundActive = false;

    private final int MAX_ROUNDS = 5;
    private static final List<Material> TARGET_BLOCKS = Arrays.asList(
            Material.OCHRE_FROGLIGHT,
            Material.YELLOW_WOOL,
            Material.COPPER_GRATE,
            Material.DIAMOND_BLOCK,
            Material.PINK_SHULKER_BOX,
            Material.RED_BED,
            Material.SOUL_TORCH,
            Material.FLOWER_POT,
            Material.CHEST
    );

    public MinigameManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void setBlockTargetListener(BlockTargetListener listener) {
        this.blockTargetListener = listener;
    }

    public void joinLobby(Player player) {
        if (gameRunning) {
            player.sendMessage(ChatColor.RED + "A game is already running!");
            return;
        }
        lobby.add(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You joined the Find the Block lobby!");
    }

    public void startGame() {
        if (lobby.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players in the lobby!");
            return;
        }

        gameRunning = true;
        scores.clear();
        currentRound = 0;

        Bukkit.broadcastMessage(ChatColor.GOLD + "Find the Block has started!");

        setupScoreboards();
        startNextRound();
    }

    private void setupScoreboards() {
        for (UUID id : lobby) {
            Player player = Bukkit.getPlayer(id);
            if (player == null) continue;

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();

            Objective obj = board.registerNewObjective("score", "dummy", ChatColor.AQUA + "Find the Block");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            Score score = obj.getScore(ChatColor.GREEN + "Score:");
            score.setScore(0);

            player.setScoreboard(board);
        }
    }

    private void startNextRound() {
        if (currentRound >= MAX_ROUNDS) {
            endGame();
            return;
        }

        roundActive = true;
        currentRound++;
        roundTimeLeft = Math.max(5, 15 - (currentRound - 1) * 2); // 15s, 13s, 11s, 9s, 7s
        foundPlayersThisRound.clear();
        currentTargetBlock = getRandomBlock();

        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Round " + currentRound + ": Find " + ChatColor.AQUA + currentTargetBlock.name());

        if (blockTargetListener != null) {
            blockTargetListener.startListening(currentTargetBlock);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!roundActive) {
                    cancel();
                    return;
                }

                if (roundTimeLeft <= 0) {
                    endRoundDueToTimer();
                    cancel();
                    return;
                }

                if (roundTimeLeft <= 5 || roundTimeLeft % 5 == 0) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Time left: " + roundTimeLeft + " seconds");
                }

                roundTimeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void endRoundDueToTimer() {
        if (!roundActive) return;
        roundActive = false;

        if (currentRound < MAX_ROUNDS) {
            Bukkit.broadcastMessage(ChatColor.RED + "Round over! Preparing next round...");
        }
        else {
            Bukkit.broadcastMessage(ChatColor.RED + "Round over!");
        }

        if (blockTargetListener != null) {
            blockTargetListener.stopListening();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                startNextRound();
            }
        }.runTaskLater(plugin, 60L);
    }

    private Material getRandomBlock() {
        return TARGET_BLOCKS.get(new Random().nextInt(TARGET_BLOCKS.size()));
    }

    private void endGame() {
        gameRunning = false;

        Bukkit.broadcastMessage(ChatColor.GOLD + "Game over! Final scores:");

        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            UUID id = sorted.get(i).getKey();
            int score = sorted.get(i).getValue();
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + "" + (i + 1) + ". " + player.getName() + " - " + score + " points");
            }
        }

        for (UUID id : lobby) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }

        lobby.clear();
    }

    public void foundTarget(Player player) {
        if (!gameRunning) return;
        if (!lobby.contains(player.getUniqueId())) return;
        if (foundPlayersThisRound.contains(player.getUniqueId())) return;

        foundPlayersThisRound.add(player.getUniqueId());

        int points;
        int position = foundPlayersThisRound.size();

        if (position == 1) points = 3;
        else if (position == 2) points = 2;
        else points = 1;

        scores.put(player.getUniqueId(), scores.getOrDefault(player.getUniqueId(), 0) + points);
        updatePlayerScoreboard(player);

        player.sendMessage(ChatColor.GREEN + "You found the block and earned " + points + " points!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        if (foundPlayersThisRound.size() >= lobby.size() && roundActive) {
            roundActive = false;

            if (currentRound < MAX_ROUNDS) {
                Bukkit.broadcastMessage(ChatColor.AQUA + "Everyone found the block! Preparing next round...");
            }
            else {
                Bukkit.broadcastMessage(ChatColor.AQUA + "Everyone found the block!");
            }

            if (blockTargetListener != null) {
                blockTargetListener.stopListening();
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    startNextRound();
                }
            }.runTaskLater(plugin, 60L);
        }
    }

    private void updatePlayerScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("score");
        if (obj != null) {
            obj.getScore(ChatColor.GREEN + "Score:").setScore(scores.get(player.getUniqueId()));
        }
    }

    public boolean isInGame(Player player) {
        return gameRunning && lobby.contains(player.getUniqueId());
    }
}
