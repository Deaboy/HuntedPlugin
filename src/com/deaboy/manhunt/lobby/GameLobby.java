package com.deaboy.manhunt.lobby;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.deaboy.amber.Amber;
import com.deaboy.manhunt.Manhunt;
import com.deaboy.manhunt.ManhuntPlugin;
import com.deaboy.manhunt.ManhuntUtil;
import com.deaboy.manhunt.chat.ChatManager;
import com.deaboy.manhunt.game.Game;
import com.deaboy.manhunt.game.GameClass;
import com.deaboy.manhunt.loadouts.Loadout;
import com.deaboy.manhunt.map.Map;
import com.deaboy.manhunt.map.World;
import com.deaboy.manhunt.settings.GameLobbySettings;
import com.deaboy.manhunt.settings.SettingsPack;

public abstract class GameLobby extends Lobby
{
	//////////////// PROPERTIES ////////////////
	private Game game;
	private List<String> maps;
	private Map current_map;
	private HashMap<String, Team> teams;
	private List<String> prey_loadouts;
	private List<String> hunter_loadouts;
	
	
	//////////////// CONSTRUCTORS ////////////////
	public GameLobby(long id, File file)
	{
		super(id, file);
		this.game = null;
		this.maps = new ArrayList<String>();
		this.current_map = null;
		this.teams = new HashMap<String, Team>();
		this.prey_loadouts = new ArrayList<String>();
		this.hunter_loadouts = new ArrayList<String>();
	}
	public GameLobby(long id, File file, String name, Location loc)
	{
		super(id, file, name, loc);
		this.game = null;
		this.maps = new ArrayList<String>();
		this.current_map = null;
		this.teams = new HashMap<String, Team>();
		this.prey_loadouts = new ArrayList<String>();
		this.hunter_loadouts = new ArrayList<String>();
	}
	
	
	//////////////// PUBLIC FUNCTIONS ////////////////
	//---------------- INTERFACE ----------------//
	public abstract boolean playerForfeit(String name);
	public abstract boolean playerChangeTeam(String player, Team team);
	public abstract boolean registerMap(Map map);
	public abstract boolean unregisterMap(Map map);
	
	
	//---------------- PLAYERS ----------------//
	protected boolean addPlayer(Player player, Team team)
	{
		if (player != null)
		{
			return addPlayer(player.getName(), team);
		}
		else
		{
			return false;
		}
	}
	protected boolean addPlayer(String name, Team team)
	{
		if (team != null && !teams.containsKey(name))
		{
			this.teams.put(name, team);
			Manhunt.unlockPlayer(name);
			return true;
		}
		else
		{
			return false;
		}
	}
	@Override
	protected boolean addPlayer(Player player)
	{
		if (player != null)
		{
			return addPlayer(player.getName());
		}
		else
		{
			return false;
		}
	}
	@Override
	protected boolean addPlayer(String name)
	{
		return addPlayer(name, Team.STANDBY);
	}
	@Override
	public boolean containsPlayer(Player player)
	{
		if (player != null)
		{
			return containsPlayer(player.getName());
		}
		else
		{
			return false;
		}
	}
	@Override
	public boolean containsPlayer(String name)
	{
		return this.teams.containsKey(name);
	}
	@Override
	protected boolean removePlayer(Player player)
	{
		if (player != null)
		{
			return removePlayer(player.getName());
		}
		else
		{
			return false;
		}
	}
	@Override
	protected boolean removePlayer(String name)
	{
		if (this.teams.containsKey(name))
		{
			this.teams.remove(name);
			Manhunt.unlockPlayer(name);
			return true;
		}
		else
		{
			return false;
		}
	}
	@Override
	protected void clearPlayers()
	{
		this.teams.clear();
	}
	protected void clearOfflinePlayers()
	{
		for (String name : getOfflinePlayerNames())
		{
			this.teams.remove(name);
		}
	}
	
	@Override
	public List<String> getPlayerNames()
	{
		return new ArrayList<String>(this.teams.keySet());
	}
	public List<String> getPlayerNames(Team...teams)
	{
		List<String> names = new ArrayList<String>();
		for (String name : this.teams.keySet())
		{
			if (arrContains(this.teams.get(name), teams))
				names.add(name);
		}
		return names;
	}
	@Override
	public List<String> getOnlinePlayerNames()
	{
		List<String> names = new ArrayList<String>();
		for (String name : this.teams.keySet())
		{
			if (Bukkit.getPlayerExact(name) != null)
				names.add(name);
		}
		return names;
	}
	public List<String> getOnlinePlayerNames(Team...teams)
	{
		List<String> names = new ArrayList<String>();
		for (String name : this.teams.keySet())
		{
			if (Bukkit.getPlayerExact(name) != null && arrContains(this.teams.get(name), teams))
				names.add(name);
		}
		return names;
	}
	@Override
	public List<String> getOfflinePlayerNames()
	{
		List<String> names = new ArrayList<String>();
		for (String name : this.teams.keySet())
		{
			if (Bukkit.getPlayerExact(name) == null)
				names.add(name);
		}
		return names;
	}
	public List<String> getOfflinePlayerNames(Team...teams)
	{
		List<String> names = new ArrayList<String>();
		for (String name : this.teams.keySet())
		{
			if (Bukkit.getPlayerExact(name) == null && arrContains(this.teams.get(name), teams))
				names.add(name);
		}
		return names;
	}
	@Override
	public List<Player> getOnlinePlayers()
	{
		List<Player> players = new ArrayList<Player>();
		for (String name : this.teams.keySet())
		{
			if (Bukkit.getPlayerExact(name) != null)
				players.add(Bukkit.getPlayerExact(name));
		}
		return players;
	}
	public List<Player> getOnlinePlayers(Team...teams)
	{
		List<Player> players = new ArrayList<Player>();
		for (String name : this.teams.keySet())
		{
			if (Bukkit.getPlayerExact(name) != null && arrContains(this.teams.get(name), teams))
				players.add(Bukkit.getPlayerExact(name));
		}
		return players;
	}
	@Override
	public List<OfflinePlayer> getOfflinePlayers()
	{
		List<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
		for (String name : this.teams.keySet())
		{
			if (Bukkit.getPlayerExact(name) == null)
				players.add(Bukkit.getOfflinePlayer(name));
		}
		return players;
	}
	public List<OfflinePlayer> getOfflinePlayers(Team...teams)
	{
		List<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
		for (String name : this.teams.keySet())
		{
			if (Bukkit.getPlayerExact(name) == null && arrContains(this.teams.get(name), teams))
				players.add(Bukkit.getOfflinePlayer(name));
		}
		return players;
	}
	
	
	@Override
	public void broadcast(String message)
	{
		Player player;
		for (String name : this.teams.keySet())
		{
			player = Bukkit.getPlayerExact(name);
			if (player != null)
				player.sendMessage(message);
		}
	}
	public void broadcast(String message, Team...teams)
	{
		if (teams.length == 0)
		{
			broadcast(message);
			return;
		}
		
		Player player;
		for (String name : this.teams.keySet())
		{
			if (arrContains(this.teams.get(name), teams))
			{
				player = Bukkit.getPlayerExact(name);
				if (player != null)
				{
					player.sendMessage(message);
				}
			}
		}
	}
	
	private boolean arrContains(Object o, Object[] a)
	{
		for (Object b : a)
			if (o == b)
				return true;
		return false;
	}
	
	
	//---------------- TEAMS -----------------//
	protected void setPlayerTeam(Player player, Team team)
	{
		if (player != null)
			setPlayerTeam(player.getName(), team);
	}
	protected void setPlayerTeam(String name, Team team)
	{
		if (team != null && this.teams.containsKey(name))
			this.teams.put(name, team);
	}
	protected void setAllPlayerTeams(Team team)
	{
		if (team != null)
		{
			for (String name : this.teams.keySet())
			{
				this.teams.put(name, team);
			}
		}
	}
	public Team getPlayerTeam(Player p)
	{
		if (p != null)
		{
			return getPlayerTeam(p.getName());
		}
		else
		{
			return null;
		}
	}
	public Team getPlayerTeam(String name)
	{
		if (this.teams.containsKey(name))
		{
			return this.teams.get(name);
		}
		else
		{
			return null;
		}
	}
	protected void distributeTeams()
	{
		if (this.game != null)
			this.game.distributeTeams();
	}
	
	
	//---------------- GAMES ----------------//
	protected Game getGame()
	{
		return this.game;
	}
	public boolean gameIsRunning()
	{
		if (this.game != null)
		{
			return this.game.isRunning();
		}
		else
		{
			return false;
		}
	}
	public boolean startGame()
	{
		if (game == null || gameIsRunning())
		{
			return false;
		}
		chooseMap();
		if (getCurrentMap() == null || Manhunt.getLobby(getCurrentMap().getWorld().getWorld()) != null && Manhunt.getLobby(getCurrentMap().getWorld().getWorld()).getType() == LobbyType.GAME && ((GameLobby) Manhunt.getLobby(getCurrentMap().getWorld().getWorld())).gameIsRunning())
		{
			return false;
		}
		if (getSettings().USE_AMBER.getValue())
		{
			if (Amber.getWorldRecorder(getCurrentMap().getWorld().getWorld()).isIdle())
			{
				Amber.startRecordingWorld(getCurrentMap().getWorld().getWorld(), ManhuntPlugin.getInstance());
			}
			else
			{
				return false;
			}
		}
		distributeTeams();
		game.setMap(getCurrentMap());
		game.startGame();
		return true;
	}
	public boolean endGame()
	{
		if (game == null || !gameIsRunning())
		{
			return false;
		}
		game.endGame();
		stopGame();
		return true;
	}
	public boolean cancelGame()
	{
		if (game == null || !gameIsRunning())
		{
			return false;
		}
		game.cancelGame();
		stopGame();
		return true;
	}
	protected void stopGame()
	{
		for (String playername : getPlayerNames())
		{
			Manhunt.unlockPlayer(playername);
		}
		clearOfflinePlayers();
		for (Player player : getOnlinePlayers(Team.HUNTERS, Team.PREY, Team.SPECTATORS))
		{
			player.teleport(ManhuntUtil.safeTeleport(getRandomSpawnLocation()));
			setPlayerTeam(player, Team.STANDBY);
			ManhuntUtil.resetPlayer(player);
			player.setGameMode(GameMode.ADVENTURE);
		}
		if (getSettings().USE_AMBER.getValue())
		{
			Amber.stopRecordingWorld(getCurrentMap().getWorld().getWorld());
			Amber.startRestoringWorld(getCurrentMap().getWorld().getWorld(), ManhuntPlugin.getInstance(), new Runnable()
			{
				public void run()
				{
					onWorldRestore();
				}
			});
		}
	}
	protected void onWorldRestore()
	{
		getCurrentMap().getWorld().getWorld().setTime(1000);
	}
	public boolean setGameClass(GameClass gameclass)
	{
		if (gameIsRunning())
			return false;
		
		Game game = gameclass.createInstance(this);
		if (game == null)
			return false;
		this.game = game;
		
		broadcast(ChatManager.leftborder + "Game has been changed to " + ChatColor.DARK_BLUE + gameclass.getName());
		Manhunt.log('[' + getName() + "] Game changed to " + gameclass.getName());
		saveFiles();
		return true;
	}
	public abstract long getGameStartTime();
	public abstract long getGameTicksRemaining();
	public abstract void resetIntermissionTime();
	
	
	//---------------- MAPS ----------------//
	protected boolean addMap(Map map)
	{
		if (map != null && !this.maps.contains(map.getName()))
		{
			this.maps.add(map.getFullName());
			return true;
		}
		else
		{
			return false;
		}
	}
	public boolean containsMap(Map map)
	{
		if (map != null)
		{
			return containsMap(map.getFullName());
		}
		else
		{
			return false;
		}
	}
	public boolean containsMap(String name)
	{
		return this.maps.contains(name);
	}
	protected boolean removeMap(Map map)
	{
		if (map != null)
		{
			return removeMap(map.getFullName());
		}
		else
		{
			return false;
		}
	}
	protected boolean removeMap(String name)
	{
		if (this.maps.contains(name))
		{
			this.maps.remove(name);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	protected boolean setCurrentMap(String fullmapname)
	{
		if (this.maps.contains(fullmapname))
		{
			this.current_map = Manhunt.getMap(fullmapname);
			return true;
		}
		else
		{
			return false;
		}
	}
	public String chooseMap()
	{
		String map;
		
		if (gameIsRunning())
			return null;
		if (maps.isEmpty())
			return null;
		
		Collections.sort(maps, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		} );
		
		map = maps.get((int) (Math.random()*maps.size()));
		setCurrentMap(map);
		return map;
	}
	public Map getCurrentMap()
	{
		return current_map;
	}
	public List<Map> getMaps()
	{
		List<Map> maplist = new ArrayList<Map>();
		Map map;
		
		for (String mapname : maps)
		{
			map = Manhunt.getMap(mapname);
			if (map != null)
				maplist.add(map);
		}
		
		return maplist;
	}
	public List<World> getWorlds()
	{
		Map map;
		List<World> worlds = new ArrayList<World>();
		
		for (String mapname : maps)
		{
			map = Manhunt.getMap(mapname);
			if (map != null)
				worlds.add(map.getWorld());
		}
		
		if (!worlds.contains(getWorld()))
			worlds.add(getWorld());
		
		return worlds;
	}
	
	
	//---------------- LOADOUTS ----------------//
	public boolean addHunterLoadout(Loadout loadout)
	{
		if (loadout == null)
			return false;
		if (this.hunter_loadouts.contains(loadout.getName()))
			return false;
		
		this.hunter_loadouts.add(loadout.getName());
		return true;
	}
	public boolean addPreyLoadout(Loadout loadout)
	{
		if (loadout == null)
			return false;
		if (this.prey_loadouts.contains(loadout.getName()))
			return false;
		
		this.prey_loadouts.add(loadout.getName());
		return true;
	}
	public boolean removeLoadout(String name)
	{
		if (name != null && (this.hunter_loadouts.contains(name) || this.prey_loadouts.contains(name)))
		{
			if (this.hunter_loadouts.contains(name))
				this.hunter_loadouts.remove(name);
			if (this.prey_loadouts.contains(name))
				this.prey_loadouts.remove(name);
			return true;
		}
		else
		{
			return false;
		}
	}
	public boolean removeHunterLoadout(String name)
	{
		if (name != null && this.hunter_loadouts.contains(name))
		{
			this.hunter_loadouts.remove(name);
			return true;
		}
		else
		{
			return false;
		}
	}
	public boolean removePreyLoadout(String name)
	{
		if (name != null && this.prey_loadouts.contains(name))
		{
			this.prey_loadouts.remove(name);
			return true;
		}
		else
		{
			return false;
		}
	}
	public List<Loadout> getLoadouts()
	{
		List<Loadout> loadouts = new ArrayList<Loadout>();
		loadouts.addAll(getHunterLoadouts());
		for (Loadout loadout : getPreyLoadouts())
		{
			if (!loadouts.contains(loadout))
			{
				loadouts.add(loadout);
			}
		}
		return loadouts;
	}
	public List<Loadout> getHunterLoadouts()
	{
		List<Loadout> loadouts = new ArrayList<Loadout>();
		for (String loadoutname : this.hunter_loadouts)
		{
			if (Manhunt.getLoadout(loadoutname) != null && !loadouts.contains(Manhunt.getLoadout(loadoutname)))
			{
				loadouts.add(Manhunt.getLoadout(loadoutname));
			}
		}
		return loadouts;
	}
	public List<Loadout> getPreyLoadouts()
	{
		List<Loadout> loadouts = new ArrayList<Loadout>();
		for (String loadoutname : this.prey_loadouts)
		{
			if (Manhunt.getLoadout(loadoutname) != null && !loadouts.contains(Manhunt.getLoadout(loadoutname)))
			{
				loadouts.add(Manhunt.getLoadout(loadoutname));
			}
		}
		return loadouts;
	}
	public List<String> getLoadoutNames()
	{
		List<String> loadoutnames = new ArrayList<String>();
		loadoutnames.addAll(this.hunter_loadouts);
		for (String loadoutname : this.prey_loadouts)
		{
			if (!loadoutnames.contains(loadoutname))
			{
				loadoutnames.add(loadoutname);
			}
		}
		return loadoutnames;
	}
	public List<String> getHunterLoadoutNames()
	{
		return new ArrayList<String>(this.hunter_loadouts);
	}
	public List<String> getPreyLoadoutNames()
	{
		return new ArrayList<String>(this.prey_loadouts);
	}
	public Loadout getRandomLoadout()
	{
		return randomLoadout(getLoadoutNames());
	}
	public Loadout getRandomHunterLoadout()
	{
		return randomLoadout(getHunterLoadoutNames());
	}
	public Loadout getRandomPreyLoadout()
	{
		return randomLoadout(getPreyLoadoutNames());
	}
	private Loadout randomLoadout(List<String> loadouts)
	{
		Loadout loadout;
		int i;

		if (loadouts.size() == 0)
			return null;
		
		do
		{
			i = (int) (Math.random() * loadouts.size());
			loadout = Manhunt.getLoadout(loadouts.get(i));
			loadouts.remove(i);
		}
		while (loadout == null && loadouts.size() > 0);
		
		return loadout;
	}
	public boolean containsLoadout(String loadoutname)
	{
		return containsHunterLoadout(loadoutname) || containsPreyLoadout(loadoutname);
	}
	public boolean containsHunterLoadout(String loadoutname)
	{
		return this.hunter_loadouts.contains(loadoutname);
	}
	public boolean containsPreyLoadout(String loadoutname)
	{
		return this.prey_loadouts.contains(loadoutname);
	}
	
	
	//---------------- SETTINGS ----------------//
	@Override
	public abstract GameLobbySettings getSettings();
	public SettingsPack getGameSettings()
	{
		if (game != null)
		{
			return game.getSettings();
		}
		else
		{
			return null;
		}
	}
	@Override
	public void saveFiles()
	{
		getSettings().GAME_CLASS.setValue(this.game != null ? game.getClass().getCanonicalName() : "");
		getSettings().MAPS.setValue(this.maps);
		getSettings().HUNTER_LOADOUTS.setValue(this.hunter_loadouts);
		getSettings().PREY_LOADOUTS.setValue(this.prey_loadouts);
		
		super.saveFiles();
	}
	@Override
	public void loadFiles()
	{
		super.loadFiles();
		
		this.maps = new ArrayList<String>(getSettings().MAPS.getValue());
		this.hunter_loadouts = new ArrayList<String>(getSettings().HUNTER_LOADOUTS.getValue());
		this.prey_loadouts = new ArrayList<String>(getSettings().PREY_LOADOUTS.getValue());
		if ((this.game == null || !this.game.getClass().getCanonicalName().equals(getSettings().GAME_CLASS.getValue())) && Manhunt.getGameClassByCanonicalName(getSettings().GAME_CLASS.getValue()) != null)
		{
			this.game = Manhunt.getGameClassByCanonicalName(getSettings().GAME_CLASS.getValue()).createInstance(this);
			super.softLoad();
		}
	}
	
	
	//---------------- MISCELLANEOUS ----------------//
	@Override
	public LobbyType getType()
	{
		return LobbyType.GAME;
	}
	
	
	
}
