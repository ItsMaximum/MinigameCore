package com.maximum.minigamecore;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;

public class ProtocolListener {
    private ProtocolManager protocolManager;

    public ProtocolListener(ProtocolManager protocolManager) {
		this.protocolManager = protocolManager;
    }

    public void registerListeners() {
        protocolManager.addPacketListener(
                new PacketAdapter(Main.getInstance(), ListenerPriority.NORMAL,
                        PacketType.Play.Client.USE_ENTITY) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    Player p = event.getPlayer();

                    PacketContainer packet = event.getPacket();
                    int entityId = packet.getIntegers().read(0);

                    try {
                        Hand hand = packet.getEnumEntityUseActions().read(0).getHand();
                        EntityUseAction action = packet.getEnumEntityUseActions().read(0).getAction();

                        if(hand != Hand.MAIN_HAND || action != EntityUseAction.INTERACT) {
                            return;
                        }
                    } catch(Exception e) {

                    }
                        
                    for(GameType gameType : GameConstants.gameTypes) {
                        if(gameType.getSpawnedNpc().getBukkitEntity().getEntityId() == entityId) {

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    gameType.queue(p);
                                }
                            }.runTask(plugin);
                        }
                    }  
                } 
            }
        );
    }
}
