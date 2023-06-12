package com.maximum.minigamecore;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.PosRot;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandOrbit {
    private double radius;
    private int step;
    private double centerX;
    private double centerY;
    private double centerZ;

    public ArmorStandOrbit(double radius, int step, double centerX, double centerY, double centerZ) {
        this.radius = radius;
        this.step = step;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
    }

    public BukkitTask orbit(Player player, Location end) {
        return orbit(player, null, end);
    }

    public BukkitTask orbit(Player player, Location start, Location end) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer serverPlayer = craftPlayer.getHandle();

        double startAngle;
        double endRadius;
        if(start != null) {
            startAngle = Math.toRadians(end.getYaw() + 180);
            endRadius = Math.sqrt(Math.pow(end.getX() - centerX, 2) + Math.pow(end.getZ() - centerZ, 2));
        } else if(centerX == end.getX() && centerZ == end.getZ()) {
            startAngle = Math.toRadians(end.getYaw() + 180);
            endRadius = 0;
        } else {
            startAngle = Math.atan2((centerX - end.getX()), (end.getZ() - centerZ));
            endRadius = Math.sqrt(Math.pow(end.getX() - centerX, 2) + Math.pow(end.getZ() - centerZ, 2));
        }

        double actualEndY = end.getY() - 0.175;
        double endYaw = end.getYaw() % 360;
        boolean clockwise = endYaw - Math.toDegrees(startAngle) >= 0;

        double startX = centerX + (-radius * Math.sin(startAngle));
        double startY = centerY;
        double startZ = centerZ + (radius * Math.cos(startAngle));

        player.setInvisible(true);
        player.teleport(new Location(end.getWorld(), startX, startY, startZ));
        /*
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, -1, 255, false, false));
        Title title = Title.title(Component.text("Loading...").color(NamedTextColor.RED), Component.empty());
        player.showTitle(title);
        */

        ArmorStand armorStand = new ArmorStand(serverPlayer.getLevel(), startX, startY, startZ);
        armorStand.setYHeadRot((float) ((Math.toDegrees(startAngle) + 180) % 360));
        ClientboundAddEntityPacket entityPacket = new ClientboundAddEntityPacket(armorStand);
        serverPlayer.connection.send(entityPacket);

        List<SynchedEntityData.DataValue<?>> entityData = new ArrayList<>();
        entityData.add(new SynchedEntityData.DataItem<>(new EntityDataAccessor<>(0,EntityDataSerializers.BYTE),(byte) 32).value());
        ClientboundSetEntityDataPacket armorStandData = new ClientboundSetEntityDataPacket(armorStand.getId(), entityData);
        serverPlayer.connection.send(armorStandData);

        ClientboundSetCameraPacket cameraPacket = new ClientboundSetCameraPacket(armorStand);
        serverPlayer.connection.send(cameraPacket);

        return new BukkitRunnable() {
            private double angle = startAngle;
            private double radianStep = ((2 * Math.PI) / 256) * step;
            private int iteration = 0;
            private int totalIterations = 256 / step;
            private boolean isRotating = true;
            private boolean easeInRadius = radius > endRadius;
            private boolean easeInY = startY > actualEndY;

            private short prevX = (short) (startX * 4096);
            private short prevY = (short) (startY * 4096);
            private short prevZ = (short) (startZ * 4096);
            
            @Override
            public void run() {
                angle += (clockwise ? radianStep : -radianStep);

                double progress = iteration / (double) totalIterations;
                double radiusEase = easeInRadius ? Math.pow(progress, 3) : Math.pow(progress - 1, 3) + 1;
                double currRadius = radius - ((radius - endRadius) * radiusEase);

                //Main.getInstance().getLogger().info("Curr radius " + currRadius + "End Radius " + endRadius + " Clockwise " + clockwise + " End yaw " + endYaw + " Curr yaw " + (Math.toDegrees(angle) + 180) % 360 + " Start angle " + startAngle);
                double yEase = easeInY ? Math.pow(progress, 3) : Math.pow(progress - 1, 3) + 1;
                double currXDouble = centerX + (-currRadius * Math.sin(angle));
                double currYDouble = startY - ((startY - actualEndY) * yEase);
                double currZDouble = centerZ + (currRadius * Math.cos(angle));

                short currX = (short) (currXDouble * 4096);
                short currY = (short) (currYDouble * 4096);
                short currZ = (short) (currZDouble * 4096);

                short deltaX = (short) (currX - prevX);
                short deltaY = (short) (currY - prevY);
                short deltaZ = (short) (currZ - prevZ);

                PosRot standRotPosPacket = new PosRot(armorStand.getId(), deltaX, deltaY, deltaZ, (byte) 0, (byte) (end.getPitch() * 256 / 360), false);
                serverPlayer.connection.send(standRotPosPacket);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(new Location(end.getWorld(), currXDouble, currYDouble, currZDouble));
                    }
                }.runTask(Main.getInstance());

                double doubleYaw = (Math.toDegrees(angle) + 180) % 360;

                if(isRotating) {
                    byte byteYaw = (byte) (doubleYaw * 256 / 360);
                    if(iteration > 2 && Math.abs(doubleYaw - endYaw) < (step / 256.0)) {
                        byteYaw = (byte) (endYaw * 256 / 360);
                        isRotating = false;
                    }

                    ClientboundRotateHeadPacket rotHeadPacket = new ClientboundRotateHeadPacket(armorStand, byteYaw);
                    serverPlayer.connection.send(rotHeadPacket);
                }

                if(iteration == totalIterations) {
                    this.cancel();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(end);
                            ClientboundSetCameraPacket resetCameraPacket = new ClientboundSetCameraPacket(serverPlayer);
                            serverPlayer.connection.send(resetCameraPacket); 
                            player.setInvisible(false);
                        }
                    }.runTaskLater(Main.getInstance(), 3L);
                }
                
                prevX = currX;
                prevY = currY;
                prevZ = currZ;
                iteration++;
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 1L, 1L);
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getCenterZ() {
        return centerZ;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public void setCenterZ(double centerZ) {
        this.centerZ = centerZ;
    }
}
