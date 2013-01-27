package com.deaboy.manhunt.map.models;

import java.util.ArrayList;

import com.deaboy.manhunt.NewManhuntPlugin;
import com.deaboy.manhunt.map.Map;
import com.deaboy.manhunt.map.Spawn;
import com.deaboy.manhunt.map.World;

public class SimpleWorld
{
	//---------------- Properties ----------------//
	public String Version;
	public String Name;
	public SimpleSpawn Spawn;
	public ArrayList<SimpleMap> Maps;
	
	
	
	//---------------- Constructors ----------------//
	public SimpleWorld()
	{
		this.Version = NewManhuntPlugin.getVersion();
		this.Maps = new ArrayList<SimpleMap>();
	}
	
	
	
	//---------------- Public Methods ----------------//
	public void toWorld(World world)
	{
		Spawn spawn = Spawn.toSpawn(world);
		world.getSpawn().setLocation(spawn.getLocation());
		world.getSpawn().setRange(spawn.getRange());
		
		world.clearMaps();
		for (SimpleMap map : Maps)
		{
			Map m = map.toMap(world);
			world.addMap(m.getName(), m);
		}
		
	}
	
	
	
	//---------------- Public Static Methods ----------------//
	public static SimpleWorld fromWorld(World world)
	{
		SimpleWorld model = new SimpleWorld();
		
		model.Name = world.getWorld().getName();
		model.Spawn = SimpleSpawn.fromSpawn(world.getSpawn());
		
		for (Map map : world.getMaps())
			model.Maps.add(SimpleMap.fromMap(map));
		
		return model;
	}
	
}