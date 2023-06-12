package com.maximum.minigamecore;

import org.bukkit.Location;

public class SpawnPoint {
    private Location location;
    private GameTeam team;

    public SpawnPoint(Location location, GameTeam team) {
        this.location = location;
        this.team = team;
    }

    public GameTeam getTeam() {
        return team;
    }

    public Location getLocation() {
        return location;
    }
}
