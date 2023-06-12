package com.maximum.minigamecore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class NPC {
	private Location location;
	private String skinTexture;
	private String skinSignature;
	
	public NPC(Location location, String skinTexture, String skinSignature) {
		this.location = location;
		this.skinTexture = skinTexture;
		this.skinSignature = skinSignature;
	}
	
	public ServerPlayer spawn() {
		MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
		ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "Click to Join!");
		ServerPlayer npc = new ServerPlayer(nmsServer, nmsWorld, gameProfile);
		npc.setPos(location.getX(), location.getY(), location.getZ());
		
		gameProfile.getProperties().put("textures", new Property("textures", skinTexture, skinSignature));
		
		return npc;
	}
	
	public static void addNPCPacketForPlayer(ServerPlayer npc, Player player, Location npcl) {
		ServerGamePacketListenerImpl listener = ((CraftPlayer) player).getHandle().connection;
		npc.getEntityData().set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte)127);
		listener.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc));
        listener.send(new ClientboundAddPlayerPacket(npc));
        listener.send(new ClientboundRotateHeadPacket(npc, (byte) (npcl.getYaw()*256/360)));
        listener.send(new ClientboundMoveEntityPacket.Rot(npc.getBukkitEntity().getEntityId(), (byte) (npcl.getYaw()*256/360), (byte) (npcl.getPitch()*256/360), false));
        List<SynchedEntityData.DataValue<?>> entityData = new ArrayList<>();
		entityData.add(new SynchedEntityData.DataItem<>(new EntityDataAccessor<>(17,EntityDataSerializers.BYTE),(byte)127).value());
		listener.send(new ClientboundSetEntityDataPacket(npc.getId(), entityData));
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
        	@Override
        	public void run() {
        		listener.send(new ClientboundPlayerInfoRemovePacket(List.of(npc.getUUID())));
        	}
        },40L);
        
	}
	
	public static void removeNPC(ServerPlayer npc) {
		for(Player player : Bukkit.getOnlinePlayers()){
			ServerGamePacketListenerImpl listener = ((CraftPlayer) player).getHandle().connection;
			//listener.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
			listener.send(new ClientboundRemoveEntitiesPacket(npc.getBukkitEntity().getEntityId()));
	    }
	}

	public Location getLocation() {
		return this.location;
	}
}
