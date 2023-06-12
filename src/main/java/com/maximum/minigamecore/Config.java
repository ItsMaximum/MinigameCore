package com.maximum.minigamecore;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.format.TextColor;

public class Config {
	
    private static FileConfiguration c;
	
	public Config(Main main) {
        c = main.getConfig();
		
		main.saveDefaultConfig(); // Generates a config.yml in the plugins folder
		c.options().copyDefaults();
	}

    public static Location getLobbySpawn() {
		return getConfigLocation("lobby-spawn.");
	}

    public static Location getConfigLocation(String root) {
		return new Location(
				Bukkit.getWorld(c.getString(root + "world")),
				c.getDouble(root + "x"),
				c.getDouble(root + "y"),
				c.getDouble(root + "z"),
				(float) c.getDouble(root + "yaw"),
				(float) c.getDouble(root + "pitch"));
	}

    public static Location getConfigLocation(String root, World world) {
		return new Location(
				world,
				c.getDouble(root + "x"),
				c.getDouble(root + "y"),
				c.getDouble(root + "z"),
				(float) c.getDouble(root + "yaw"),
				(float) c.getDouble(root + "pitch"));
	}

    public static ArmorStandOrbit getOrbit(String root) {
		return new ArmorStandOrbit(
                c.getDouble(root + "radius"),
                c.getInt(root + "step"),
                c.getDouble(root + "center.x"),
                c.getDouble(root + "center.y"),
                c.getDouble(root + "center.z"));
	}

    // Will need to be updated to support other games
    public static List<GameTeam> getGlobalTeams() {
        return new ArrayList<GameTeam>();
    }

    public static int getMinCountdown() {
        return c.getInt("min-countdown");
    }

    public static int getMaxCountdown() {
        return c.getInt("max-countdown");
    }

    public static String getGameName() {
        return c.getString("game-name");
    }

    public static TextColor getGameColor() {
        return TextColor.fromHexString(c.getString("game-color"));
    }

    public static List<GameType> getGameTypes() {
        List<GameType> gameTypes = new ArrayList<>();

        for(String id : c.getConfigurationSection("game-types.").getKeys(false)) {
            String name = c.getString("game-types." + id + ".name");
            TextColor color = TextColor.fromHexString(c.getString("game-types." + id + ".color"));
            int minPlayers = c.getInt("game-types." + id + ".min-players");
            int maxPlayers = c.getInt("game-types." + id + ".max-players");
            NPC npc = getNpc("game-types." + id + ".npc.");
            List<GameTeam> teams = getGameTeams("game-types." + id + ".teams."); //finish later
            ArmorStandOrbit orbit = getOrbit("game-types." + id + ".orbit.");

            GameType gameType = new GameType(Integer.parseInt(id), name, color, minPlayers, maxPlayers, npc, teams, orbit);

            List<Queue> queues = getQueues("game-types." + id + ".queues.", gameType);
            gameType.setQueues(queues);

            gameTypes.add(gameType);
        }
        return gameTypes;
    }

    public static NPC getNpc(String root) {
        Location npcLocation = getConfigLocation(root + "location.");
        String npcSkinTexture = c.getString(root + "skin.texture");
        String npcSkinSignature = c.getString(root + "skin.signature");
        NPC npc = new NPC(npcLocation, npcSkinTexture, npcSkinSignature);
        return npc;
    }

    public static List<Queue> getQueues(String root, GameType gameType) {
        List<Queue> queues = new ArrayList<>();
        for(String id : c.getConfigurationSection(root).getKeys(false)) {
            World world = Bukkit.getWorld(c.getString(root + id + ".world"));
            List<SpawnPoint> spawnPoints = getSpawnPoints("game-types." + gameType.getId() + ".spawn-points.", world, gameType);
            queues.add(new Queue(Integer.parseInt(id), world, gameType, spawnPoints));
        }
        return queues;
    }

    public static List<GameTeam> getGameTeams(String root) {
        List<GameTeam> teams = new ArrayList<>();
        ConfigurationSection cs = c.getConfigurationSection(root);
        if(cs == null) {
            Log.info("Game type does not have any teams!");
            return teams;
        }

        for(String id : cs.getKeys(false)) {
            String name = c.getString(root + id + ".name");
            TextColor color = TextColor.fromHexString(c.getString(root + id + ".color"));
            Material material = Material.getMaterial(c.getString(root + id + ".material"));
            String distributionType = c.getString(root + id + ".distribution-type");
            int queueCap = c.getInt(root + id + ".queue-cap");
            teams.add(new GameTeam(name, color, material, distributionType, queueCap));
        }
        return teams;
    }

    public static List<SpawnPoint> getSpawnPoints(String root, World world, GameType gameType) {
        List<SpawnPoint> spawnPoints = new ArrayList<>();
        ConfigurationSection cs = c.getConfigurationSection(root);
        if(cs == null) {
            Log.info("Game type does not have any spawn points!");
            return spawnPoints;
        }

        for(String id : cs.getKeys(false)) {
            String type = c.getString(root + id + ".type");
            String teamName = c.getString(root + id + ".team");
            GameTeam pointTeam = null;

            if(teamName != null) {
                Log.info(root + id + teamName);
                for(GameTeam team : gameType.getTeams()) {
                    if(team.getName().equals(teamName)) {
                        pointTeam = team;
                        break;
                    }
                }

                if(pointTeam == null) {
                    Log.warning("Invalid team name was specified for game type " + gameType.getName() +"!");
                }
            }

            switch(type) {
            case "point":
                Location point = getConfigLocation(root + id + ".location.", world);
                
                spawnPoints.add(new SpawnPoint(point, pointTeam));
                break;
            case "circle":
                int numCirclePoints = gameType.getMaxPlayers();
                double radius = c.getDouble(root + id + ".radius");
                float pitch = (float) c.getDouble(root + id + ".pitch");
                double centerX = c.getDouble(root + id + ".center.x");
                double centerY = c.getDouble(root + id + ".center.y");
                double centerZ = c.getDouble(root + id + ".center.z");

                for (int i = 0; i < numCirclePoints; i++) {
                    double angle = 2.0 * Math.PI * ((double) i / numCirclePoints);
                    double pointX = centerX + (-radius * Math.sin(angle));
                    double pointY = centerY;
                    double pointZ = centerZ + (radius * Math.cos(angle));
                    float pointYaw = (float) (Math.toDegrees(angle) + 180);

                    Location circlePoint = new Location(world, pointX, pointY, pointZ, pointYaw, pitch);

                    spawnPoints.add(new SpawnPoint(circlePoint, pointTeam));
                }
                break;
            case "line":
                int numLinePoints = gameType.getMaxPlayers();
                double length = c.getDouble(root + id + ".length");
                String axis = c.getString(root + id + ".axis");
                double startX = c.getDouble(root + id + ".midpoint.x");
                double startY = c.getDouble(root + id + ".midpoint.y");
                double startZ = c.getDouble(root + id + ".midpoint.z");
                float lineYaw = (float) c.getDouble(root + id + ".midpoint.yaw");
                float linePitch = (float) c.getDouble(root + id + ".midpoint.pitch");

                Vector increment;

                switch(axis) {
                case "x":
                    startX -= (length / 2);
                    increment = new Vector(length / (numLinePoints - 1), 0, 0);
                    break;
                case "y":
                    startY -= (length / 2);
                    increment = new Vector(0, length / (numLinePoints - 1), 0);
                    break;
                case "z":
                    startZ -= (length / 2);
                    increment = new Vector(0, 0, length / (numLinePoints - 1));
                    break;
                default:
                    Log.warning("Invalid axis specified for spawn point in game type " + gameType.getName() + "!");
                    increment = new Vector(0, 0, 0);
                    break;
                }

                Vector start = new Vector(startX, startY, startZ);

                for (int i = 0; i < numLinePoints; i++) {
                    Location linePoint = new Location(world, start.getX(), start.getY(), start.getZ(), lineYaw, linePitch);
                    spawnPoints.add(new SpawnPoint(linePoint, pointTeam));
                    start.add(increment);
                }
                break;
            default:
                Log.warning("Invalid point type was specified for game type " + gameType.getName() + "!");
            }
        }
        return spawnPoints;
    }
}