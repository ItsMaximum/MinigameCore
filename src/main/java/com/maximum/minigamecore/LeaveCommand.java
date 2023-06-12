package com.maximum.minigamecore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LeaveCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!(sender instanceof Player)) {
            return false;
        }    
		
		Player p = (Player) sender;

        if(!Manager.inLobby(p)) {
            PlayerLeaveEvent e = new PlayerLeaveEvent(p);
            Bukkit.getServer().getPluginManager().callEvent(e);
            p.sendMessage(Component.text("Returning to the lobby!", NamedTextColor.GREEN));
            return true;
        }

        Queue q = Manager.getQueue(p);
        if(q != null) {
            q.removePlayer(p);
            p.sendMessage(Component.text("You have left the queue!", NamedTextColor.RED));
        } else {
            p.sendMessage(Component.text("You are not in a queue / game!", NamedTextColor.RED));
        }
        return true;
	}
}
