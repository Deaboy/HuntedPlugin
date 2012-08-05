package com.bendude56.hunted;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.bendude56.hunted.chat.ChatManager;
import com.bendude56.hunted.commands.OldCommands;
import com.bendude56.hunted.finder.FinderManager;
import com.bendude56.hunted.games.Game;
import com.bendude56.hunted.listeners.BlockEventHandler;
import com.bendude56.hunted.listeners.EntityEventHandler;
import com.bendude56.hunted.listeners.PlayerEventHandler;
import com.bendude56.hunted.loadouts.LoadoutManager;
import com.bendude56.hunted.settings.SettingsManager;
import com.bendude56.hunted.teams.TeamManager;

public class HuntedPlugin extends JavaPlugin {

	private Logger log = Logger.getLogger("Minecraft");
	
	private SettingsManager	settings;
	private LoadoutManager	loadouts;
	private TeamManager		teams;
	private FinderManager	finders;
	private Game			game;
	private ChatManager		chat;
	
	private World 	manhuntWorld;

	@Override
	public void onEnable()
	{
		//Start up all the classes.

		settings =	new SettingsManager();
		loadouts =	new LoadoutManager();
		teams =		new TeamManager(this);
		chat =		new ChatManager(this);
		game =		null;
		new OldCommands();
		
		manhuntWorld = Bukkit.getWorld(settings.WORLD.value);
		
		//Register Events
		getServer().getPluginManager().registerEvents(new PlayerEventHandler(this), this);
		getServer().getPluginManager().registerEvents(new BlockEventHandler(this), this);
		getServer().getPluginManager().registerEvents(new EntityEventHandler(this), this);
		
	}

	@Override
	public void onDisable()
	{
		
		teams.restoreAllGameModes();
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(ChatColor.DARK_RED + getDescription().getName()
					+ ChatColor.RED + " has been disabled.");
		}
		
		log(Level.INFO, "Unloaded from memory.");
	}

	public void log(Level level, String message) {
		log.log(level, "[" + this.getDescription().getName() + "] " + message);
	}

	public static HuntedPlugin getInstance() {
		return (HuntedPlugin) Bukkit.getServer().getPluginManager().getPlugin("Manhunt");
	}

	public World getWorld() {
		return manhuntWorld;
	}

	public SettingsManager getSettings() {
		return settings;
	}
	
	public LoadoutManager getLoadouts() {
		return loadouts;
	}
	
	public TeamManager getTeams()
	{
		return teams;
	}

	public ChatManager getChat()
	{
		return chat;
	}
	
	public FinderManager getFinders()
	{
		return finders;
	}

	public Game getGame() {
		return game;
	}
	
	public boolean gameIsRunning()
	{
		return game != null;
	}

}
