package com.maximum.minigamecore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Manager {
    public static void sendNpcs(Player p) {
        for(GameType gameType : GameConstants.gameTypes) {
            NPC.addNPCPacketForPlayer(gameType.getSpawnedNpc(), p, gameType.getNpc().getLocation());
        }
    }

    public static void spawnNpcs() {
        for(GameType gameType : GameConstants.gameTypes) {
            gameType.spawnNpc();
        }
    }

    public static void createInfoStands() {
        for(GameType gameType : GameConstants.gameTypes) {
            gameType.createInfoStands();
        }
    }

    public static void removeInfoStands() {
        for(GameType gameType : GameConstants.gameTypes) {
            gameType.removeInfoStands();
        }
    }

    public static void removeFormerInfoStands() {
        List<Entity> lobbyEntities = GameConstants.lobbySpawn.getWorld().getEntities();
    
        for(Entity entity : lobbyEntities) {
            if(Manager.isFormerInfoStand(entity)) {
                entity.remove();
            }
        }
    }

    public static boolean isFormerInfoStand(Entity e) {
        if(!e.getType().equals(EntityType.ARMOR_STAND)) {
            return false;
        }
        ArmorStand as = (ArmorStand) e;
        
        if(!as.isMarker()) {
            return false;
        }

        for(GameType gameType : GameConstants.gameTypes) {
            for(InfoStand infoStand : gameType.getInfoStands()) {
                if(as.equals(infoStand.getArmorStand())) {
                    return false;
                }
            }
        }

        return true;
    }

    public static void removeFromQueues(Player p) {
        for(GameType gameType : GameConstants.gameTypes) {
            for(Queue queue : gameType.getQueues()) {
                if(queue.hasPlayer(p)) {
                    queue.removePlayer(p);
                }
            }
        }
    }

    public static boolean inQueue(Player p) {
        for(GameType gameType : GameConstants.gameTypes) {
            for(Queue queue : gameType.getQueues()) {
                if(!queue.isLocked() && queue.hasPlayer(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Queue getQueue(Player p) {
        for(GameType gameType : GameConstants.gameTypes) {
            for(Queue queue : gameType.getQueues()) {
                if(!queue.isLocked() && queue.hasPlayer(p)) {
                    return queue;
                }
            }
        }
        return null;
    }


    public static boolean inLobby(Player p) {
        return p.getWorld().equals(GameConstants.lobbySpawn.getWorld());
    }

    public static boolean isLobby(World w) {
        return w.equals(GameConstants.lobbySpawn.getWorld());
    }

    public static void refreshPlayer(Player p) {
		p.setVelocity(new Vector(0,0,0));
		p.setFallDistance(0F);
		p.setNoDamageTicks(10);
	}

    public static void resetPlayer(Player p) {
        refreshPlayer(p);
        p.setHealth(20);
        p.getInventory().clear();
        p.clearTitle();
        p.setInvisible(false);
        p.setLevel(0);
        p.setAllowFlight(false);
        p.setFoodLevel(20);
        p.setSaturation(20);
        p.setExp(0);
        p.displayName(Component.text(p.getName()));
        p.setGameMode(GameMode.ADVENTURE);
		p.teleport(GameConstants.lobbySpawn);
		p.setScoreboard(GameConstants.scoreboard);
		Manager.sendNpcs(p);
        
        for(PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
    }

    public static int numQueued(GameType gt) {
        int numQueued = 0;
        for(Queue q : gt.getQueues()) {
            if(!q.isLocked()) {
                numQueued += q.getPlayers().size();
            }
        }
        return numQueued;
    }

    public static int numInGame(GameType gt) {
        int numInGame = 0;
        for(Queue q : gt.getQueues()) {
            if(q.isLocked()) {
                numInGame += q.getPlayers().size();
            }
        }
        return numInGame;
    }

    public static List<Component> buildTeamLore(Queue q, GameTeam team) {
        List<Component> lore = new ArrayList<>();
        List<GamePlayer> teamPlayers = q.getPlayersOnTeam(team);

        if(teamPlayers.size() == 0) {
            return lore;
        }
        Style topStyle = Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(false));
        lore.add(Component.text("Players: ", topStyle));

        Style playersStyle = Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC.withState(false));
        for(GamePlayer gp : teamPlayers) {
            lore.add(Component.text("  - " + gp.getPlayer().getName(), playersStyle));
        }

        return lore;
    }

    public static void updateMainScoreboard() {
        for(ScoreboardEntry entry : GameConstants.scoreboardEntries) {
			entry.update();
		}
    }

    public static List<ScoreboardEntry> createMainScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective;

        String name = GameConstants.gameName;
        
        Style style = Style.style(GameConstants.gameColor)
            .decoration(TextDecoration.BOLD, true);

        objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, Component.text(GameConstants.gameName, style));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<ScoreboardEntry> scoreboardEntries = new ArrayList<>();

        List<GameType> gameTypes = GameConstants.gameTypes;
        int numGameTypes = gameTypes.size();

        scoreboardEntries.add(new ScoreboardEntry(scoreboard,
            objective, "divider1", Component.text("")));
        
        scoreboardEntries.add(new ScoreboardEntry(scoreboard, objective, "totalPlaying",
            Component.text("Online Players: ", GameConstants.gameColor),
            team -> team.suffix(Component.text(Bukkit.getOnlinePlayers().size()))));

        scoreboardEntries.add(new ScoreboardEntry(scoreboard,
            objective, "divider2", Component.text("")));

        if(numGameTypes > 0) {
            for(int i = 0; i < numGameTypes; i++) {
                GameType gameType = gameTypes.get(i);
                
                scoreboardEntries.add(new ScoreboardEntry(scoreboard, objective, gameType.getName(),
                    Component.text(gameType.getName() + ": ", gameType.getColor()),
                    team -> team.suffix(Component.text(Manager.numQueued(gameType) + " in Queue", NamedTextColor.AQUA))));
            }
            scoreboardEntries.add(new ScoreboardEntry(scoreboard,
                    objective, "divider3", Component.text("")));
        }

        Component debugText = Component.text()
            .append(Component.text(java.time.LocalDate.now().toString(), TextColor.fromHexString("#616161")))
            .append(Component.text(" " + Main.getInstance().getPluginMeta().getVersion(), TextColor.fromHexString("#545454")))
            .build();
        scoreboardEntries.add(new ScoreboardEntry(scoreboard,
            objective, "debugText", debugText));

        scoreboardEntries.add(new ScoreboardEntry(scoreboard, objective,
            "serverIP", Component.text("flinny.net", GameConstants.gameColor)));

        for(int i = 0; i < scoreboardEntries.size(); i++) {
            int score = scoreboardEntries.size() - i;
            scoreboardEntries.get(i).initialize(score);
            scoreboardEntries.get(i).update();
        }

        return scoreboardEntries;
    }

    public static void loadWorlds() {
        File[] serverSubfiles = Bukkit.getWorldContainer().listFiles();

        for(int i = 0; i < serverSubfiles.length; i++) {
            File world = serverSubfiles[i];

            if(!world.isDirectory()) {
                continue;
            }

            File[] worldSubfiles = world.listFiles();

            for(File file : worldSubfiles) {
                if(file.getName().equals("level.dat")) {
                    Bukkit.createWorld(new WorldCreator(world.getName()));
                    break;
                }
            }
        }
    }

    public static void setUpWorlds() {
        for(World w : Bukkit.getWorlds()) {
            setGamerules(w);
            GameType gameType = worldGameType(w);
            if(gameType != null) {
                Bukkit.getPluginManager().callEvent(new GameWorldLoadEvent(w, gameType));
            }
        }
    }

    public static GameType worldGameType(World w) {
        for(GameType gameType : GameConstants.gameTypes) {
            for(Queue queue : gameType.getQueues()) {
                if(w.equals(queue.getWorld())) {
                    return gameType;
                }
            }
        }
        return null;
    }

    public static void setGamerules(World w) {
        w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        w.setGameRule(GameRule.DISABLE_RAIDS, true);
        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        w.setGameRule(GameRule.DO_FIRE_TICK, false);
        w.setGameRule(GameRule.DO_INSOMNIA, false);
        w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        w.setGameRule(GameRule.DO_VINES_SPREAD, false);
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        w.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
        w.setGameRule(GameRule.FORGIVE_DEAD_PLAYERS, true);
        w.setGameRule(GameRule.GLOBAL_SOUND_EVENTS, false);
        w.setGameRule(GameRule.MOB_GRIEFING, false);
        w.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        w.setGameRule(GameRule.SPAWN_RADIUS, 1);
        w.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        w.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        w.setWeatherDuration(1);
        w.setTime(6000);
    }
    public static Location findGround(Location l) {
		Vector start = new Vector(l.getX(), l.getY(), l.getZ());
        Vector direction = new Vector(0,-1,0);
		
        BlockIterator bi = new BlockIterator(l.getWorld(), start, direction, 0, 140);
        while(bi.hasNext()) {
            Block b = bi.next();
            if(b.getType() == Material.WATER || b.getType() == Material.LAVA || b.isSolid()) {
                return new Location(l.getWorld(), l.getX(), b.getY() + b.getBoundingBox().getHeight(), l.getZ(), l.getYaw(), l.getPitch());
            }
        }
        Log.warning("Unable to find ground under location " + l.toString());
        return l;
    }
}
