package com.slowhandsmcgee.demoPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class VersionCommand implements CommandExecutor {
    private final Plugin plugin;

    public VersionCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§a=== Plugin Info ===");
        sender.sendMessage("§bName: §f" + plugin.getDescription().getName());
        sender.sendMessage("§bVersion: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§bAuthor: §f" + String.join(", ", plugin.getDescription().getAuthors()));
        sender.sendMessage("§bDescription: §f" + plugin.getDescription().getDescription());
        return true;
    }
}
