package com.maximum.minigamecore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public class TeamGUI {
	
	public TeamGUI(Player player, Queue queue) {
        TextComponent inventoryName = Component.text("Team Selection");
        GameType gameType = queue.getGameType();
		
		Inventory gui = Bukkit.createInventory(null,  9, inventoryName);
		
		for (GameTeam team : gameType.getTeams()) {
			ItemStack is = new ItemStack(team.getMaterial());
			ItemMeta isMeta = is.getItemMeta();

            Style style = Style.style()
                .color(team.getColor())
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.UNDERLINED, true)
                .build();

            Component teamName = Component.text(team.getName(), style);
			isMeta.displayName(teamName);
            isMeta.lore(Manager.buildTeamLore(queue, team));
			is.setItemMeta(isMeta);
			
			gui.addItem(is);
		}
		
		player.openInventory(gui); // Open the inventory of the player
	}

}
