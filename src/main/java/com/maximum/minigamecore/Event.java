package com.maximum.minigamecore;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class Event {
    private String name;
    private TextColor color;
    private int length;
    private int secondsRemaining;
    private Runnable execute;
    private Runnable callback;
    private BukkitTask countdown;

    public Event(String name, TextColor color, int length, Runnable execute, Runnable callback) {
        this.name = name;
        this.color = color;
        this.length = length;
        this.secondsRemaining = length;
        this.execute = execute;
        this.callback = callback;
    }

    public void startCountdown() {
        this.countdown = new BukkitRunnable() {
            @Override
            public void run() {
                secondsRemaining--;
                callback.run();
                if(secondsRemaining == 0) {
                    cancel();
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> execute.run());
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20L, 20L);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public void setSecondsRemaining(int secondsRemaining) {
        this.secondsRemaining = secondsRemaining;
    }

    public String getMMSSRemaining() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public BukkitTask getCountdown() {
        return countdown;
    }

    public TextColor getColor() {
        return color;
    }

    public void setColor(TextColor color) {
        this.color = color;
    }

    public Component getComponent() {
        return Component.text()
            .append(Component.text(this.name + " ", this.color))
            .append(Component.text(this.getMMSSRemaining()))
            .build();
    }
}
