package com.maximum.minigamecore;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.scoreboard.Scoreboard;

import net.kyori.adventure.text.format.TextColor;

public class GameConstants {
    public static final String gameName = Config.getGameName();
    public static final TextColor gameColor = Config.getGameColor();
    public static final int minCountdown = Config.getMinCountdown();
    public static final int maxCountdown = Config.getMaxCountdown();

    public static Location lobbySpawn;
    public static List<GameTeam> globalTeams;
    public static List<GameType> gameTypes;
    public static List<ScoreboardEntry> scoreboardEntries;
    public static Scoreboard scoreboard;

    public static void setConstants() {
        lobbySpawn = Config.getLobbySpawn();
        globalTeams = Config.getGlobalTeams();
        gameTypes = Config.getGameTypes();
        scoreboardEntries = Manager.createMainScoreboard();
        scoreboard = scoreboardEntries.get(0).getScoreboard();
    }
}
