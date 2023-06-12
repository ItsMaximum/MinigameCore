package com.maximum.minigamecore;

import java.time.Duration;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class GameCountdown extends BukkitRunnable {

    private int tickLength;
    private int ticksRemaining;
    private Queue queue;

    public GameCountdown(Queue queue, int tickLength) {
        this.tickLength = tickLength;
        this.ticksRemaining = tickLength;
        this.queue = queue;
	}

    public void begin() {
		this.runTaskTimer(Main.getInstance(), 0, 1);
        queue.displayGameTitle();
	}

    @Override
    public void run() {
        if (ticksRemaining == 0) {
            Times times = Times.times(Duration.ZERO, Duration.ofMillis(1000), Duration.ofMillis(500));
            Title title = Title.title(Component.text("Go!").color(NamedTextColor.GREEN), Component.empty(), times);
            queue.displayTitle(title);
            queue.playSound(Sound.BLOCK_ANVIL_LAND);

			cancel();
			GameStartEvent event = new GameStartEvent(queue);
            Bukkit.getServer().getPluginManager().callEvent(event);
			return;
		}
        
        if (tickLength - ticksRemaining == 40) {
            queue.displayGameSubtitle();
        }

        if (ticksRemaining == 120) {
            Times times = Times.times(Duration.ofMillis(500), Duration.ofMillis(100000), Duration.ZERO);
            Title title = Title.title(Component.text("Game starts in...").color(NamedTextColor.YELLOW), Component.empty(), times);
            queue.displayTitle(title);
        }

        if (ticksRemaining < 120 && ticksRemaining % 20 == 0) {
            int secondsLeft = ticksRemaining / 20;
            Times times = Times.times(Duration.ZERO, Duration.ofMillis(100000), Duration.ZERO);
            String hexColor = "#FFFFFF";

            switch(secondsLeft) {
            case 5:
                hexColor = "#FA6F01";
                break;
            case 4:
                hexColor = "#F55301";
                break;
            case 3:
                hexColor = "#F03801";
                break;
            case 2:
                hexColor = "#EB1C01";
                break;
            case 1:
                hexColor = "#E60001";
                break;
            }

            TextColor color = TextColor.fromHexString(hexColor);
            Title title = Title.title(Component.text("" + secondsLeft).color(color), Component.empty(), times);
            queue.displayTitle(title);
            queue.playSound(Sound.BLOCK_DISPENSER_FAIL);
        }
        ticksRemaining--;
    }
}
