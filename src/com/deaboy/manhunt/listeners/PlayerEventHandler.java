package com.deaboy.manhunt.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.deaboy.manhunt.Manhunt;
import com.deaboy.manhunt.lobby.LobbyType;
import com.deaboy.manhunt.lobby.GameLobby;

public class PlayerEventHandler implements Listener
{
	
	/**
	 * Handles Manhunt chat events. Handles team-exclusive chat
	 * and name colors.
	 * Updated: 1.3
	 * @param e
	 */
	@EventHandler
	public void onAsynchPlayerChat(AsyncPlayerChatEvent e)
	{
		if (!Manhunt.getSettings().HANDLE_CHAT.getValue())
			return;
		
		String message;
		GameLobby lobby;
		List<Player> recipients = new ArrayList<Player>();
		
		if (Manhunt.getPlayerLobby(e.getPlayer()) == null || Manhunt.getPlayerLobby(e.getPlayer()).getType() != LobbyType.GAME)
		{
			recipients.addAll(e.getPlayer().getWorld().getPlayers());
		}
		
		lobby = (GameLobby) Manhunt.getPlayerLobby(e.getPlayer());
		
		if (!lobby.gameIsRunning() || lobby.getSettings().ALL_TALK.getValue())
		{
			recipients.addAll(lobby.getOnlinePlayers());
		}
		else
		{
			recipients.addAll(lobby.getOnlinePlayers(lobby.getPlayerTeam(e.getPlayer())));
		}
		
		if (!recipients.contains(e.getPlayer()))
		{
			recipients.add(e.getPlayer());
		}
		
		message = lobby.getPlayerTeam(e.getPlayer()).getColor() + 
				e.getPlayer().getName() + ChatColor.WHITE + ": " +
				e.getMessage();
		
		for (Player p : recipients)
			p.sendMessage(message);
		
		Manhunt.log(message);
		
		e.setCancelled(true);
	}

	/**
	 * Handles player joining events. Sends them to the main lobby
	 * or broadcasts a return message to the players in the game.
	 * Updated: 1.3
	 * @param e
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		e.setJoinMessage(null);
		
		Manhunt.playerJoinServer(e.getPlayer());
	}
	
	/**
	 * Handles player kick events. Sends them to the main lobby
	 * and removes them from their current lobby (because they
	 * were kicked!)
	 * Updated 1.3
	 * @param e
	 */
	@EventHandler
	public void onPlayerKick(PlayerKickEvent e)
	{
		e.setLeaveMessage(null);
		
		Manhunt.playerLeaveServer(e.getPlayer());
	}

	/**
	 * Handles player quit events. Will initiate a timeout
	 * countdown for that player.
	 * Updated: 1.3
	 * @param e
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		e.setQuitMessage(null);
		
		Manhunt.playerLeaveServer(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLoseFood(FoodLevelChangeEvent e)
	{
		if (e.getEntityType() == EntityType.PLAYER && (Manhunt.getPlayerLobby((Player) e.getEntity()).getType() != LobbyType.GAME || !((GameLobby) Manhunt.getPlayerLobby((Player) e.getEntity())).gameIsRunning()))
		{
			e.setFoodLevel(20);
			((Player) e.getEntity()).setSaturation(20);
		}
	}
	
}