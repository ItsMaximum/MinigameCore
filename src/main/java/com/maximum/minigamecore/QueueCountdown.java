package com.maximum.minigamecore;

import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class QueueCountdown extends BukkitRunnable {

    private int seconds;
    private Queue queue;
    private boolean isRunning;

    public QueueCountdown(Queue queue) {
        this.seconds = GameConstants.minCountdown;
        this.queue = queue;
        this.isRunning = false;
	}

    public void begin() {
		this.runTaskTimer(Main.getInstance(), 0, 20);
        this.isRunning = true;
        queue.sendMessage(Component.text("Countdown has begun!", NamedTextColor.GOLD));
	}

    @Override
    public void run() {
        if (seconds == 0) {
			cancel();
            this.isRunning = false;
			queue.startGame();
            queue.setCountdown(new QueueCountdown(queue));
			return;
		}
		
		if(seconds % 30 == 0 || seconds <= 10) {
			if(seconds == 1) {
				queue.sendMessage(Component.text("Game will start in 1 second.", NamedTextColor.AQUA));
			} else {
				queue.sendMessage(Component.text("Game will start in " + seconds + " seconds.", NamedTextColor.AQUA));
			}
		}

        seconds--;
    }

    public void stop() {
        cancel();
        this.isRunning = false;
		queue.sendMessage(Component.text("There are too few players. Countdown stopped.", NamedTextColor.RED));
		queue.setCountdown(new QueueCountdown(queue));
    }

    public void shorten() {
        if(this.seconds > GameConstants.maxCountdown) {
            this.seconds = GameConstants.maxCountdown;
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
