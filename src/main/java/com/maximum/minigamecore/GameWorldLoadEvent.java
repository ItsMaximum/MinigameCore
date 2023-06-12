package com.maximum.minigamecore;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameWorldLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private World world;
    private GameType gameType;

    public GameWorldLoadEvent(World world, GameType gameType) {
        this.world = world;
        this.gameType = gameType;
    }

    public World getWorld() {
        return this.world;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}