package com.maximum.minigamecore;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import com.comphenix.protocol.ProtocolLibrary;

@DefaultQualifier(NonNull.class)
public final class Main extends JavaPlugin implements Listener {
  private static Main instance;
  private static ProtocolListener pl;

  @Override
  public void onEnable() {
    Main.instance = this;

    new Config(this);

    pl = new ProtocolListener(ProtocolLibrary.getProtocolManager());
		pl.registerListeners();

    getCommand("leave").setExecutor(new LeaveCommand());

    getServer().getScheduler().runTask(this, () -> {
      Manager.loadWorlds();
      GameConstants.setConstants();
      Manager.setUpWorlds();
      Manager.spawnNpcs();
      Manager.createInfoStands();
      Manager.removeFormerInfoStands();
      this.getServer().getPluginManager().registerEvents(new GameListener(), this);
    });
  }

  @Override
	public void onDisable() {
		Manager.removeInfoStands();
	}

  public static Main getInstance() { return instance; }
}
