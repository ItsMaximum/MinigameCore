package com.maximum.minigamecore;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Queue queue;

    public GameCreateEvent(Queue queue) {
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