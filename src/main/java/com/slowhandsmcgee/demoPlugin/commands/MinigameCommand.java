package com.slowhandsmcgee.demoPlugin.commands;

import com.slowhandsmcgee.demoPlugin.MinigameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MinigameCommand implements CommandExecutor {

    private final MinigameManager minigameManager;

    public MinigameCommand(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /minigame ftb [join/start]");
            return true;
        }

        if (!args[0].equalsIgnoreCase("ftb")) {
            sender.sendMessage(ChatColor.RED + "Unknown minigame.");
            return true;
        }

        if (args[1].equalsIgnoreCase("join")) {
            if (sender instanceof Player player) {
                minigameManager.joinLobby(player);
            }
        } else if (args[1].equalsIgnoreCase("start")) {
            minigameManager.startGame();
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown command.");
        }

        return true;
    }
}
