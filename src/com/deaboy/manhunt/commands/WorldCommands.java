package com.deaboy.manhunt.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.deaboy.manhunt.Manhunt;
import com.deaboy.manhunt.chat.ChatManager;

public abstract class WorldCommands
{

	public static boolean mworlds(CommandSender sender, String[] args)
	{
		final int perpage = 8;
		int page = 1;
		List<World> worlds;
		
		
		// Check permissions
		if (!sender.isOp())
		{
			sender.sendMessage(CommandUtil.NO_PERMISSION);
			return true;
		}
		
		
		// Get the page #
		if (args.length == 0)
			page = 1;
		else
			try
		{
			page = Integer.parseInt(args[0]);	
		}
		catch (NumberFormatException e)
		{
			page = 1;
		}
		
		page--;
		
		// Assemble list of settings
		worlds = Bukkit.getWorlds();
 
		if (page * perpage > worlds.size() - 1 )
			page = (worlds.size()-1) / perpage;
		
		if (page < 0)
			page = 0;
		
		if (worlds.size() == 0)
		{
			sender.sendMessage("There are no worlds to display.");
			return true;
		}
		
		
		sender.sendMessage(ChatManager.bracket1_ + ChatColor.RED + "Manhunt Worlds " + ChatManager.color + "(" + (page+1) + "/" + (int) Math.ceil((double) worlds.size()/perpage) + ")" + ChatManager.bracket2_);
		sender.sendMessage(ChatColor.GRAY + "Use /mworlds [n] to get page n of worlds");
		worlds = worlds.subList(page * perpage, Math.min( (page + 1) * perpage, worlds.size() ));
		for (World world : worlds)
		{
			sender.sendMessage((Manhunt.getWorld(world) == null ? ChatColor.GRAY : ChatColor.GOLD) + world.getName());
		}
		return true;
		
		
		
		
	}
	
	
}
