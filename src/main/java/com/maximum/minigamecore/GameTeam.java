package com.maximum.minigamecore;

import org.bukkit.Material;

import net.kyori.adventure.text.format.TextColor;

public class GameTeam {
    private String name;
    private TextColor color;
    private Material material;
    private String distributionType;
    private int queueCap;

    public GameTeam(String name, TextColor color, Material material, String distributionType, int queueCap) {
        this.name = name;
        this.color = color;
        this.material = material;
        this.distributionType = distributionType;
        this.queueCap = queueCap;
    }

    public String getName() {
        return name;
    }

    public TextColor getColor() {
        return color;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public int getQueueCap() {
        return queueCap;
    }
}
