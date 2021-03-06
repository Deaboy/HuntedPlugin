package com.deaboy.manhunt.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.deaboy.manhunt.Manhunt;
import com.deaboy.manhunt.ManhuntUtil;
import com.deaboy.manhunt.chat.ChatManager;
import com.deaboy.manhunt.map.ManhuntMap;
import com.deaboy.manhunt.map.Map;
import com.deaboy.manhunt.map.Spawn;
import com.deaboy.manhunt.map.SpawnType;
import com.deaboy.manhunt.map.World;
import com.deaboy.manhunt.map.Zone;
import com.deaboy.manhunt.map.ZoneFlag;

public abstract class MapCommands
{
	//////// Maps ////////
	public static boolean mmap(CommandSender sender, Command cmd)
	{
		boolean action = false;
		
		// Check permissions
		if (!sender.isOp())
		{
			sender.sendMessage(CommandUtil.NO_PERMISSION);
			return true;
		}
		
		for (Subcommand scmd : cmd.getSubcommands())
		{
			if (scmd.containsArgument(CommandUtil.arg_list))
			{
				action |= maplist(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_lspoints))
			{
				action |= maplistpoints(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_lszones))
			{
				action |= maplistzones(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_create))
			{
				action |= mapcreate(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_select))
			{
				action |= mapselect(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_info))
			{
				action |= mapinfo(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_tp))
			{
				action |= mapteleport(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_issues))
			{
				action |= mapissues(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_delete))
			{
				action |= mapdelete(sender, scmd);
			}
		}
		
		return action;
	}
	private static boolean maplist(CommandSender sender, Subcommand cmd)
	{
		final String spacing = "   ";
		
		if (cmd.containsArgument(CommandUtil.arg_world))
		{
			sender.sendMessage(ChatManager.bracket1_ + "Loaded Manhunt Maps" + ChatManager.bracket2_);
			for (String wname : cmd.getArgument(CommandUtil.arg_world).getParameters())
			{
				World world = Manhunt.getWorld(wname);
				if (world == null)
				{
					sender.sendMessage(ChatManager.leftborder + ChatColor.RED + wname + " is not a valid world.");
				}
				else
				{
					sender.sendMessage(ChatColor.GOLD + world.getName());
					if (world.getMaps().size() == 0)
						sender.sendMessage(ChatManager.leftborder + spacing + ChatColor.GRAY + "[NONE]");
					else
						for (Map map : world.getMaps())
							sender.sendMessage(ChatManager.leftborder + spacing + ChatColor.WHITE + world.getName() + "." + map.getName());
				}
			}
		}
		else
		{
			sender.sendMessage(ChatManager.bracket1_ + "All Loaded Manhunt Maps" + ChatManager.bracket2_);
			if (Manhunt.getWorlds().size() == 0)
			{
				sender.sendMessage(ChatColor.RED + "  No worlds loaded into Manhunt.");
			}
			for (World world : Manhunt.getWorlds())
			{
				sender.sendMessage(ChatManager.leftborder + world.getName());
				if (world.getMaps().size() == 0)
					sender.sendMessage(ChatManager.leftborder + spacing + ChatColor.GRAY + "[NONE]");
				else
					for (Map map : world.getMaps())
						sender.sendMessage(ChatManager.leftborder + spacing + ChatColor.WHITE + world.getName() + "." + map.getName());
			}
		}
		
		return true;
	}
	private static boolean mapcreate(CommandSender sender, Subcommand cmd)
	{
		String mapname;
		Map map;
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage(CommandUtil.IS_SERVER);
			return false;
		}
		
		if (cmd.containsArgument(CommandUtil.arg_name))
		{
			mapname = cmd.getArgument(CommandUtil.arg_name).getParameter();
			if (mapname == null || mapname.isEmpty())
			{
				sender.sendMessage(ChatColor.RED + "Invalid parameter use: name");
				sender.sendMessage(ChatColor.GRAY + " Parameter usage: -name <name>");
				return false;
			}
		}
		else
		{
			mapname = cmd.getArgument(CommandUtil.arg_create).getParameter();
			if (mapname == null || mapname.isEmpty())
			{
				sender.sendMessage(ChatColor.RED + "No map name specified. Options:");
				sender.sendMessage(ChatColor.GRAY + " Parameter usage: -" + cmd.getArgument(CommandUtil.arg_create).getLabel() + " <name>");
				sender.sendMessage(ChatColor.GRAY + " Parameter usage: -name <name>");
				return false;
			}
		}
		
		if (Manhunt.getWorld(((Player) sender).getWorld()).getMap(mapname) != null)
		{
			sender.sendMessage(ChatColor.RED + "A map by that name already exists!");
			return false;
		}
		
		map = new ManhuntMap(mapname, ((Player) sender).getLocation(), Manhunt.getWorld(((Player) sender).getWorld()));
		
		Manhunt.getWorld(((Player) sender).getWorld()).addMap(mapname, map);
		sender.sendMessage(ChatColor.GREEN + "Map '" + mapname + "' created in world '" + Manhunt.getWorld(((Player) sender).getWorld()).getName() + "'.");
		
		CommandUtil.setSelectedMap(sender, map);
		sender.sendMessage(ChatColor.YELLOW + "  New map selected.");
		
		return true;
	}
	private static boolean mapselect(CommandSender sender, Subcommand cmd)
	{
		Map map;
		String mapname;
		
		if (cmd.getArgument(CommandUtil.arg_select).getParameter() == null)
		{
			sender.sendMessage(ChatColor.RED + "Invalid parameter usage: -" + cmd.getArgument(CommandUtil.arg_select).getLabel());
			sender.sendMessage(ChatColor.GRAY + " Parameter usage: -select <map name>");
			return false;
		}
		
		mapname = cmd.getArgument(CommandUtil.arg_select).getParameter();
		if (!mapname.contains("."))
		{
			if (sender instanceof Player)
				mapname = Manhunt.getWorld(((Player) sender).getWorld()).getName() + '.' + mapname;
		}
		
		map = Manhunt.getMap(mapname);
		if (map == null)
		{
			sender.sendMessage(ChatColor.RED + "No map named '" + mapname + "' exists.");
			sender.sendMessage(ChatManager.leftborder + "Use /mmap -list [-world <world name>] to see available maps.");
			return false;
		}
		
		CommandUtil.setSelectedMap(sender, map);
		sender.sendMessage(ChatColor.YELLOW + "Selected map '" + map.getFullName() + "'.");
		return true;
	}
	private static boolean mapinfo(CommandSender sender, Subcommand cmd)
	{
		Map map;
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null)
		{
			sender.sendMessage(ChatColor.RED + "No map selected.");
			return false;
		}
		
		sender.sendMessage(ChatManager.bracket1_ + "Map info on " + map.getName() + ChatManager.bracket2_);
		sender.sendMessage(ChatManager.leftborder + "World: " + map.getWorld().getName());
		sender.sendMessage(ChatManager.leftborder + "Zones: " + map.getZones().size() + "   Points: " + map.getPoints().size());
		if (map.hasIssues())
		{
			sender.sendMessage(ChatManager.leftborder + ChatColor.RED + "ISSUES: " + ChatManager.color + "use '/" + CommandUtil.cmd_mmap.getName() + " -" + CommandUtil.arg_issues.getName() + "' to view issues.");
		}
		else
		{
			sender.sendMessage(ChatManager.leftborder + "This map has no issues.");
		}
		return true;
	}
	private static boolean mapteleport(CommandSender sender, Subcommand cmd)
	{
		Map map;
		Player player;
		String playername;
		
		if (cmd.containsArgument(CommandUtil.arg_player) || cmd.getArgument(CommandUtil.arg_tp).getParameter() != null)
		{
			playername = cmd.containsArgument(CommandUtil.arg_player) ? cmd.getArgument(CommandUtil.arg_player).getParameter() : cmd.getArgument(CommandUtil.arg_tp).getParameter();
			player = Bukkit.getPlayer(playername);
			if (player == null)
			{
				sender.sendMessage(ChatManager.leftborder + ChatColor.RED + '\'' + playername + '\'' + ChatManager.color + " is not online.");
				return false;
			}
		}
		else if (sender instanceof Player)
		{
			player = (Player) sender;
		}
		else
		{
			sender.sendMessage(ChatManager.leftborder + "You must select a player to teleport.");
			sender.sendMessage(ChatManager.leftborder + "  Example: /" + cmd.getLabel() + " -" + cmd.getArgument(CommandUtil.arg_tp).getLabel() + " <player>");
			return false;	
		}
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null)
		{
			sender.sendMessage(ChatColor.RED + "You must first select a map!");
			sender.sendMessage(ChatColor.GRAY + " Use /mmap -select <map name>");
			return false;
		}
		
		player.teleport(map.getSpawnLocation(), TeleportCause.COMMAND);
		player.sendMessage(ChatManager.leftborder + "Teleported to " + map.getName());
		if (player != sender)
		{
			sender.sendMessage(ChatManager.leftborder + "Teleported " + player.getName() + " to " + map.getName());
		}
		return true;
	}
	private static boolean mapdelete(CommandSender sender, Subcommand cmd)
	{
		Map map;
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null)
		{
			sender.sendMessage(ChatColor.RED + "You must first select a map!");
			sender.sendMessage(ChatColor.GRAY + " Use /mmap -select <map name>");
			return false;
		}
		
		Manhunt.deleteMap(map);
		
		sender.sendMessage(ChatColor.GREEN + "Map '" + map.getName() + "' has been deleted.");
		
		return true;
	}
	private static boolean mapissues(CommandSender sender, Subcommand cmd)
	{
		Map map;
		List<String> issues;
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null)
		{
			sender.sendMessage(ChatColor.RED + "Please select a map first.");
			return false;
		}
		
		issues = map.getIssues();
		sender.sendMessage(ChatManager.bracket1_ + "Issues with " + map.getName() + ChatManager.bracket2_);
		if (issues.isEmpty())
		{
			sender.sendMessage(ChatManager.leftborder + ChatColor.GRAY + "  There are no issues with this map.");
		}
		else
		{
			for (String issue : issues)
				sender.sendMessage(ChatManager.leftborder + issue);
		}
		return true;
	}
	private static boolean maplistpoints(CommandSender sender, Subcommand cmd)
	{
		String command = CommandUtil.cmd_mpoint.getName() + " -" + CommandUtil.arg_list.getName();
		for (String parameter : cmd.getArgument(CommandUtil.arg_lspoints).getParameters())
			command += " " + parameter;
		
		if (cmd.containsArgument(CommandUtil.arg_page))
		{
			command += " -" + CommandUtil.arg_page.getName();
			for (String parameter : cmd.getArgument(CommandUtil.arg_page).getParameters())
				command += " " + parameter;
		}
		
		Bukkit.dispatchCommand(sender, command);
		return true;
	}
	private static boolean maplistzones(CommandSender sender, Subcommand cmd)
	{
		String command = CommandUtil.cmd_mzone.getName() + " -" + CommandUtil.arg_list.getName();
		for (String parameter : cmd.getArgument(CommandUtil.arg_lszones).getParameters())
			command += " " + parameter;
		
		if (cmd.containsArgument(CommandUtil.arg_page))
		{
			command += " -" + CommandUtil.arg_page.getName();
			for (String parameter : cmd.getArgument(CommandUtil.arg_page).getParameters())
				command += " " + parameter;
		}
		
		Bukkit.dispatchCommand(sender, command);
		return true;
	}
	
	
	//////// Zones ////////
	public static boolean mzone(CommandSender sender, Command cmd)
	{
		boolean action = false;
		
		// Check permissions
		if (!sender.isOp())
		{
			sender.sendMessage(CommandUtil.NO_PERMISSION);
			return true;
		}
		
		for (Subcommand scmd : cmd.getSubcommands())
		{
			if (scmd.containsArgument(CommandUtil.arg_list))
			{
				action |= listzones(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_create))
			{
				action |= createzone(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_select))
			{
				action |= selectzone(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_redefine))
			{
				action |= redefinezone(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_zoneflags))
			{
				action |= flagzone(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_delete))
			{
				action |= deletezone(sender, scmd);
			}
		}
		
		return action;
	}
	private static boolean listzones(CommandSender sender, Subcommand cmd)
	{
		int perpage = 8;
		boolean all = false;
		Map m;
		List<Zone> zones;
		int page;
		
		if (cmd.containsArgument(CommandUtil.arg_page) && cmd.getArgument(CommandUtil.arg_page).getParameter() != null)
		{
			if (cmd.getArgument(CommandUtil.arg_page).getParameter().equalsIgnoreCase("all"))
			{
				all = true;
				page = 1;
			}
			else
			{
				try
				{
					page = Integer.parseInt(cmd.getArgument(CommandUtil.arg_page).getParameter());
				}
				catch (NumberFormatException e)
				{
					page = 1;
				}
			}
		}
		else if (cmd.getArgument(CommandUtil.arg_list).getParameter() != null)
		{
			 if (cmd.getArgument(CommandUtil.arg_list).getParameter().equalsIgnoreCase("all"))
			 {
				 all = true;
				 page = 1;
			 }
			 else
			 {
				 try
				 {
					 page = Integer.parseInt(cmd.getArgument(CommandUtil.arg_list).getParameter());
				 }
				 catch (Exception e)
				 {
					 page = 1;
				 }
			 }
		}
		else
		{
			page = 1;
		}
		
		
		m = CommandUtil.getSelectedMap(sender);
		
		if (m == null)
		{
			sender.sendMessage(ChatColor.RED + "You must first select a map!");
			sender.sendMessage(ChatColor.GRAY + "Use /map select <map full name>");
			return false;
		}
		
		page--;
		
		// Assemble list of settings
		zones = m.getZones();
 
		if (!all)
		{
			if (page * perpage > zones.size() - 1 )
				page = (zones.size()-1) / perpage;
			
			if (page < 0)
				page = 0;
			
			if (zones.size() == 0)
			{
				sender.sendMessage("There are no zones to display.");
				return false;
			}
		}
		
		sender.sendMessage(ChatManager.bracket1_ + ChatColor.RED + "Map Zones " + ChatManager.color + "(" + (all ? "All" : (page+1) + "/" + (int) Math.ceil((double) zones.size()/perpage)) + ")" + ChatManager.bracket2_);
		if (!all && zones.size() > perpage)
		{
			sender.sendMessage(ChatColor.GRAY + "Use /map zones [n] to get page n of zones");
			zones = zones.subList(page * perpage, Math.min( (page + 1) * perpage, zones.size() ));
		}
		
		for (Zone zone : zones)
		{
			String flaginfo = new String();
			for (ZoneFlag flag : ZoneFlag.values())
			{
				if (zone.checkFlag(flag))
					flaginfo += flag.getName() + ", ";
			}
			sender.sendMessage(ChatManager.leftborder + ChatColor.WHITE + zone.getName() + "    " + flaginfo);
		}
		return true;
	}
	private static boolean selectzone(CommandSender sender, Subcommand cmd)
	{
		String zonename;
		Zone zone;
		Map map;
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null)
		{
			sender.sendMessage(ChatColor.RED + "You must select a map before you can select a zone within!");
			sender.sendMessage(ChatColor.GRAY+ " Use /mmap -s <mapname> to select a map.");
			return false;
		}
		
		zonename = cmd.getArgument(CommandUtil.arg_select).getParameter();
		zone = map.getZone(zonename);
		if (zonename == null || zonename.isEmpty())
		{
			sender.sendMessage(ChatColor.RED + "You must include the name of the zone you want to select.");
			sender.sendMessage(ChatColor.GRAY + " /mzone -s <zonename>");
			return false;
		}
		else if (zone == null)
		{
			sender.sendMessage(ChatColor.RED + "No zone with that name exists in the selected map.");
			sender.sendMessage(ChatColor.GRAY + " To see available zones, use /mzone -list [all] [-page <page>]");
			sender.sendMessage(ChatColor.GRAY + " To select a different map, use /mmap -s <mapname>");
			return false;
		}
		
		CommandUtil.setSelectedZone(sender, zone);
		sender.sendMessage(ChatManager.leftborder + "Selected zone '" + ChatColor.YELLOW + zone.getName() + ChatManager.color + "' in map '" + ChatColor.YELLOW + map.getName() + ChatManager.color + "'.");
		if (sender instanceof Player)
		{
			Manhunt.setPlayerSelectionPrimaryCorner((Player) sender, zone.getPrimaryCorner());
			Manhunt.setPlayerSelectionSecondaryCorner((Player) sender, zone.getSecondaryCorner());
			sender.sendMessage(ChatManager.leftborder + ChatColor.YELLOW + "Selected zone's corner points as well.");
		}
		return true;
	}
	private static boolean createzone(CommandSender sender, Subcommand cmd)
	{
		// Declarations.
		Zone zone;
		Map map;
		String zonename;
		Location primarycorner;
		Location secondarycorner;
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage(CommandUtil.IS_SERVER);
			return false;
		}
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null)
		{
			sender.sendMessage(CommandUtil.MAP_NOT_SELECTED);
			return false;
		}
		
		if (cmd.containsArgument(CommandUtil.arg_name))
		{
			zonename = cmd.getArgument(CommandUtil.arg_name).getParameter();
			if (zonename == null || zonename.isEmpty())
			{
				sender.sendMessage(ChatColor.RED + "Name argument missing.");
				sender.sendMessage(ChatColor.GRAY + " Parameter usage: -name <name>");
				return false;
			}
		}
		else
		{
			zonename = cmd.getArgument(CommandUtil.arg_create).getParameter();
			if (zonename == null || zonename.isEmpty())
			{
				sender.sendMessage(ChatColor.RED + "No zone name specified. Options:");
				sender.sendMessage(ChatColor.GRAY + " Parameter usage: -" + cmd.getArgument(CommandUtil.arg_create).getLabel() + " <name>");
				sender.sendMessage(ChatColor.GRAY + " Parameter usage: -name <name>");
				return false;
			}
		}
		
		primarycorner = Manhunt.getPlayerSelectionPrimaryCorner((Player) sender);
		secondarycorner = Manhunt.getPlayerSelectionSecondaryCorner((Player) sender);
		if (primarycorner == null || secondarycorner == null)
		{
			if (primarycorner == null)
				sender.sendMessage(CommandUtil.CORNER_PRIMARY_NOT_SELECTED);
			if (secondarycorner == null)
				sender.sendMessage(CommandUtil.CORNER_SECONDARY_NOT_SELECTED);
			return false;
		}
		
		if (map.getZone(zonename) != null)
		{
			int i;
			for (i = 0; map.getZone(zonename + i) != null; i++);
			zonename += i;
		}
		
		zone = map.createZone(zonename, primarycorner, secondarycorner, cmd.containsArgument(CommandUtil.arg_ignorey)
					&& (cmd.getArgument(CommandUtil.arg_ignorey).getParameter() == null
					|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().isEmpty()
					|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("true")
					|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("t")
					|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("yes")
					|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("y")));
		if (zone == null)
		{
			sender.sendMessage(ChatColor.RED + "There was an error creating the zone.");
			return false;
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + "Zone '" + zone.getName() + "' successfully created in map '" + map.getName() + "'");
			sender.sendMessage(ChatColor.GRAY + " ["
					+ zone.getPrimaryCorner().getBlockX() + ", "
					+ (zone.getIgnoreY() ? 0 : zone.getPrimaryCorner().getBlockY()) + ", "
					+ zone.getPrimaryCorner().getBlockZ() + "]  ["
					+ zone.getSecondaryCorner().getBlockX() + ", "
					+ (zone.getIgnoreY() ? zone.getWorld().getMaxHeight() : zone.getSecondaryCorner().getBlockY()) + ", "
					+ zone.getSecondaryCorner().getBlockZ() + "]  ("
					+ zone.getVolume() + " blocks)");
			CommandUtil.setSelectedZone(sender, zone);
			sender.sendMessage(ChatColor.YELLOW + " Selected new zone.");
			
			return true;
		}
	}
	private static boolean deletezone(CommandSender sender, Subcommand cmd)
	{
		String zonename;
		
		if (CommandUtil.getSelectedMap(sender) == null)
		{
			sender.sendMessage(CommandUtil.MAP_NOT_SELECTED);
			return false;
		}
		
		if (CommandUtil.getSelectedZone(sender) == null)
		{
			sender.sendMessage(ChatColor.RED + "You have not selected a zone to delete.");
			return false;
		}
		else
		{
			zonename = CommandUtil.getSelectedZone(sender).getName();
		}
		
		CommandUtil.getSelectedMap(sender).removeZone(zonename);
		sender.sendMessage(ChatColor.GREEN + "Zone '" + zonename + "' removed from map '" + CommandUtil.getSelectedMap(sender).getName() +"'");
		return true;
	}
	private static boolean flagzone(CommandSender sender, Subcommand cmd)
	{
		Zone zone;
		List<ZoneFlag> flags;
		
		zone = CommandUtil.getSelectedZone(sender);
		if (zone == null)
		{
			sender.sendMessage(ChatColor.RED + "Please select a zone to modify.");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + cmd.getLabel() + " -" + CommandUtil.arg_select.getName() + " <zonename>");
			return false;
		}
		
		flags = new ArrayList<ZoneFlag>();
		for (String f : cmd.getArgument(CommandUtil.arg_zoneflags).getParameters())
		{
			if (ZoneFlag.fromName(f) != null)
				flags.add(ZoneFlag.fromName(f));
		}
		
		if (flags.isEmpty())
		{
			sender.sendMessage(ChatColor.RED + "Please list the flags you would like to set.");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + cmd.getLabel() + " -" + cmd.getArgument(CommandUtil.arg_zoneflags).getLabel() + " <flag1> [flag2] [flag3] ...");
			return false;
		}
		else
		{
			zone.clearFlags();
			sender.sendMessage(ChatColor.GREEN + "Flags set of " + zone.getName() + ":");
			for (ZoneFlag flag : flags)
			{
				zone.setFlag(flag, true);
				sender.sendMessage(ChatColor.GREEN + "  - " + flag.getName());
			}
		}
		
		return true;
	}
	private static boolean redefinezone(CommandSender sender, Subcommand cmd)
	{
		Zone zone;
		Location primary, secondary;
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + CommandUtil.IS_SERVER);
			return false;
		}
		
		zone = CommandUtil.getSelectedZone(sender);
		if (zone == null)
		{
			sender.sendMessage(ChatColor.RED + "Please select a zone to redefine.");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + cmd.getLabel() + " -" + CommandUtil.arg_select.getName() + " <zonename>");
			return false;
		}
		
		primary = Manhunt.getPlayerSelectionPrimaryCorner((Player) sender);
		secondary = Manhunt.getPlayerSelectionSecondaryCorner((Player) sender);
		
		if (cmd.containsArgument(CommandUtil.arg_ignorey))
		{
			if (cmd.getArgument(CommandUtil.arg_ignorey).getParameter() == null
				|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().isEmpty()
				|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("true")
				|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("t")
				|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("yes")
				|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("y"))
				zone.setIgnoreY(true);
			else if (cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("false")
				|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("f")
				|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("no")
				|| cmd.getArgument(CommandUtil.arg_ignorey).getParameter().equalsIgnoreCase("n"))
				zone.setIgnoreY(false);
		}
		
		if (primary == null || secondary == null || !Manhunt.getPlayerSelectionValid((Player) sender))
		{
			sender.sendMessage(ChatColor.RED + "Please select a region first.");
			return false;
		}
		
		zone.setCorner1(primary);
		zone.setCorner2(secondary);
		sender.sendMessage(ChatColor.GREEN + "Zone redefined with the following coordinates:");
		sender.sendMessage(ChatColor.GRAY + " ["
				+ zone.getPrimaryCorner().getBlockX() + ", "
				+ (zone.getIgnoreY() ? 0 : zone.getPrimaryCorner().getBlockY()) + ", "
				+ zone.getPrimaryCorner().getBlockZ() + "]  ["
				+ zone.getSecondaryCorner().getBlockX() + ", "
				+ (zone.getIgnoreY() ? zone.getWorld().getMaxHeight() : zone.getSecondaryCorner().getBlockY()) + ", "
				+ zone.getSecondaryCorner().getBlockZ() + "]  ("
				+ zone.getVolume() + " blocks)");
		return true;
	}
	
	//////// Spawns ////////
	public static boolean mpoint(CommandSender sender, Command cmd)
	{
		boolean action = false;

		// Check permissions
		if (!sender.isOp())
		{
			sender.sendMessage(CommandUtil.NO_PERMISSION);
			return true;
		}
		
		for (Subcommand scmd : cmd.getSubcommands())
		{
			if (scmd.containsArgument(CommandUtil.arg_list))
			{
				action |= listpoints(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_select))
			{
				action |= selectpoint(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_create))
			{
				action |= createpoint(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_range))
			{
				action |= rangepoint(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_redefine))
			{
				action |= redefinepoint(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_tp))
			{
				action |= tppoint(sender, scmd);
			}
			else if (scmd.containsArgument(CommandUtil.arg_delete))
			{
				action |= deletepoint(sender, scmd);
			}
		}
		
		return action;
	}
	private static boolean listpoints(CommandSender sender, Subcommand cmd)
	{
		int perpage = 8;
		boolean all = false;
		Map map;
		List<Spawn> points;
		SpawnType type;
		int page;
		
		if (cmd.containsArgument(CommandUtil.arg_page) && cmd.getArgument(CommandUtil.arg_page).getParameter() != null)
		{
			if (cmd.getArgument(CommandUtil.arg_page).getParameter().equalsIgnoreCase("all"))
			{
				all = true;
				page = 1;
			}
			else
			{
				try
				{
					page = Integer.parseInt(cmd.getArgument(CommandUtil.arg_page).getParameter());
				}
				catch (NumberFormatException e)
				{
					page = 1;
				}
			}
		}
		else if (cmd.getArgument(CommandUtil.arg_list).getParameter() != null)
		{
			 if (cmd.getArgument(CommandUtil.arg_list).getParameter().equalsIgnoreCase("all"))
			 {
				 all = true;
				 page = 1;
			 }
			 else
			 {
				 try
				 {
					 page = Integer.parseInt(cmd.getArgument(CommandUtil.arg_list).getParameter());
				 }
				 catch (Exception e)
				 {
					 page = 1;
				 }
			 }
		}
		else
		{
			page = 1;
		}
		
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null)
		{
			sender.sendMessage(ChatColor.RED + "You must first select a map!");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + CommandUtil.cmd_mmap.getName() + " -" + CommandUtil.arg_select.getName() + " <full map name>");
			return false;
		}
		if (cmd.containsArgument(CommandUtil.arg_pointtype))
		{
			type = SpawnType.fromName(cmd.getArgument(CommandUtil.arg_pointtype).getParameter());
		}
		else
		{
			type = null;
		}
		
		page--;
		
		// Assemble list of settings
		if (type == null)
			points = map.getPoints();
		else
			points = map.getPoints(type);
 
		if (!all)
		{
			if (page * perpage > points.size() - 1 )
				page = (points.size()-1) / perpage;
			
			if (page < 0)
				page = 0;
			
			if (points.size() == 0)
			{
				sender.sendMessage("There are no points to display.");
				return false;
			}
		}
		
		sender.sendMessage(ChatManager.bracket1_ + ChatColor.RED + "Map Points " + ChatManager.color + "(" + (all ? "All" : (page+1) + "/" + (int) Math.ceil((double) points.size()/perpage)) + ")" + ChatManager.bracket2_);
		if (!all && points.size() > perpage)
		{
			sender.sendMessage(ChatColor.GRAY + "Use /" + cmd.getLabel() + " -" + cmd.getArgument(CommandUtil.arg_list).getLabel() + " [n] to get page n of points");
			points = points.subList(page * perpage, Math.min( (page + 1) * perpage, points.size() ));
		}
		
		for (Spawn point : points)
		{
			sender.sendMessage(ChatManager.leftborder + ChatColor.WHITE + point.getName() + "    [" + point.getLocation().getBlockX() + ", " + point.getLocation().getBlockY() + ", " + point.getLocation().getBlockZ() + "]  (range: " + point.getRange() + ", " + point.getType().getName() + ")");
		}
		return true;
	}
	private static boolean selectpoint(CommandSender sender, Subcommand cmd)
	{
		String pointname;
		Spawn point;
		Map map;
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null)
		{
			sender.sendMessage(ChatColor.RED + "You must select a map before you can select a point within!");
			sender.sendMessage(ChatColor.GRAY+ "  Example: /" + CommandUtil.cmd_mmap.getName() + " -" + CommandUtil.arg_select.getName() + " <map name>");
			return false;
		}
		
		pointname = cmd.getArgument(CommandUtil.arg_select).getParameter();
		point = map.getPoint(pointname);
		if (pointname == null || pointname.isEmpty())
		{
			sender.sendMessage(ChatColor.RED + "You must include the name of the point you want to select.");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + cmd.getLabel() + " -" + cmd.getArgument(CommandUtil.arg_select).getLabel() + " <point name>");
			return false;
		}
		else if (point == null)
		{
			sender.sendMessage(ChatColor.RED + "No point with that name exists in the selected map.");
			sender.sendMessage(ChatColor.GRAY + "  To see available points, use /" + cmd.getLabel() + " -" + CommandUtil.arg_list.getName() + " [page | all]");
			sender.sendMessage(ChatColor.GRAY + "  To select a different map, use /" + CommandUtil.cmd_mmap.getName() + " -" + CommandUtil.arg_select.getName() + " <map name>");
			return false;
		}
		
		CommandUtil.setSelectedPoint(sender, point);
		sender.sendMessage(ChatColor.YELLOW + "Selected point '" + point.getName() + "' in map '" + map.getName() + "'.");
		return true;
	}
	private static boolean createpoint(CommandSender sender, Subcommand cmd)
	{
		// Declarations.
		Spawn point;
		Map map;
		String pointname;
		Location loc;
		SpawnType type;
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage(CommandUtil.IS_SERVER);
			return false;
		}
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null || map.getWorld().getWorld() != ((Player) sender).getWorld())
		{
			sender.sendMessage(CommandUtil.MAP_NOT_SELECTED);
			return false;
		}
		
		// Location
		loc = ((Player) sender).getLocation();
		
		if (cmd.containsArgument(CommandUtil.arg_name))
		{
			pointname = cmd.getArgument(CommandUtil.arg_name).getParameter();
			if (pointname == null || pointname.isEmpty())
			{
				sender.sendMessage(ChatColor.RED + "Invalid use of name parameter.");
				sender.sendMessage(ChatColor.GRAY + "  Parameter usage: -" + CommandUtil.arg_name.getName() + " <name>");
				return false;
			}
		}
		else
		{
			pointname = cmd.getArgument(CommandUtil.arg_create).getParameter();
			if (pointname == null || pointname.isEmpty())
			{
				sender.sendMessage(ChatColor.RED + "No point name specified. Options:");
				sender.sendMessage(ChatColor.GRAY + "  Parameter usage: -" + cmd.getArgument(CommandUtil.arg_create).getLabel() + " <name>");
				sender.sendMessage(ChatColor.GRAY + "  Parameter usage: -" + CommandUtil.arg_name.getName() + " <name>");
				return false;
			}
		}
		
		if (cmd.containsArgument(CommandUtil.arg_pointtype))
		{
			if (cmd.getArgument(CommandUtil.arg_pointtype).getParameter() == null)
			{
				sender.sendMessage(ChatColor.RED + "Please include a type for this point.");
				sender.sendMessage(ChatColor.GRAY + "  Usage: -" + cmd.getArgument(CommandUtil.arg_pointtype).getLabel() + " <setup | hunter | prey>");
				return false;
			}
			type = SpawnType.fromName(cmd.getArgument(CommandUtil.arg_pointtype).getParameter());
			if (type == null)
			{
				sender.sendMessage(ChatColor.RED + "Invalid type point type.");
				sender.sendMessage(ChatColor.GRAY + "  Usage: -" + cmd.getArgument(CommandUtil.arg_pointtype).getLabel() + " <setup | hunter | prey>");
				return false;
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "Please include a type for this point.");
			sender.sendMessage(ChatColor.GRAY + "  Usage: -" + CommandUtil.arg_pointtype.getName() + " <setup | hunter | prey>");
			return false;
		}
		
		if (map.getPoint(pointname) != null)
		{
			int i;
			for (i = 0; map.getPoint(pointname + i) != null; i++);
			pointname += i;
		}
		
		point = map.createPoint(pointname, type, loc);
		if (point == null)
		{
			sender.sendMessage(ChatColor.RED + "There was an error creating the point.");
			return false;
		}
		else
		{
			sender.sendMessage(ChatColor.GREEN + "Point '" + point.getName() + "' successfully created in map '" + map.getName() + "'");
			sender.sendMessage(ChatColor.GRAY + " [" +
					point.getLocation().getBlockX() + ", " +
					point.getLocation().getBlockY() + ", " +
					point.getLocation().getBlockZ() + ", y:" +
					point.getLocation().getYaw() + ", p:" +
					point.getLocation().getPitch() + "] (range: " +
					point.getRange() + ", " +
					point.getType().getName() + ")");
			CommandUtil.setSelectedPoint(sender, point);
			sender.sendMessage(ChatColor.YELLOW + "Selected new point.");
			
			return true;
		}
	}
	private static boolean rangepoint(CommandSender sender, Subcommand cmd)
	{
		Spawn point;
		int range;
		
		point = CommandUtil.getSelectedPoint(sender);
		if (point == null)
		{
			sender.sendMessage(ChatColor.RED + "Please select the point you wish to modify.");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + cmd.getLabel() + " -" + CommandUtil.arg_select.getName() + " <point name>");
			return false;
		}
		
		try
		{
			range = Integer.parseInt(cmd.getArgument(CommandUtil.arg_range).getParameter());
		}
		catch (NumberFormatException e)
		{
			sender.sendMessage(ChatColor.RED + "Invalid range: " + cmd.getArgument(CommandUtil.arg_range).getParameter());
			return false;
		}
		if (range < 0) range = 0;
		
		point.setRange(range);
		sender.sendMessage(ChatColor.GREEN + "Range of " + point.getName() + " set to " + range + ".");
		return true;
	}
	private static boolean redefinepoint(CommandSender sender, Subcommand cmd)
	{
		Spawn point;
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage(CommandUtil.IS_SERVER);
			return false;
		}
		
		point = CommandUtil.getSelectedPoint(sender);
		if (point == null || point.getWorld() != ((Player) sender).getWorld())
		{
			sender.sendMessage(ChatColor.RED + "Please select the point you wish to redefine.");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + cmd.getLabel() + " -" + CommandUtil.arg_select.getName() + " <point name>");
			return false;
		}
		
		point.setLocation(((Player) sender).getLocation());
		sender.sendMessage(ChatColor.GREEN + "Moved point '" + point.getName() + "' to " + ChatColor.WHITE + "[" + point.getLocation().getBlockX() + ", " + point.getLocation().getBlockY() + ", " + point.getLocation().getBlockZ() + ", y:" + point.getLocation().getYaw() + ", p:" + point.getLocation().getPitch() +"]");
		return true;
	}
	private static boolean tppoint(CommandSender sender, Subcommand cmd)
	{
		Spawn point;
		Player player;
		String playername;
		
		if (cmd.containsArgument(CommandUtil.arg_player) || cmd.getArgument(CommandUtil.arg_tp).getParameter() != null)
		{
			playername = cmd.containsArgument(CommandUtil.arg_player) ? cmd.getArgument(CommandUtil.arg_player).getParameter() : cmd.getArgument(CommandUtil.arg_tp).getParameter();
			player = Bukkit.getPlayer(playername);
			if (player == null)
			{
				sender.sendMessage(ChatManager.leftborder + ChatColor.RED + '\'' + playername + '\'' + ChatManager.color + " is not online.");
				return false;
			}
		}
		else if (sender instanceof Player)
		{
			player = (Player) sender;
		}
		else
		{
			sender.sendMessage(ChatManager.leftborder + "You must select a player to teleport.");
			sender.sendMessage(ChatManager.leftborder + "  Example: /" + cmd.getLabel() + " -" + cmd.getArgument(CommandUtil.arg_tp).getLabel() + " <player>");
			return false;	
		}
		
		point = CommandUtil.getSelectedPoint(sender);
		if (point == null)
		{
			sender.sendMessage(ChatColor.RED + "Please select the point you wish to modify.");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + cmd.getLabel() + " -" + CommandUtil.arg_select.getName() + " <point name>");
			return false;
		}
		
		player.teleport(ManhuntUtil.safeTeleport(point.getLocation()), TeleportCause.COMMAND);
		player.sendMessage(ChatManager.leftborder + "Teleported to " + point.getName() + ".");
		if (player != sender)
		{
			sender.sendMessage(ChatManager.leftborder + "Teleported " + player.getName() + " to " + point.getName() + ".");
		}
		return true;
	}
	private static boolean deletepoint(CommandSender sender, Subcommand cmd)
	{
		Spawn point;
		Map map;
		
		map = CommandUtil.getSelectedMap(sender);
		if (map == null)
		{
			sender.sendMessage(ChatColor.RED + "You must first select a map you wish to delete a point from.");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + cmd.getLabel() + " -" + CommandUtil.arg_select.getName() + " <point name");
			return false;
		}
		
		point = CommandUtil.getSelectedPoint(sender);
		if (point == null)
		{
			sender.sendMessage(ChatColor.RED + "Please select the point you wish to delete.");
			sender.sendMessage(ChatColor.GRAY + "  Example: /" + cmd.getLabel() + " -" + CommandUtil.arg_select.getName() + " <point name>");
			return false;
		}
		
		map.removePoint(point.getName());
		sender.sendMessage(ChatColor.GREEN + "Deleted point '" + point.getName() + "' from map '" + map.getName() + "'.");
		return true;
	}
	
	
	
	
}
