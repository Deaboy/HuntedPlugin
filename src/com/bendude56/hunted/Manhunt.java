package com.bendude56.hunted;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

import com.bendude56.hunted.lobby.GameLobby;
import com.bendude56.hunted.lobby.MainLobby;
import com.bendude56.hunted.lobby.ManhuntGameLobby;
import com.bendude56.hunted.settings.ManhuntSettings;

public class Manhunt
{
	
	//-------- Constants --------//
	
	public static final String extension_properties = ".properties";
	public static final String extension_loadout = ".dat";
	
	public static final String filename_settings = "config"; 
	
	public static final String dirname_plugins = "plugins";
	public static final String dirname_main = "Manhunt";
	public static final String dirname_loadouts = "loadouts";
	public static final String dirname_world = "Manhunt";
	
	public static final String path_maindir = dirname_plugins + "/" + dirname_main;
	public static final String path_settings = path_maindir + "/" + filename_settings + extension_properties;
	public static final String path_loadouts = path_maindir + "/" + dirname_loadouts;
	
	
	//-------- Local variables ---------//
	
	private MainLobby mainlobby;
	private List<GameLobby> lobbies;
	private ManhuntSettings settings;	
	
	
	//-------- Constructor --------//
	public Manhunt()
	{
		this.lobbies = new ArrayList<GameLobby>();
		this.settings = new ManhuntSettings(path_settings);
		
		loadLobbiesFromFile();
	}
	
	
	//-------- Public static methods --------//
	public static MainLobby getMainLobby()
	{
		return getInstance().mainlobby;
	}
	
	public static ManhuntSettings getSettings()
	{
		return getInstance().settings;
	}
	
	public static GameLobby getLobby(String name)
	{
		for (GameLobby lobby : getInstance().lobbies)
		{
			if (lobby.getName().equalsIgnoreCase(name))
			{
				return lobby;
			}
		}
		return null;
		
	}
	
	public static GameLobby getLobby(World world)
	{
		for (GameLobby lobby : getInstance().lobbies)
		{
			if (lobby.getWorlds().contains(world))
			{
				return lobby;
			}
		}
		return null;
		
	}
	
	private static Manhunt getInstance()
	{
		return NewManhuntPlugin.getManhuntInstance();
	}
	
	
	//-------- Private methods --------//
	private void loadLobbiesFromFile()
	{
		// TODO Finish this method
	}
	
	
	//-------- Public Interface Methods --------//
	public static GameLobby createLobby(String name, World world)
	{
		GameLobby lobby = null;
		
		lobby = getLobby(name);
		if (lobby != null)
		{
			return null;
		}
		lobby = getLobby(world);
		if (lobby != null)
		{
			return null;
		}
		
		lobby = new ManhuntGameLobby(name, world);
		
		getInstance().lobbies.add(new ManhuntGameLobby(name, world));
		
		return lobby;
	}
	
	public static void destroyLobby(GameLobby lobby)
	{
		if (!getInstance().lobbies.contains(lobby))
		{
			return;
		}
		
		getInstance().lobbies.remove(lobby);
		
	}
	
	
	
	
}
