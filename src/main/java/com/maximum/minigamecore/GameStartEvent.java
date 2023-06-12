package com.maximum.minigamecore;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Queue queue;

    public GameStartEvent(Queue queue) {
        this.queue = queue;
    }

    public Queue getQueue() {
        return this.queue;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
