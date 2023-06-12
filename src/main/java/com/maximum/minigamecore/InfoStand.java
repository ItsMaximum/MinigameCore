package com.maximum.minigamecore;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import net.kyori.adventure.text.Component;

public class InfoStand {
    private ArmorStand armorStand;
    private Component staticText;
	private final Consumer<ArmorStand> updateAction;	

	public InfoStand(Consumer<ArmorStand> updateAction) {
        this.updateAction = updateAction;
	}

    public InfoStand(Component staticText) {
        this.staticText = staticText;
        this.updateAction = null;
	}

    public void spawn(Location l) {
        this.armorStand = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        this.armorStand.setMarker(true);
		this.armorStand.setCustomNameVisible(true);
		this.armorStand.setCollidable(false);
		this.armorStand.setInvulnerable(true);
		this.armorStand.setInvisible(true);
		this.armorStand.setCanPickupItems(false);
		this.armorStand.setGravity(false);

        if(this.updateAction != null) {
        	this.update();
		} else {
            this.armorStand.customName(staticText);
        }
    }

	public void update() {
		if(this.updateAction != null) {
        	this.updateAction.accept(this.armorStand);
		}
	}

    public ArmorStand getArmorStand() {
        return this.armorStand;
    }
}
