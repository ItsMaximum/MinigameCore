package com.maximum.minigamecore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class Queue {
    private int id;
    private World world;
    private GameType gameType;
    private List<GamePlayer> players;
    private boolean isLocked;
    private QueueCountdown countdown;
    private Scoreboard scoreboard;
    private List<ScoreboardEntry> scoreboardEntries;
    private List<SpawnPoint> spawnPoints;
    private Title gameTitle;
    private Title gameSubtitle;

    public Queue(int id, World world, GameType gameType, List<SpawnPoint> spawnPoints) {
        this.id = id;
        this.world = world;
        this.gameType = gameType;
        this.spawnPoints = spawnPoints;
        this.players = new ArrayList<GamePlayer>();
        this.isLocked = false;
        this.countdown = new QueueCountdown(this);

        Times titleTimes = Times.times(Duration.ofMillis(500), Duration.ofMillis(100000), Duration.ZERO);
        Times subtitleTimes = Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500));
        this.gameTitle = Title.title(Component.text(GameConstants.gameName).color(GameConstants.gameColor), Component.empty(), titleTimes);
        this.gameSubtitle = Title.title(Component.text(GameConstants.gameName).color(GameConstants.gameColor),
            Component.text(this.gameType.getName()).color(this.gameType.getColor()), subtitleTimes);

        this.createScoreboard();
    }

    private void createScoreboard() {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        String name = gameType.getName() + " Queue " + id;
        
        Style style = Style.style(GameConstants.gameColor)
            .decoration(TextDecoration.BOLD, true);

        Objective objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, Component.text(GameConstants.gameName, style));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.scoreboardEntries = new ArrayList<>();

        List<GameTeam> teams = this.gameType.getTeams();
        int numTeams = teams.size() == 0 ? -1 : teams.size();

        this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard,
            objective, "divider1", Component.text("")));

        Component mode = Component.text()
            .append(Component.text("Mode: ", GameConstants.gameColor))
            .append(Component.text(this.gameType.getName()))
            .build();
        this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard,
            objective, "mode", mode));

        this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard,
            objective, "divider2", Component.text("")));
        
        this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard, objective, "queuedPlayers",
            Component.text("Players: ", GameConstants.gameColor),
            team -> team.suffix(Component.text("" + players.size() + "/" + gameType.getMaxPlayers()))));

        this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard, objective, "neededPlayers",
            Component.text("Needed: ", NamedTextColor.GOLD),
            team -> team.suffix(Component.text(gameType.getMinPlayers()))));

        this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard,
            objective, "divider3", Component.text("")));

        if(numTeams > 0) {
            for(int i = 0; i < numTeams; i++) {
                GameTeam gameTeam = teams.get(i);

                this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard, objective, gameTeam.getName(),
                    Component.text(gameTeam.getName() + ": ", gameTeam.getColor()),
                    team -> team.suffix(Component.text(this.getPlayersOnTeam(gameTeam).size()))));
            }

            this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard,
            objective, "divider4", Component.text("")));
        }

        Component debugText = Component.text()
            .append(Component.text(java.time.LocalDate.now().toString(), TextColor.fromHexString("#616161")))
            .append(Component.text(" " + this.id, TextColor.fromHexString("#545454")))
            .build();
        this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard,
            objective, "debugText", debugText));

        this.scoreboardEntries.add(new ScoreboardEntry(this.scoreboard, objective,
            "serverIP", Component.text("flinny.net", GameConstants.gameColor)));

        for(int i = 0; i < this.scoreboardEntries.size(); i++) {
            int score = this.scoreboardEntries.size() - i;
            this.scoreboardEntries.get(i).initialize(score);
        }
    }

    public void updateScoreboard() {
        for(ScoreboardEntry entry : this.scoreboardEntries) {
			entry.update();
		}
    }

    public World getWorld() {
        return this.world;
    }

    public boolean hasPlayer(Player p) {
        for(GamePlayer gp : this.players) {
            if(gp.getPlayer().equals(p)) {
                return true;
            }
        }
        return false;
    }

    public GamePlayer getGamePlayer(Player p) {
        for(GamePlayer gp : this.players) {
            if(gp.getPlayer().equals(p)) {
                return gp;
            }
        }
        return null;
    }

    public List<GamePlayer> getPlayersOnTeam(GameTeam team) {
        List<GamePlayer> teamPlayers = new ArrayList<>();
        for(GamePlayer gp : this.players) {
            if(gp.getTeam() != null && gp.getTeam().equals(team)) {
                teamPlayers.add(gp);
            }
        }
        return teamPlayers;
    }

    public void addPlayer(Player p) {
        if(this.isLocked) {
            Log.warning("A player was attempted to be added to a " + this.gameType.getName() + " queue for the map "
                + this.getWorld().getName() + ", but the queue is locked.");
            return;
        }

        if(this.isFull() || this.hasPlayer(p) || p == null) {
            Log.warning("A player was attempted to be added to a " + this.gameType.getName() + " queue for the map "
                + this.getWorld().getName() + ", but the queue is full, the player is null, or the player is already queued.");
            return;
        }

        this.players.add(new GamePlayer(p));

        this.updateScoreboard();
        Manager.updateMainScoreboard();
        p.setScoreboard(this.scoreboard);

        this.gameType.updateInfoStands();

        this.giveItems(p);

        if(this.isFull()) {
            this.countdown.shorten();
        }

        if(this.countdown.isRunning()) {
            return;
        }

        if(this.players.size() >= gameType.getMinPlayers()) {
            this.countdown.begin();
        }
    }

    public void removePlayer(Player p) {
        if(this.isLocked) {
            Log.warning("A player was attempted to be removed from a " + this.gameType.getName() + " queue for the map "
                + this.getWorld().getName() + ", but the queue is locked.");
            return;
        }

        GamePlayer gp = this.getGamePlayer(p);

        if(p == null || gp == null) {
            Log.warning("A player was attempted to be removed from a " + this.gameType.getName() + " queue for the map "
            + this.getWorld().getName() + ", but that player was not in the queue or is null.");
            return;
        }

        this.players.remove(gp);

        this.updateScoreboard();
        Manager.updateMainScoreboard();
        p.setScoreboard(GameConstants.scoreboard);

        this.gameType.updateInfoStands();

        p.getInventory().clear();

        if(!this.countdown.isRunning() || this.countdown.isCancelled()) {
            return;
        }

        if(this.players.size() < gameType.getMinPlayers()) {
            this.countdown.stop();
        }
    }

    public void assignTeam(Player p, GameTeam desiredTeam) {
        GamePlayer gamePlayer = this.getGamePlayer(p);
        if(gamePlayer == null) {
            Log.warning("You have tried to assign a player to a team in a queue that they are not in!");
            return;
        }

        List<GamePlayer> desiredTeamPlayers = this.getPlayersOnTeam(desiredTeam);
        if(desiredTeamPlayers.contains(gamePlayer)) {
            p.sendMessage(Component.text("You have already chosen this team!", NamedTextColor.RED));
            return;
        }
        
        int queueCap = desiredTeam.getQueueCap();
        if(queueCap == -1 || (desiredTeamPlayers.size() < queueCap)) {
            gamePlayer.setTeam(desiredTeam);
            this.updateScoreboard();

            Component message = Component.text()
                .append(Component.text("You have chosen the "))
                .append(Component.text(desiredTeam.getName(), desiredTeam.getColor()))
                .append(Component.text(" team!"))
                .build();
            p.sendMessage(message);
        } else {
            p.sendMessage(Component.text("This team is full!", NamedTextColor.RED));
        }
    }

    public void distributeTeams() {
        Collections.shuffle(this.players);

        if(gameType.getTeams().size() == 0) {
            return;
        }

        for(int i = 0; i < gameType.getTeams().size(); i++) {
            GameTeam team = gameType.getTeams().get(i);
            String distributionType = team.getDistributionType();
            if(distributionType.equals("remaining")) {
                continue;
            }

            List<GamePlayer> teamPlayers = this.getPlayersOnTeam(team);
            int numPlayers = teamPlayers.size();
            int minPlayers = 0;
            int maxPlayers = 0;

            switch(distributionType) {
            case "one":
                minPlayers = maxPlayers = 1;
                break;
            case "third":
                minPlayers = (int) Math.floor(this.players.size() / 3.0);
                maxPlayers = (int) Math.ceil(this.players.size() / 3.0);
                break;
            case "half":
                minPlayers = (int) Math.floor(this.players.size() / 2.0);
                maxPlayers = (int) Math.ceil(this.players.size() / 2.0);
                break;
            default:
                Log.warning("Encountered unknown distribution type " + distributionType + ". Defaulting to 'remaining'.");
                continue;
            }

            if(numPlayers < minPlayers) {
                int neededPlayers = minPlayers - numPlayers;

                for(int j = 0; j < neededPlayers; j++) {
                    GamePlayer player = findPlayerForTeam(team);

                    if(player == null) {
                        Log.warning("Unable to find a player for team " + team.getName() + ". Config must be adjusted!");
                        break;
                    }

                    player.setTeam(team);
                    teamPlayers.add(player);

                    Log.info("added " + player.getPlayer().getName() + " from the " + team.getName() + " team.");
                }
            } else if(numPlayers > maxPlayers) {
                int extraPlayers = numPlayers - maxPlayers;

                for(int j = 0; j < extraPlayers; j++) {
                    GamePlayer player = teamPlayers.get(j);

                    player.setTeam(null);
                    teamPlayers.remove(player);

                    Log.info("removed " + player.getPlayer().getName() + " from the " + team.getName() + " team.");
                }
            }
            for(GamePlayer gp : teamPlayers) {
                gp.setTeamFinalized(true);
            }
        }

        for(int i = 0; i < gameType.getTeams().size(); i++) {
            GameTeam team = gameType.getTeams().get(i);
            String distributionType = team.getDistributionType();
            if(!distributionType.equals("remaining")) {
                continue;
            }

            List<GamePlayer> teamPlayers = this.getPlayersOnTeam(team);

            for(GamePlayer gp : this.players) {
                if(team.getQueueCap() != -1 && (teamPlayers.size() >= team.getQueueCap())) {
                    break;
                }

                if(gp.getTeam() == null) {
                    gp.setTeam(team);
                    teamPlayers.add(gp);

                    Log.info("added " + gp.getPlayer().getName() + " to the " + team.getName() + " team.");
                }
            }

            for(GamePlayer gp : teamPlayers) {
                gp.setTeamFinalized(true);
            }
        }
        for(GamePlayer gp : this.players) {
            if(!gp.isTeamFinalized()) {
                Log.warning("The game started with a player having a non-finalized team. Something has gone horribly wrong!");
            }

            if(gp.getTeam() == null) {
                Log.warning("The game started with a player having a null team. Something has gone horribly wrong!");
            }
        }
    }

    public GamePlayer findNoTeamPlayerForTeam(GameTeam team) {
        for(GamePlayer gp : this.players) {
            if(gp.getTeam() == null) {
                return gp;
            }
        }
        return null;
    }

    public GamePlayer findPlayerForTeam(GameTeam team) {
        for(GamePlayer gp : this.players) {
            if(gp.getTeam() == null) {
                return gp;
            }
        }

        for(GamePlayer gp : this.players) {
            if(!gp.isTeamFinalized() && !gp.getTeam().equals(team)) {
                return gp;
            }
        }
        
        return null;
    }

    public void giveItems(Player p) {
        if(this.gameType.getTeams().size() > 0) {
            addItem(p,Material.COMPASS, 1, "Team Selector", "#00FFFF", 0);
        }
        addItem(p,Material.RED_BED, 1, "Leave Queue", "#FF5555", 8);
    }

    public void addItem(Player p, Material material, int amount, String name, String hexColor, int slot) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta itemStackMeta = item.getItemMeta();
		itemStackMeta.displayName(Component.text(name).color(TextColor.fromHexString(hexColor)));
		item.setItemMeta(itemStackMeta);
		p.getInventory().setItem(slot, item);
	}

    public void sendMessage(Component message) {
        for(GamePlayer gp : this.players) {
            gp.getPlayer().sendMessage(message);
        }
    }

    public void displayGameTitle() {
        for(GamePlayer gp : this.players) {
            gp.getPlayer().showTitle(this.gameTitle);
        }
    }

    public void displayGameSubtitle() {
        for(GamePlayer gp : this.players) {
            gp.getPlayer().showTitle(this.gameSubtitle);
        }
    }

    public void displayTitle(Title title) {
        for(GamePlayer gp : this.players) {
            gp.getPlayer().showTitle(title);
        }
    }

    public void clearTitle() {
        for(GamePlayer gp : this.players) {
            gp.getPlayer().clearTitle();
        }
    }

    public void playSound(Sound sound) {
        for(GamePlayer gp : this.players) {
            gp.getPlayer().playSound(gp.getPlayer(), sound, 0.5f, 1.0f);
        }
    }

    public void setCountdown(QueueCountdown countdown) {
        this.countdown = countdown;
    }

    // finish later
    public void startGame() {
        this.isLocked = true;

        for(GamePlayer gp : this.players) {
            Player p = gp.getPlayer();
            Manager.resetPlayer(p);
            if(gp.getTeam() != null) {
                Log.info("Setting display name for " + p.getName());
                p.displayName(Component.text(p.getName(), gp.getTeam().getColor()));
            }
        }

        Manager.updateMainScoreboard();
        this.gameType.updateInfoStands();

        this.distributeTeams();
        this.setDisplayNames();
        GameCreateEvent event = new GameCreateEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        this.sendPlayers();
        //replace with value from config eventually
        new GameCountdown(this, 256).begin();
    }

    public void reset() {
        this.players.forEach(player -> Manager.resetPlayer(player.getPlayer()));
        this.players.clear();
        this.isLocked = false;
    }

    public void sendPlayers() {
        Collections.shuffle(this.spawnPoints);
        int pointIndex = 0;
        for(GamePlayer player : this.players) {
            if(pointIndex >= this.spawnPoints.size()) {
                pointIndex = 0;
            }
            for(int i = pointIndex; i < this.spawnPoints.size(); i++) {
                SpawnPoint point = this.spawnPoints.get(i);
                GameTeam playerTeam = player.getTeam();
                GameTeam pointTeam = point.getTeam();

                if(playerTeam != null && pointTeam != null && !playerTeam.equals(pointTeam)) {
                    pointIndex++;
                    continue;
                }
                player.setOrbitTask(this.gameType.getOrbit().orbit(player.getPlayer(), Manager.findGround(point.getLocation())));
                player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                player.setState(PlayerState.ALIVE);
                pointIndex++;
                break;
            }
        }
    }

    public void setDisplayNames() {
        for(GamePlayer gp : this.players) {
            if(gp.getTeam() != null) {
                Player p = gp.getPlayer();
                p.displayName(Component.text(p.getName(), gp.getTeam().getColor()));
            }
        }
    }

    public List<GamePlayer> getPlayers() {
        return this.players;
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    public boolean isFull() {
        return this.players.size() >= gameType.getMaxPlayers();
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public int getId() {
        return id;
    }

    public List<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }
}
