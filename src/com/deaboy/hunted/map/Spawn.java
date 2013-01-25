package com.deaboy.hunted.map;

import org.bukkit.Location;
import org.bukkit.World;

public interface Spawn
{
	//---------------- Getters ----------------//
	/**
	 * Gets the spawn point for this Spawn.
	 * @return The spawn point.
	 */
	public Location getLocation();
	
	/**
	 * Gets the World for this spawn.
	 * @return The World.
	 */
	public World getWorld();
	
	/**
	 * Gets the range players can spawn around the spawn point.
	 * @return The range around the spawn point players can spawn.
	 */
	public int getRange();
	
	
	
	//---------------- Setters ----------------//
	/**
	 * Sets the spawn's location.
	 * @param loc The location to change the spawn's location to.
	 */
	public void setLocation(Location loc);
	
	/**
	 * Sets the range around the spawn point players will spawn.
	 * @param range The new range for this Spawn.
	 */
	public void setRange(int range);
	
	
	
	//---------------- Public Methods ----------------//
	/**
	 * Generates a random location within a circular area around the center
	 * of the spawn.
	 * @return
	 */
	public Location getRandomLocation();
}
