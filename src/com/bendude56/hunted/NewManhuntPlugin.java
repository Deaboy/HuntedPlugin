package com.bendude56.hunted;

import org.bukkit.plugin.java.JavaPlugin;

public class NewManhuntPlugin extends JavaPlugin
{
	//---------------- Declarations ----------------//
	
	private static NewManhuntPlugin instance;
	
	
	@Override
	public void onEnable()
	{
		instance = this;
	}
	
	@Override
	public void onDisable()
	{
		instance = null;
	}
	
	public static NewManhuntPlugin getInstance()
	{
		return instance;
	}
}
