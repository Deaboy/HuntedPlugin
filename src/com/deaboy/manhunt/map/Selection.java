package com.deaboy.manhunt.map;

import java.io.Closeable;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Selection implements Closeable
{
	private Player player;
	private Location primary;
	private Location secondary;
	
	public Selection(Player p)
	{
		this.player = p;
		this.primary = null;
		this.secondary = null;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public Location getPrimaryCorner()
	{
		if (primary == null)
			return null;
		else
			return primary.clone();
	}
	
	public Location getSecondaryCorner()
	{
		if (secondary == null)
			return null;
		else
			return secondary.clone();
	}
	
	public void setPrimaryCorner(Location corner)
	{
		if (corner == null)
			this.primary = null;
		if (this.secondary != null && corner.getWorld() != this.secondary.getWorld())
			this.secondary = null;
		this.primary = corner.clone();
	}
	
	public void setSecondaryCorner(Location corner)
	{
		if (corner == null)
			this.secondary = null;
		if (this.primary != null && corner.getWorld() != this.primary.getWorld())
			this.primary = null;
		this.secondary = corner.clone();
	}
	
	public int getArea()
	{
		if (!isComplete())
			return -1;
		else
			return (Math.abs(primary.getBlockX() - secondary.getBlockX()) + 1) * (Math.abs(primary.getBlockY() - secondary.getBlockY()) + 1) * (Math.abs(primary.getBlockZ() - secondary.getBlockZ()) + 1);
	}
	
	public boolean isComplete()
	{
		return (primary != null && secondary != null);
	}
	
	public boolean isValid()
	{
		return (primary != null && secondary != null && primary.getWorld() == secondary.getWorld() && primary.getWorld() == player.getWorld());
	}
	
	public void clear()
	{
		this.primary = null;
		this.secondary = null;
	}
	
	public void close()
	{
		player = null;
		primary = null;
		secondary = null;
	}
}
