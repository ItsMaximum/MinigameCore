package com.maximum.minigamecore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GameListener implements Listener {
    @EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

        if(Manager.inLobby(p)) {
            Manager.removeFromQueues(p);
        }

		Manager.resetPlayer(p);
		Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Manager.updateMainScoreboard(), 2L);
	}

    @EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		Manager.resetPlayer(p);
		Manager.updateMainScoreboard();
	}

    @EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();

        if(Manager.inLobby(p)) {
            if(!p.isOp()) {
			    e.setCancelled(true);
            }
		}
	}

	@EventHandler
	public void onFoodLost(FoodLevelChangeEvent e) {
		if(!(e.getEntity() instanceof Player)) {
			return;
		}

		Player p = (Player) e.getEntity();
		if(Manager.inLobby(p)) {
			p.setFoodLevel(20);
			p.setSaturation(20);
			e.setCancelled(true);
		}
	}

    @EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();

        if(Manager.inLobby(p)) {
            if(!p.isOp()) {
			    e.setCancelled(true);
            }
		}
	}

    @EventHandler
    public void damage(EntityDamageEvent e) //Listens to EntityDamageEvent
	{
		if (e.isCancelled() || !(e.getEntity() instanceof Player)) {
	        return;
	    }
		
		DamageCause cause = e.getCause();
		Player p = (Player) e.getEntity();

		if(Manager.inLobby(p)) {
			e.setCancelled(true);
			if(cause.equals(DamageCause.LAVA) || cause.equals(DamageCause.FIRE)) {
				Manager.refreshPlayer(p);
				p.teleport(GameConstants.lobbySpawn);
			} else if (cause.equals(DamageCause.FIRE_TICK)) {
				p.setFireTicks(0);
			}
		}
	}

    @EventHandler
	public void onPaintingBreak(HangingBreakByEntityEvent e) {
		/*
		if(!(e.getRemover() instanceof Player)) {
			return;
		}

        Player p = (Player) e.getRemover();
		*/
		e.setCancelled(true);
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractAtEntityEvent e) {
		
		Player p = e.getPlayer();
		Entity entity = e.getRightClicked();
		
		if(p == null || entity == null) {
			return;
		}

		if(Manager.inLobby(p)) {
            if (entity.getType().equals(EntityType.ARMOR_STAND)) {
				Log.info("Clicked an armor stand!");
			}
		}
	}

	@EventHandler
	public void move(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Location l = e.getTo();
		
		if(Manager.inLobby(p)) {
            if(Math.abs(l.getX() - GameConstants.lobbySpawn.getX()) > 500) {
				Manager.refreshPlayer(p);
				p.teleport(GameConstants.lobbySpawn);
			} else if(Math.abs(l.getY() - GameConstants.lobbySpawn.getY()) > 100) {
				Manager.refreshPlayer(p);
				p.teleport(GameConstants.lobbySpawn);
			} else if(Math.abs(l.getZ() - GameConstants.lobbySpawn.getZ()) > 500) {
				Manager.refreshPlayer(p);
				p.teleport(GameConstants.lobbySpawn);
			}
		}	
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		
		Player p = e.getPlayer();
		
		if(e.getAction() == null) {
			return;
		}
		
		if(Manager.inLobby(p)) {
			switch(e.getAction()) {
			case LEFT_CLICK_AIR:
			case RIGHT_CLICK_AIR:
			case LEFT_CLICK_BLOCK:
			case RIGHT_CLICK_BLOCK:
				break;
			default:
				return;
			}

			ItemStack is = e.getItem();
			if(is == null) {
				return;
			}

			Material itemType = is.getType();
			if(!itemType.equals(Material.COMPASS) && !itemType.equals(Material.RED_BED)) {
				return;
			}

			Queue q = Manager.getQueue(p);
			if(q == null) {
				return;
			}

			if (itemType.equals(Material.COMPASS)) {
				new TeamGUI(p, q);
			} else if(itemType.equals(Material.RED_BED)) {
				q.removePlayer(p);
				p.sendMessage(Component.text("You have left the queue!", NamedTextColor.RED));
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		
		Player p = (Player) e.getWhoClicked();

		if(Manager.inLobby(p)) {
			if(!e.getView().getType().equals(InventoryType.CHEST)) {
				return;
			}

			Component title = e.getView().title();

			if(!title.contains(Component.text("Team Selection"), Component.EQUALS) || e.getRawSlot() >= 9) {
				return;
			}

			if(e.getCurrentItem() == null) {
				e.setCancelled(true);
				return;
			}

			Queue queue = Manager.getQueue(p);
			GameType gameType = queue.getGameType();

			for(GameTeam team : gameType.getTeams()) {
				if(team.getMaterial().equals(e.getCurrentItem().getType())) {
					queue.assignTeam(p, team);
					break;
				}
			}

			e.setCancelled(true);
			p.closeInventory();
		}
	}

	@EventHandler
	public void onChat(AsyncChatEvent e) {
		e.setCancelled(true);
		Player chatter = e.getPlayer();
		Component message = Component.text()
			.append(Component.text("<"))
			.append(chatter.displayName())
			.append(Component.text("> "))
			.append(e.message())
			.build();
		for(Player p : chatter.getWorld().getPlayers()) {
			p.sendMessage(message);
		}
	}
}
