package com.maximum.minigamecore;

import java.util.function.Consumer;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.kyori.adventure.text.Component;

public class ScoreboardEntry {
	private Scoreboard scoreboard;
	private Objective objective;
	private String teamName;
	private Component prefix;
	private int score;
	private String identifier;
	private Team team;
	private final Consumer<Team> updateAction;	

	public ScoreboardEntry(Scoreboard scoreboard, Objective objective, String teamName, Component prefix, Consumer<Team> updateAction) {
		this.scoreboard = scoreboard;
		this.objective = objective;
		this.teamName = teamName;
		this.prefix = prefix;
		this.updateAction = updateAction;
	}

	public ScoreboardEntry(Scoreboard scoreboard, Objective objective, String teamName, Component prefix) {
		this(scoreboard, objective, teamName, prefix, null);
	}
	
	public void initialize(int score) {
		this.score = score;
		this.team = this.scoreboard.registerNewTeam(this.teamName);
		this.team.prefix(this.prefix);

		this.identifier = "";
		for(int i = 0; i < this.score; i++) {
			this.identifier += "§";
		}
		this.team.addEntry(this.identifier);
		this.objective.getScore(this.identifier).setScore(this.score);
	}

	public void update() {
		if(this.updateAction != null) {
        	this.updateAction.accept(this.team);
		}
	}
	
	public Team getTeam() {
		return this.team;
	}

	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}
}
