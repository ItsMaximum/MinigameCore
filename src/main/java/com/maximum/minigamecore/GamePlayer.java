package com.maximum.minigamecore;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class GamePlayer {
    private Player player;
    private GameTeam team;
    private boolean isTeamFinalized;
    private PlayerState state;
    private HashMap<String, Object> gameData;
    private List<ScoreboardEntry> scoreboardEntries;
    private BukkitTask orbitTask;

    public GamePlayer(Player player) {
        this.player = player;
        this.team = null;
        this.isTeamFinalized = false;
        this.state = PlayerState.QUEUED;
        gameData = new HashMap<>();
        this.orbitTask = null;
    }

    public Player getPlayer() {
        return player;
    }

    public GameTeam getTeam() {
        return team;
    }

    public boolean isTeamFinalized() {
        return isTeamFinalized;
    }

    public HashMap<String, Object> getGameData() {
        return gameData;
    }

    public void setTeam(GameTeam team) {
        this.team = team;
    }

    public void setTeamFinalized(boolean isTeamFinalized) {
        this.isTeamFinalized = isTeamFinalized;
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public List<ScoreboardEntry> getScoreboardEntries() {
        return scoreboardEntries;
    }

    public void setScoreboardEntries(List<ScoreboardEntry> scoreboardEntries) {
        this.scoreboardEntries = scoreboardEntries;
    }

    public BukkitTask getOrbitTask() {
        return orbitTask;
    }

    public void setOrbitTask(BukkitTask orbitTask) {
        this.orbitTask = orbitTask;
    }
}
