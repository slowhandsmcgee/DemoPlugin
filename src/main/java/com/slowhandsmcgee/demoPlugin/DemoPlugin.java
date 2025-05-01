package com.slowhandsmcgee.demoPlugin;

import com.slowhandsmcgee.demoPlugin.commands.MinigameCommand;
import com.slowhandsmcgee.demoPlugin.commands.VersionCommand;
import com.slowhandsmcgee.demoPlugin.listeners.BlockTargetListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class DemoPlugin extends JavaPlugin {

    private MinigameManager minigameManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        minigameManager = new MinigameManager(this);
        BlockTargetListener blockTargetListener = new BlockTargetListener(this, minigameManager);
        minigameManager.setBlockTargetListener(blockTargetListener);

        //Register Commands
        getCommand("dpversion").setExecutor(new VersionCommand(this));
        getCommand("minigame").setExecutor(new MinigameCommand(minigameManager));

        //Register Listeners
        getServer().getPluginManager().registerEvents(blockTargetListener, this);


        getLogger().info("DemoPlugin enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("DemoPlugin disabled");
    }
}
