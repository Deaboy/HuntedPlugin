package com.bendude56.hunted.lobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.bendude56.hunted.map.ManhuntSpawn;
import com.bendude56.hunted.map.Spawn;
import com.bendude56.hunted.map.Map;
import com.bendude56.hunted.settings.WorldSettings;

public class ManhuntGameLobby extends ManhuntLobby implements GameLobby
{
	
	//---------------- Properties ----------------//
	String name;
	
	private HashMap<Player, Team> players;
	
	private Map current_map;
	private List<Map> maps;
	private WorldSettings settings;
	
	
	//---------------- Constructors ----------------//
	public ManhuntGameLobby(String name, World world)
	{
		this(name, world.getSpawnLocation());
	}
	
	public ManhuntGameLobby(String name, Location loc)
	{
		this(name, new ManhuntSpawn(loc));
	}
	
	public ManhuntGameLobby(String name, Spawn spawn)
	{
		super(spawn);
		
		this.name = name;
		
		players = new HashMap<Player, Team>();
		
		maps = new ArrayList<Map>();
		settings = new WorldSettings(spawn.getWorld());
	}
	
	
	//---------------- Getters ----------------//
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public List<World> getWorlds()
	{
		List<World> worlds = new ArrayList<World>();
		for (Map map : getMaps())
		{
			if (!worlds.contains(map.getWorld()))
				worlds.add(map.getWorld());
		}
		return worlds;
	}
	
	@Override
	public Map getCurrentMap()
	{
		return current_map;
	}

	@Override
	public List<Map> getMaps()
	{
		return maps;
	}

	@Override
	public List<Player> getPlayers()
	{
		return new ArrayList<Player>(players.keySet());
	}

	@Override
	public List<Player> getPlayers(Team...teams)
	{
		List<Player> plrs = new ArrayList<Player>();
		for (Player p : players.keySet())
		{
			for (Team t : teams)
			{
				if (players.get(p) == t)
				{
					plrs.add(p);
					break;
				}
			}
		}
		return plrs;
	}

	@Override
	public Team getPlayerTeam(Player p)
	{
		if (players.containsKey(p))
			return players.get(p);
		else
			return null;
	}

	@Override
	public WorldSettings getSettings()
	{
		return settings;
	}
	
	
	//---------------- Setters ----------------//
	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public void addPlayer(Player p)
	{
		setPlayerTeam(p, Team.SPECTATORS);
	}

	@Override
	public void addPlayer(Player p, Team t)
	{
		if (!players.containsKey(p))
		{
			players.put(p, t);
		}
	}

	@Override
	public void setPlayerTeam(Player p, Team t)
	{
		if (players.containsKey(p))
		{
			players.put(p, t);
		}
	}

	@Override
	public void setAllPlayerTeam(Team t)
	{
		for (Player p : players.keySet())
		{
			players.put(p, t);
		}
	}

	@Override
	public void removePlayer(Player p)
	{
		if (players.containsKey(p))
		{
			players.remove(p);
		}
	}
	
	
	//---------------- Public Methods ----------------//
	@Override
	public void broadcast(String message)
	{
		for (Player p : players.keySet())
		{
			if (p.isOnline())
			{
				p.sendMessage(message);
			}
		}
	}

	@Override
	public void broadcast(String message, Team...teams)
	{
		for (Player p : players.keySet())
		{
			if (p.isOnline())
			{
				for (Team t : teams)
				{
					if (players.get(p) == t)
						p.sendMessage(message);
				}
			}
		}
	}

	@Override
	public void clearPlayers()
	{
		players.clear();
	}
	
	@Override
	public void startGame()
	{
		current_map = maps.get(((int) Math.random()) % maps.size());
	}
	
	@Override
	public void stopGame()
	{
		current_map = null;
	}
	
}
