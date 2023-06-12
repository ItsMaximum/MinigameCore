package com.maximum.minigamecore;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.level.ServerPlayer;

public class GameType {
    private int id;
    private String name;
    private TextColor color;
    private int minPlayers;
    private int maxPlayers;
    private ServerPlayer spawnedNpc;
    private NPC npc;
    private List<InfoStand> infoStands;
    private List<Queue> queues;
    private List<GameTeam> teams;
    private ArmorStandOrbit orbit;

    public GameType(int id, String name, TextColor color, int minPlayers, int maxPlayers, NPC npc, List<GameTeam> teams, ArmorStandOrbit orbit) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.npc = npc;
        this.teams = teams;
        this.orbit = orbit;
    }

    public void createInfoStands() {
        this.infoStands = new ArrayList<>();

        this.infoStands.add(new InfoStand(Component.text(this.name, Style.style(this.color, TextDecoration.BOLD))));
        this.infoStands.add(new InfoStand(armorStand -> armorStand.customName(Component.text(Manager.numInGame(this) + " in Game").color(NamedTextColor.GREEN))));
        this.infoStands.add(new InfoStand(armorStand -> armorStand.customName(Component.text(Manager.numQueued(this) + " in Queue").color(NamedTextColor.AQUA))));

        Location npcl = this.npc.getLocation();
        for(int i = 0; i < this.infoStands.size(); i++) {
            double yOffset = 2.1 + ((this.infoStands.size() - i - 1) * 0.3);
            Location infoStandLocation = new Location(npcl.getWorld(), npcl.getX(), npcl.getY() + yOffset, npcl.getZ());
            this.infoStands.get(i).spawn(infoStandLocation);
        }
    }

    public void updateInfoStands() {
        for(InfoStand infoStand : this.infoStands) {
            infoStand.update();
        }
    }

    public void removeInfoStands() {
        for(InfoStand infoStand : this.infoStands) {
            infoStand.getArmorStand().remove();
        }
        this.infoStands.clear();
    }

    public void queue(Player p) {
        Queue currQueue = Manager.getQueue(p);

        if(currQueue != null) {
            p.sendMessage(Component.text("You are already in a queue for " + currQueue.getGameType().getName() + ".", NamedTextColor.RED));
            return;
        }

        int largestAvailableQueueSize = -1;
        Queue largestQueue = null;

        for(Queue queue : this.queues) {
            if(!queue.isLocked() && !queue.isFull() && queue.getPlayers().size() > largestAvailableQueueSize) {
                largestQueue = queue;
            }
        }
        
        if(largestQueue != null) {
            largestQueue.addPlayer(p);
            p.sendMessage(Component.text("You have joined a queue for " + this.name + "!", NamedTextColor.GREEN));
        } else {
            p.sendMessage(Component.text("Sorry, but there are currently no queues available for " + this.name + ".", NamedTextColor.RED));
        }
    }

    public String getName() {
        return name;
    }

    public TextColor getColor() {
        return this.color;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public ServerPlayer getSpawnedNpc() {
        return spawnedNpc;
    }

    public NPC getNpc() {
        return this.npc;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public List<GameTeam> getTeams() {
        return teams;
    }

    public List<InfoStand> getInfoStands() {
        return this.infoStands;
    }

    public void setQueues(List<Queue> queues) {
        this.queues = queues;
    }

    public void spawnNpc() {
        this.spawnedNpc = npc.spawn();
    }

    public int getId() {
        return this.id;
    }

    public ArmorStandOrbit getOrbit() {
        return this.orbit;
    }
}
