package com.bendude56.hunted;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.craftbukkit.entity.CraftPlayer;


import com.bendude56.hunted.config.SettingsManager;
import com.bendude56.hunted.loadout.LoadoutManager;
import com.bendude56.hunted.utilities.ManhuntUtil;

public class Game {
	private SettingsManager settings;
	private LoadoutManager loadouts;

	private HashMap<String, Long> timeout;

	private List<String> hunter;
	private List<String> hunted;
	private List<String> spectator;
	private List<String> creative;
	private List<String> locator;

	private int countdown;
	private boolean gameRunning;
	private long hunterReleaseTick;
	private long endTick;

	private int tickSched;

	public Game() {
		hunter = new ArrayList<String>();
		hunted = new ArrayList<String>();
		spectator = new ArrayList<String>();
		creative = new ArrayList<String>();
		locator = new ArrayList<String>();
		timeout = new HashMap<String, Long>();
		gameRunning = false;
		countdown = 0;
		hunterReleaseTick = 0;
		endTick = 0;
		settings = HuntedPlugin.getInstance().getSettings();
		loadouts = HuntedPlugin.getInstance().getLoadouts();
	}

	public void start() {
		if (!gameRunning) {

			if (settings.SETUP_TIME.value > 0) {
				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				broadcastAll(ChatColor.GOLD
						+ "The Manhunt game is starting! The hunt begins at sundown!");
				broadcastHunted(ChatColor.GOLD
						+ "You have until sunset to prepare! HURRY!");
				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				HuntedPlugin.getInstance().log(Level.INFO,
						"-------------------------------------");
				HuntedPlugin
						.getInstance()
						.log(Level.INFO,
								"The Manhunt game is starting! The hunt begins at sundown!");
				HuntedPlugin.getInstance().log(Level.INFO,
						"-------------------------------------");
			}

			countdown = 0;
			HuntedPlugin.getInstance().getWorld()
					.setTime(13000 - settings.SETUP_TIME.value * 1200);
			hunterReleaseTick = HuntedPlugin.getInstance().getWorld()
					.getFullTime()
					+ settings.SETUP_TIME.value * 1200;
			endTick = hunterReleaseTick + settings.SETUP_TIME.value * 24000;
			gameRunning = true;

			for (Entity entity : HuntedPlugin.getInstance().getWorld()
					.getEntities()) {
				if (entity instanceof Creeper || entity instanceof Zombie
						|| entity instanceof Skeleton
						|| entity instanceof Enderman
						|| entity instanceof Spider
						|| entity instanceof CaveSpider
						|| entity instanceof Silverfish) {
					entity.remove();
				} else if (entity instanceof Item || entity instanceof Arrow) {
					entity.remove();
				}
			}

			for (String n : hunted) {
				Player p = Bukkit.getServer().getPlayerExact(n);
				if (p != null) {
					if (p.getGameMode() == GameMode.CREATIVE) {
						creative.add(p.getName());
					}
					p.setGameMode(GameMode.SURVIVAL);
					p.setFireTicks(0);
					p.setHealth(20);
					p.setFoodLevel(20);
					p.setSaturation(20);
					if (settings.LOADOUTS.value)
					{
						p.getInventory().setContents(loadouts.getPreyLoadout().getContents());
						p.getInventory().setArmorContents(loadouts.getPreyLoadout().getArmor());
					}
					p.teleport(ManhuntUtil.safeTeleport(ManhuntUtil.randomLocation(
							settings.SPAWN_SETUP.value, 2)));
				}
			}
			for (String n : hunter) {
				Player p = Bukkit.getServer().getPlayerExact(n);
				if (p != null) {
					if (p.getGameMode() == GameMode.CREATIVE) {
						creative.add(p.getName());
					}
					p.setGameMode(GameMode.SURVIVAL);
					p.setFireTicks(0);
					if (settings.SETUP_TIME.value > 0) {
						p.teleport(ManhuntUtil.safeTeleport(ManhuntUtil.randomLocation(
								settings.SPAWN_SETUP.value, 2)));
					} else {
						p.teleport(ManhuntUtil.safeTeleport(ManhuntUtil.randomLocation(
								settings.SPAWN_HUNTER.value, 2)));
					}
					p.setHealth(20);
					p.setFoodLevel(20);
					p.setSaturation(20);
					if (settings.LOADOUTS.value) {
						ManhuntUtil.clearInventory(p.getInventory());
					}
				}
			}

			for (String n : spectator) {
				Player p = Bukkit.getServer().getPlayerExact(n);
				if (p != null) {
					if (p.getGameMode() == GameMode.CREATIVE) {
						creative.add(p.getName());
					}
					if (settings.FLYING_SPECTATORS.value) {
						p.setGameMode(GameMode.CREATIVE);
					} else {
						p.setGameMode(GameMode.SURVIVAL);
					}
					ManhuntUtil.clearInventory(p.getInventory());
					for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
						if (isHunter(p2) || isHunted(p2)) {
							((CraftPlayer) p2).getHandle().netServerHandler
									.sendPacket(new Packet29DestroyEntity(p
											.getEntityId()));
						}
					}
				}
			}
			tickSched = Bukkit.getScheduler().scheduleSyncRepeatingTask(
					HuntedPlugin.getInstance(), new Runnable() {
						public void run() {
							onTick();
						}
					}, 0, 1);
		}
	}

	public void stop() {
		Bukkit.getScheduler().cancelTask(tickSched);
		gameRunning = false;
		hunterReleaseTick = 0;
		endTick = 0;
		countdown = 0;
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			ManhuntUtil.clearInventory(p.getInventory());
			if (isSpectating(p)) {
				for (String n : hunter) {
					Player p2 = Bukkit.getServer().getPlayerExact(n);
					if (p2 != null) {
						((CraftPlayer) p2).getHandle().netServerHandler
								.sendPacket(new Packet20NamedEntitySpawn(
										((CraftPlayer) p).getHandle()));
					}
				}
				for (String n : hunted) {
					Player p2 = Bukkit.getServer().getPlayerExact(n);
					if (p2 != null) {
						((CraftPlayer) p2).getHandle().netServerHandler
								.sendPacket(new Packet20NamedEntitySpawn(
										((CraftPlayer) p).getHandle()));
					}
				}
			}
			if (creative.contains(p.getName())) {
				p.setGameMode(GameMode.CREATIVE);
				creative.remove(p.getName());
			} else {
				p.setGameMode(GameMode.SURVIVAL);
			}
			creative.clear();
			p.setCompassTarget(HuntedPlugin.getInstance().getWorld()
					.getSpawnLocation());
		}

		if (settings.AUTO_JOIN.value) {
			for (String s : spectator) {
				if (Bukkit.getPlayerExact(s) != null) {
					if (!hunter.contains(s))
						hunter.add(s);
					Bukkit.getPlayerExact(s).sendMessage(
							ChatColor.GOLD + "---[   " + ChatColor.GRAY
									+ "You have been moved to team "
									+ ChatColor.DARK_RED + "Hunters"
									+ ChatColor.GOLD + "   ]---");
					HuntedPlugin.getInstance().log(Level.INFO,
							s + " has been moved to team Hunters");
				}
			}
			spectator.clear();
			// broadcastAll(ChatColor.GOLD + "---[   All " + ChatColor.YELLOW +
			// "Spectators"
			// + ChatColor.GOLD + " have been moved to team " +
			// ChatColor.DARK_RED + "Hunters"
			// + ChatColor.GOLD + "   ]---");
		}
		locator.clear();
	}

	public void onDie(Player p) {
		onDie(p.getName());
	}

	public void onDie(String s) {
		/*
		 * Player p = Bukkit.getServer().getPlayerExact(s); if (p != null) { for
		 * (Player p2 : Bukkit.getServer().getOnlinePlayers()) { if
		 * (isHunter(p2) || isHunted(p2)) { //Makes spectator invisible!
		 * ((CraftPlayer) p2).getHandle().netServerHandler.sendPacket(new
		 * Packet29DestroyEntity(p.getEntityId())); } } }
		 */
		if (hunter.contains(s)) {
			hunter.remove(s);
			spectator.add(s);
			if (HuntersAmount(false) == 0) {
				stop();
				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				broadcastAll(ChatColor.GOLD + "All of the "
						+ ChatColor.DARK_RED + "Hunters" + ChatColor.GOLD
						+ " are dead! The " + ChatColor.BLUE + "Prey"
						+ ChatColor.GOLD + " win the game!");
				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				HuntedPlugin.getInstance().log(Level.INFO,
						"-------------------------------------");
				HuntedPlugin
						.getInstance()
						.log(Level.INFO,
								"All of the Hunters are now dead! The Prey win the game!");
				HuntedPlugin.getInstance().log(Level.INFO,
						"-------------------------------------");
			} else {
				broadcastAll(ChatColor.BLUE + "Remaining Prey: "
						+ HuntedAmount(false) + ChatColor.DARK_RED
						+ "   Remaining Hunters: " + HuntersAmount(false));
				if (Bukkit.getPlayerExact(s) != null) {
					Bukkit.getPlayerExact(s).sendMessage(
							ChatColor.GOLD + "---[   " + ChatColor.GRAY
									+ "You are now " + ChatColor.YELLOW
									+ "SPECTATING." + ChatColor.GOLD
									+ "   ]---");
					if (settings.FLYING_SPECTATORS.value)
						Bukkit.getPlayerExact(s).setGameMode(GameMode.CREATIVE);
				}
			}
		} else if (hunted.contains(s)) {
			hunted.remove(s);
			spectator.add(s);
			if (HuntedAmount(false) == 0) {
				stop();

				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				broadcastAll(ChatColor.GOLD + "All of the " + ChatColor.BLUE
						+ "Prey" + ChatColor.GOLD + " are dead! The "
						+ ChatColor.DARK_RED + "Hunters" + ChatColor.GOLD
						+ " win the game!");
				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				HuntedPlugin.getInstance().log(Level.INFO,
						"-------------------------------------");
				HuntedPlugin
						.getInstance()
						.log(Level.INFO,
								"All of the Prey are now dead! The Hunters win the game!");
				HuntedPlugin.getInstance().log(Level.INFO,
						"-------------------------------------");
			} else {
				broadcastAll(ChatColor.BLUE + "Remaining Prey: "
						+ HuntedAmount(false) + ChatColor.DARK_RED
						+ "   Remaining Hunters: " + HuntersAmount(false));
				if (Bukkit.getPlayerExact(s) != null) {
					Bukkit.getPlayerExact(s).sendMessage(
							ChatColor.GRAY + "You are now " + ChatColor.YELLOW
									+ "spectating.");
					if (settings.FLYING_SPECTATORS.value)
						Bukkit.getPlayerExact(s).setGameMode(GameMode.CREATIVE);
				}
			}
		}
	}

	private void timeout(String p) {
		if (Bukkit.getPlayerExact(p) != null) {
			if (Bukkit.getPlayerExact(p).getWorld() != HuntedPlugin
					.getInstance().getWorld()) {
				if (timeout.containsKey(p)) {
					timeout.remove(p);
				}
			}
		}
		if (hunter.contains(p)) {
			broadcastAll(getColor(p) + p + ChatColor.WHITE
					+ " was disqualified for being gone more than "
					+ settings.OFFLINE_TIMEOUT.value + " minutes!");
			HuntedPlugin.getInstance().log(
					Level.INFO,
					p + " was disqualified for being gone more than "
							+ settings.OFFLINE_TIMEOUT.value + " minutes!");
			if (Bukkit.getPlayerExact(p) != null) {
				if (Bukkit.getPlayerExact(p) != null) {
					Bukkit.getPlayerExact(p)
							.sendMessage(
									ChatColor.RED
											+ "You were disqualified from the manhunt game!");
				}
			}
		} else if (hunted.contains(p)) {
			broadcastAll(getColor(p) + p + ChatColor.WHITE
					+ " was disqualified for being gone more than "
					+ settings.OFFLINE_TIMEOUT.value + " minutes!");
			HuntedPlugin.getInstance().log(
					Level.INFO,
					p + " was disqualified for being gone more than "
							+ settings.OFFLINE_TIMEOUT.value + " minutes!");
			if (Bukkit.getPlayerExact(p) != null) {
				Bukkit.getPlayerExact(p)
						.sendMessage(
								ChatColor.RED
										+ "You were disqualified from the manhunt game!");
			}
		}
		if (timeout.containsKey(p)) {
			timeout.remove(p);
		}
		onDie(p);
	}

	private void onTick() {
		@SuppressWarnings("unchecked")
		Set<String> set = ((HashMap<String, Long>) timeout.clone()).keySet();
		for (String p : set) {
			if (new Date().getTime() >= timeout.get(p)) {
				timeout(p);
			}
		}

		if (gameRunning) {
			long tick = HuntedPlugin.getInstance().getWorld().getFullTime();

			manageLocators();

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getWorld() == HuntedPlugin.getInstance().getWorld()) {
					if (isPlaying(p)) {
						if (settings.NORTH_COMPASS.value) {
							p.setCompassTarget(new Location(p.getWorld(), p
									.getLocation().getX(), p.getLocation()
									.getY(), p.getLocation().getZ() - 1000));
						} else {
							p.setCompassTarget(HuntedPlugin.getInstance()
									.getWorld().getSpawnLocation());
						}
						if (p.getGameMode() != GameMode.SURVIVAL) {
							p.setGameMode(GameMode.SURVIVAL);
						}
					} else if (isSpectating(p)) {

						if (settings.NORTH_COMPASS.value) {
							p.setCompassTarget(new Location(p.getWorld(), p
									.getLocation().getX(), p.getLocation()
									.getY(), p.getLocation().getZ() - 1000));
						} else {
							p.setCompassTarget(HuntedPlugin.getInstance()
									.getWorld().getSpawnLocation());
						}
						p.setFoodLevel(20);
						p.setSaturation(20);
						if (p.getGameMode() != GameMode.CREATIVE
								&& settings.FLYING_SPECTATORS.value) {
							p.setGameMode(GameMode.CREATIVE);
						}
					}
				}
			}

			for (String n : spectator) {
				Player p = Bukkit.getServer().getPlayerExact(n);
				if (p != null) {
					for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
						if (isHunter(p2) || isHunted(p2)) { // Makes spectators
															// invisible!
							((CraftPlayer) p2).getHandle().netServerHandler
									.sendPacket(new Packet29DestroyEntity(p
											.getEntityId()));
						}
					}
				}
			}

			if (hunterReleaseTick != 0 && tick < hunterReleaseTick) {
				int min;
				int sec;
				long totalsec = (hunterReleaseTick - tick) / 20;
				min = (int) (totalsec / 60);
				sec = (int) (totalsec % 60);
				if (HuntedPlugin.getInstance().spoutEnabled) {
					for (String s : hunter) {
						Player p = Bukkit.getPlayerExact(s);
						if (p != null) {
							HuntedPlugin.getInstance().spoutConnect.showTime(
									"Time until sundown", min, sec, p);
						}
					}
					for (String s : hunted) {
						Player p = Bukkit.getPlayerExact(s);
						if (p != null) {
							HuntedPlugin.getInstance().spoutConnect.showTime(
									"Time until sundown", min, sec, p);
						}
					}
					for (String s : spectator) {
						Player p = Bukkit.getPlayerExact(s);
						if (p != null) {
							HuntedPlugin.getInstance().spoutConnect.showTime(
									"Time until sundown", min, sec, p);
							p.setFoodLevel(20);
							p.setSaturation(20);
							p.setHealth(20);
						}
					}
				}
				if (tick < hunterReleaseTick) {
					if (tick >= hunterReleaseTick - 20 && countdown <= 11) {
						broadcastAll(ChatColor.GOLD + "1...");
						countdown = 12;
					} else if (tick >= hunterReleaseTick - 40
							&& countdown <= 10) {
						broadcastAll(ChatColor.GOLD + "2...");
						countdown = 11;
					} else if (tick >= hunterReleaseTick - 60 && countdown <= 9) {
						broadcastAll(ChatColor.GOLD + "3...");
						countdown = 10;
					} else if (tick >= hunterReleaseTick - 80 && countdown <= 8) {
						broadcastAll(ChatColor.GOLD + "4...");
						countdown = 9;
					} else if (tick >= hunterReleaseTick - 100
							&& countdown <= 7) {
						broadcastAll(ChatColor.GOLD + "5...");
						countdown = 8;
					} else if (tick >= hunterReleaseTick - 200
							&& countdown <= 6) {
						broadcastAll(ChatColor.GOLD
								+ "10 seconds until the hunt begins!");
						countdown = 7;
					} else if (tick >= hunterReleaseTick - 600
							&& countdown <= 5) {
						broadcastAll(ChatColor.GOLD
								+ "30 seconds until the hunt begins!");
						countdown = 6;
					} else if (tick >= hunterReleaseTick - 1200
							&& countdown <= 4) {
						broadcastAll(ChatColor.GOLD
								+ "1 minute until the hunt begins!");
						HuntedPlugin.getInstance().log(Level.INFO,
								"1 minute until the hunt begins!");
						countdown = 5;
					} else if (tick >= hunterReleaseTick - 2400
							&& countdown <= 3) {
						broadcastAll(ChatColor.GOLD
								+ "2 minutes until the hunt begins!");
						countdown = 4;
					} else if (tick >= hunterReleaseTick - 3600
							&& countdown <= 2) {
						broadcastAll(ChatColor.GOLD
								+ "3 minutes until the hunt begins!");
						countdown = 3;
					} else if (tick >= hunterReleaseTick - 4800
							&& countdown <= 1) {
						broadcastAll(ChatColor.GOLD
								+ "4 minutes until the hunt begins!");
						countdown = 2;
					} else if (tick >= hunterReleaseTick - 6000
							&& countdown <= 0) {
						broadcastAll(ChatColor.GOLD
								+ "5 minutes until the hunt begins!");
						HuntedPlugin.getInstance().log(Level.INFO,
								"5 minutes until the hunt begins!");
						countdown = 1;
					}
				}
			} else if (tick >= hunterReleaseTick && hunterReleaseTick != 0) {
				hunterReleaseTick = 0;
				for (String s : hunter) {
					Player p = Bukkit.getPlayerExact(s);
					if (p != null) {
						if (settings.LOADOUTS.value)
						{
							p.getInventory().setContents(loadouts.getHunterLoadout().getContents());
							p.getInventory().setArmorContents(loadouts.getHunterLoadout().getArmor());
						}
						if (!ManhuntUtil.areNearby(settings.SPAWN_HUNTER.value,
								settings.SPAWN_PREY.value,
								settings.SPAWN_PROTECTION.value)) {
							p.teleport(ManhuntUtil.safeTeleport(ManhuntUtil.randomLocation(
									settings.SPAWN_HUNTER.value, 2)));
						}
						p.setHealth(20);
						p.setFoodLevel(20);
						p.setSaturation(12);
					}
				}
				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				broadcastAll(ChatColor.GOLD + "The hunt has begun! The "
						+ ChatColor.DARK_RED + "Hunters" + ChatColor.GOLD
						+ " are on the move!");
				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				HuntedPlugin.getInstance().log(Level.INFO,
						"--------------------------------------");
				HuntedPlugin.getInstance().log(Level.INFO,
						"The hunt has begun! The Hunters are on the move!");
				HuntedPlugin.getInstance().log(Level.INFO,
						"--------------------------------------");
				endTick = tick + settings.DAY_LIMIT.value * 24000;
			} else if (tick >= endTick) {
				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				broadcastAll(ChatColor.GOLD + "Time has run out! The "
						+ ChatColor.BLUE + "Prey" + ChatColor.GOLD
						+ " have won the game!");
				broadcastAll(ChatColor.GOLD
						+ "-----------------------------------------------------");
				HuntedPlugin.getInstance().log(Level.INFO,
						"-------------------------------------");
				HuntedPlugin.getInstance().log(Level.INFO,
						"Time has run out! The Prey have won the game!");
				HuntedPlugin.getInstance().log(Level.INFO,
						"-------------------------------------");
				stop();
			} else {
				int hour;
				int min;
				int sec;
				long totalsec = (endTick - tick) / 20;
				hour = (int) (totalsec / 3600);
				min = (int) (totalsec / 60);
				if (min > 60 && hour > 0) {
					min = min - ((int) Math.floor(min / 60)) * 60;
				}
				sec = (int) (totalsec % 60);
				if (HuntedPlugin.getInstance().spoutEnabled) {
					for (String s : hunter) {
						Player p = Bukkit.getPlayerExact(s);
						if (p != null) {
							HuntedPlugin.getInstance().spoutConnect.showTime(
									"Time until the game is over", hour, min,
									sec, p);
						}
					}
					for (String s : hunted) {
						Player p = Bukkit.getPlayerExact(s);
						if (p != null) {
							HuntedPlugin.getInstance().spoutConnect.showTime(
									"Time until the game is over", hour, min,
									sec, p);
						}
					}
					for (String s : spectator) {
						Player p = Bukkit.getPlayerExact(s);
						if (p != null) {
							HuntedPlugin.getInstance().spoutConnect.showTime(
									"Time until the game is over", hour, min,
									sec, p);
						}
					}
				}
				if (tick > hunterReleaseTick && tick <= endTick) {
					if (tick >= endTick - 20 && countdown <= 13) {
						broadcastAll(ChatColor.GOLD + "1...");
						countdown = 14;
					} else if (tick >= endTick - 40 && countdown <= 12) {
						broadcastAll(ChatColor.GOLD + "2...");
						countdown = 13;
					} else if (tick >= endTick - 60 && countdown <= 11) {
						broadcastAll(ChatColor.GOLD + "3...");
						countdown = 12;
					} else if (tick >= endTick - 80 && countdown <= 10) {
						broadcastAll(ChatColor.GOLD + "4...");
						countdown = 11;
					} else if (tick >= endTick - 100 && countdown <= 9) {
						broadcastAll(ChatColor.GOLD + "5...");
						countdown = 10;
					} else if (tick >= endTick - 200 && countdown <= 8) {
						broadcastAll(ChatColor.GOLD
								+ "10 seconds until the game is over!");
						countdown = 9;
					} else if (tick >= endTick - 600 && countdown <= 7) {
						broadcastAll(ChatColor.GOLD
								+ "30 seconds until the game is over!");
						countdown = 8;
					} else if (tick >= endTick - 1200 && countdown <= 6) {
						broadcastAll(ChatColor.GOLD
								+ "1 minute until the game is over!");
						HuntedPlugin.getInstance().log(Level.INFO,
								"1 minute until the game is over!");
						countdown = 7;
					} else if (tick >= endTick - 2400 && countdown <= 5) {
						broadcastAll(ChatColor.GOLD
								+ "2 minutes until the game is over!");
						countdown = 6;
					} else if (tick >= endTick - 3600 && countdown <= 4) {
						broadcastAll(ChatColor.GOLD
								+ "3 minutes until the game is over!");
						countdown = 5;
					} else if (tick >= endTick - 4800 && countdown <= 3) {
						broadcastAll(ChatColor.GOLD
								+ "4 minutes until the game is over!");
						countdown = 4;
					} else if (tick >= endTick - 6000 && countdown <= 2) {
						broadcastAll(ChatColor.GOLD
								+ "5 minutes until the game is over!");
						HuntedPlugin.getInstance().log(Level.INFO,
								"5 minutes until the game is over!");
						countdown = 3;
					} else if (tick >= endTick - 12000 && countdown <= 1) {
						broadcastAll(ChatColor.GOLD
								+ "The Manhunt game ends at the hunt begins!");
						HuntedPlugin.getInstance().log(Level.INFO,
								"The Manhunt game ends at the hunt begins!");
						countdown = 2;
					} else if (tick >= endTick - 24000 && countdown <= 0) {
						broadcastAll(ChatColor.GOLD
								+ "1 day until the game is over!");
						HuntedPlugin.getInstance().log(Level.INFO,
								"1 day until the game is over!");
						countdown = 1;
					}
				}
			}
		}
	}

	public void onLogin(Player p) {
		if (gameHasBegun() && (isHunter(p) || isHunted(p))) {
			broadcastAll(ChatColor.GOLD + "---[   " + getColor(p) + p.getName()
					+ ChatColor.WHITE + " is back in the hunt!"
					+ ChatColor.GOLD + "   ]---");
			HuntedPlugin.getInstance().log(Level.INFO,
					p.getName() + " is back in the hunt!");
			timeout.remove(p.getName());
		} else if (gameHasBegun()) {
			addSpectator(p);
			if (p.getGameMode() == GameMode.CREATIVE) {
				if (!creative.contains(p.getName()))
					creative.add(p.getName());
			}
			if (settings.FLYING_SPECTATORS.value) {
				p.setGameMode(GameMode.CREATIVE);
			} else {
				p.setGameMode(GameMode.SURVIVAL);
			}
			broadcastAll(ChatColor.YELLOW + p.getName() + ChatColor.WHITE
					+ " has become a " + ChatColor.YELLOW + "Spectator.");
			HuntedPlugin.getInstance().log(Level.INFO,
					p.getName() + " has become a Spectator.");
		} else if (!gameHasBegun()) {
			p.setCompassTarget(HuntedPlugin.getInstance().getWorld()
					.getSpawnLocation());
			if (settings.AUTO_JOIN.value) {
				addHunter(p);
			} else {
				addSpectator(p);
			}
			p.sendMessage(ChatColor.GOLD + "---[ " + ChatColor.DARK_GREEN
					+ "Type \"" + ChatColor.RED + "/m rules"
					+ ChatColor.DARK_GREEN
					+ "\" anytime to view the Manhunt rules!\""
					+ ChatColor.GOLD + " ]---");
			p.sendMessage(ChatColor.GOLD + "---[   " + ChatColor.DARK_GREEN
					+ "Type \"" + ChatColor.RED + "/m help"
					+ ChatColor.DARK_GREEN
					+ "\" anytime to view the Manhunt help!\"" + ChatColor.GOLD
					+ "  ]---");
		}
	}

	public void onLogout(Player p) {
		if (gameHasBegun()) {
			if (creative.contains(p.getName())) {
				p.setGameMode(GameMode.CREATIVE);
				creative.remove(p.getName());
			} else {
				p.setGameMode(GameMode.SURVIVAL);
				creative.remove(p.getName());
			}
		}

		if (isPlaying(p)) {
			if (gameHasBegun()) {
				if (settings.OFFLINE_TIMEOUT.value >= 0) {
					if (settings.OFFLINE_TIMEOUT.value > 0) {
						broadcastAll(ChatColor.GOLD + "---[   " + getColor(p)
								+ p.getName() + ChatColor.WHITE
								+ " has left the game!" + ChatColor.GOLD
								+ "  (" + settings.OFFLINE_TIMEOUT.value
								+ " min.)   ]---");
						p.sendMessage(ChatColor.GOLD + "---[   "
								+ ChatColor.RED + "You have "
								+ settings.OFFLINE_TIMEOUT.value
								+ " minutes till you are disqualified!");
						HuntedPlugin.getInstance().log(Level.INFO,
								p.getName() + " has left the game!");
						timeout.put(p.getName(), new Date().getTime()
								+ settings.OFFLINE_TIMEOUT.value * 72000);
					} else {
						broadcastAll(ChatColor.GOLD + "---[   " + getColor(p)
								+ p.getName() + ChatColor.WHITE
								+ " has left the game!" + ChatColor.GOLD
								+ "   ]---");
						p.sendMessage(ChatColor.GOLD + "---[   "
								+ ChatColor.RED
								+ "You have left the Manhunt game!");
						HuntedPlugin.getInstance().log(Level.INFO,
								p.getName() + " has left the game!");
						onDie(p);
					}
				}
			} else {
				deletePlayer(p.getName());
			}
		} else if (isSpectating(p)) {
			deletePlayer(p.getName());
			if (gameHasBegun()) {
				broadcastAll(ChatColor.YELLOW + p.getName() + ChatColor.WHITE
						+ " is no longer spectating.");
			}
		}
		if (getLocatorByPlayer(p) != -1) { // PLAYER IS IN LOCATOR LIST
			stopLocator(p);
		}
	}

	public void broadcastAll(String msg) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (isHunter(p) || isHunted(p) || isSpectating(p)) {
				p.sendMessage(msg);
			}
		}
	}

	public void broadcastHunters(String msg) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (isHunter(p)) {
				p.sendMessage(msg);
			}
		}
	}

	public void broadcastHunted(String msg) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (isHunted(p)) {
				p.sendMessage(msg);
			}
		}
	}

	public void broadcastSpectators(String msg) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (isSpectating(p)) {
				p.sendMessage(msg);
			}
		}
	}

	public boolean gameHasBegun() {
		return gameRunning;
	}

	public boolean huntHasBegun() {
		if (gameRunning && hunterReleaseTick == 0) {
			return true;
		} else
			return false;
	}

	public long getTick() {
		return HuntedPlugin.getInstance().getWorld().getFullTime();
	}

	public long getHunterReleaseTick() {
		return hunterReleaseTick;
	}

	public long getEndTick() {
		return endTick;
	}

	public boolean isHunter(Player p) {
		return isHunter(p.getName());
	}

	public boolean isHunted(Player p) {
		return isHunted(p.getName());
	}

	public boolean isSpectating(Player p) {
		return isSpectating(p.getName());
	}

	public boolean isCreative(Player p) {
		return isCreative(p.getName());
	}

	public boolean isHunter(String p) {
		for (String s : hunter) {
			if (s.equalsIgnoreCase(p))
				return true;
		}
		return false;
	}

	public boolean isHunted(String p) {
		for (String s : hunted) {
			if (s.equalsIgnoreCase(p))
				return true;
		}
		return false;
	}

	public boolean isSpectating(String p) {
		for (String s : spectator) {
			if (s.equalsIgnoreCase(p))
				return true;
		}
		return false;
	}

	public boolean isCreative(String p) {
		for (String s : creative) {
			if (s.equalsIgnoreCase(p))
				return true;
		}
		return false;
	}

	public boolean isPlaying(Player p) {
		return (isHunter(p) || isHunted(p));
	}

	public void addHunter(Player p) {
		addHunter(p.getName());
		p.setDisplayName(getColor(p) + p.getName());
	}

	public void addHunted(Player p) {
		addHunted(p.getName());
		p.setDisplayName(getColor(p) + p.getName());
	}

	public void addSpectator(Player p) {
		addSpectator(p.getName());
		p.setDisplayName(getColor(p) + p.getName());
	}

	public void addHunter(String p) {
		if (Bukkit.getPlayerExact(p) == null) {
			return;
		} else {
			p = Bukkit.getPlayerExact(p).getName();
		}
		if (hunter.contains(p)) {
			hunter.remove(p);
		}
		if (hunted.contains(p)) {
			hunted.remove(p);
		}
		if (spectator.contains(p)) {
			spectator.remove(p);
		}
		hunter.add(p);
	}

	public void addHunted(String p) {
		if (Bukkit.getPlayerExact(p) == null) {
			return;
		} else {
			p = Bukkit.getPlayerExact(p).getName();
		}
		if (hunter.contains(p)) {
			hunter.remove(p);
		}
		if (hunted.contains(p)) {
			hunted.remove(p);
		}
		if (spectator.contains(p)) {
			spectator.remove(p);
		}
		hunted.add(p);
	}

	public void addSpectator(String p) {
		if (Bukkit.getPlayerExact(p) == null) {
			return;
		} else {
			p = Bukkit.getPlayerExact(p).getName();
		}
		if (hunter.contains(p)) {
			hunter.remove(p);
		}
		if (hunted.contains(p)) {
			hunted.remove(p);
		}
		if (spectator.contains(p)) {
			spectator.remove(p);
		}
		spectator.add(p);
	}

	public void deletePlayer(String p) {
		if (isHunter(p)) {
			hunter.remove(p);
		}
		if (isHunted(p)) {
			hunted.remove(p);
		}
		if (isSpectating(p)) {
			spectator.remove(p);
		}
		if (timeout.containsKey(p)) {
			timeout.remove(p);
		}
		if (locator.contains(p)) {
			locator.remove(p);
		}
	}

	public void reloadPlayers() {
		if (this.gameRunning)
			return;
		hunter.clear();
		hunted.clear();
		spectator.clear();
		timeout.clear();
		locator.clear();
		creative.clear();

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getWorld() == HuntedPlugin.getInstance().getWorld()) {
				if (settings.AUTO_JOIN.value) {
					addHunter(p);
				} else {
					addSpectator(p);
				}
			}
		}
	}

	public void quit(Player p) {
		if (isHunter(p) || isHunted(p)) {
			onDie(p);
			broadcastAll(getColor(p) + p.getName() + ChatColor.WHITE
					+ " has quit the game and is now spectating.");
		}
	}

	public ChatColor getColor(Player p) {
		if (p == null) {
			return ChatColor.YELLOW;
		} else if (isHunter(p)) {
			return ChatColor.DARK_RED;
		} else if (isHunted(p)) {
			return ChatColor.BLUE;
		} else if (isSpectating(p)) {
			return ChatColor.YELLOW;
		} else {
			return ChatColor.WHITE;
		}
	}

	public ChatColor getColor(String p) {
		return getColor(Bukkit.getPlayerExact(p));
	}

	public List<String> getHunters() {
		return hunter;
	}

	public List<String> getHunted() {
		return hunted;
	}

	public List<String> getSpectators() {
		return spectator;
	}

	public List<String> getCreative() {
		return creative;
	}

	public int HuntersAmount(Boolean online) {
		if (online) {
			int num = 0;
			for (String k : hunter) {
				if (Bukkit.getPlayerExact(k) != null) {
					num++;
				}
			}
			return num;
		} else {
			return hunter.size();
		}
	}

	public int HuntedAmount(Boolean online) {
		if (online) {
			int num = 0;
			for (String k : hunted) {
				if (Bukkit.getPlayerExact(k) != null) {
					num++;
				}
			}
			return num;
		} else {
			return hunted.size();
		}
	}

	public int SpectatorsAmount(Boolean online) {
		if (online) {
			int num = 0;
			for (String k : spectator) {
				if (Bukkit.getPlayerExact(k) != null) {
					num++;
				}
			}
			return num;
		} else {
			return spectator.size();
		}
	}

	public Location getNearestLocation(Location location, Location preySpawn,
			Location hunterSpawn) {
		double scalar = ((((hunterSpawn.getX() - preySpawn.getX()) * (location
				.getX() - preySpawn.getX()))
				+ ((hunterSpawn.getY() - preySpawn.getY()) * (location.getY() - preySpawn
						.getY())) + ((hunterSpawn.getZ() - preySpawn.getZ()) * (location
				.getZ() - preySpawn.getZ()))) / (((hunterSpawn.getX() - preySpawn
				.getX()) * (hunterSpawn.getX() - preySpawn.getX()))
				+ ((hunterSpawn.getY() - preySpawn.getY()) * (hunterSpawn
						.getY() - preySpawn.getY())) + ((hunterSpawn.getZ() - preySpawn
				.getZ()) * (hunterSpawn.getZ() - preySpawn.getZ()))));
		Location projectedLocation = new Location(HuntedPlugin.getInstance()
				.getWorld(), (preySpawn.getX() + scalar
				* (hunterSpawn.getX() - preySpawn.getX())),
				(preySpawn.getY() + scalar
						* (hunterSpawn.getY() - preySpawn.getY())),
				(preySpawn.getZ() + scalar
						* (hunterSpawn.getZ() - preySpawn.getZ())));
		if (scalar > 1 || scalar < 0) {
			if (ManhuntUtil.getDistance(preySpawn, location) < ManhuntUtil.getDistance(hunterSpawn,
					location)) {
				return preySpawn;
			} else {
				return hunterSpawn;
			}
		} else {
			return projectedLocation;
		}

	}

	public boolean outsideBoxedArea(Location loc, boolean pregame) {
		if (pregame) {
			if (loc.getX() < settings.SPAWN_SETUP.value.getX()
					- settings.BOUNDARY_SETUP.value)
				return true;
			if (loc.getX() > settings.SPAWN_SETUP.value.getX()
					+ settings.BOUNDARY_SETUP.value)
				return true;
			if (loc.getZ() < settings.SPAWN_SETUP.value.getZ()
					- settings.BOUNDARY_SETUP.value)
				return true;
			if (loc.getZ() > settings.SPAWN_SETUP.value.getZ()
					+ settings.BOUNDARY_SETUP.value)
				return true;
			return false;
		} else {
			if (loc.getX() < settings.SPAWN_HUNTER.value.getX()
					- settings.BOUNDARY_WORLD.value
					&& loc.getX() < settings.SPAWN_PREY.value.getX()
							- settings.BOUNDARY_WORLD.value)
				return true;
			if (loc.getX() > settings.SPAWN_HUNTER.value.getX()
					+ settings.BOUNDARY_WORLD.value
					&& loc.getX() > settings.SPAWN_PREY.value.getX()
							+ settings.BOUNDARY_WORLD.value)
				return true;
			if (loc.getZ() < settings.SPAWN_HUNTER.value.getZ()
					- settings.BOUNDARY_WORLD.value
					&& loc.getZ() < settings.SPAWN_PREY.value.getZ()
							- settings.BOUNDARY_WORLD.value)
				return true;
			if (loc.getZ() > settings.SPAWN_HUNTER.value.getZ()
					+ settings.BOUNDARY_WORLD.value
					&& loc.getZ() > settings.SPAWN_PREY.value.getZ()
							+ settings.BOUNDARY_WORLD.value)
				return true;
			return false;
		}
	}

	public Location teleportPregameBoxedLocation(Location loc) {
		Location newLoc = loc;
		if (loc.getX() < settings.SPAWN_SETUP.value.getX()
				- settings.BOUNDARY_SETUP.value) {
			newLoc.setX(newLoc.getX() + 1);
		}
		if (loc.getX() > settings.SPAWN_SETUP.value.getX()
				+ settings.BOUNDARY_SETUP.value) {
			newLoc.setX(newLoc.getX() - 1);
		}
		if (loc.getZ() < settings.SPAWN_SETUP.value.getZ()
				- settings.BOUNDARY_SETUP.value) {
			newLoc.setZ(newLoc.getZ() + 1);
		}
		if (loc.getZ() > settings.SPAWN_SETUP.value.getZ()
				+ settings.BOUNDARY_SETUP.value) {
			newLoc.setZ(newLoc.getZ() - 1);
		}
		return newLoc;
	}

	public Location teleportBoxedLocation(Location loc) {
		Location newLoc = loc;
		if (loc.getX() < settings.SPAWN_HUNTER.value.getX()
				- settings.BOUNDARY_WORLD.value
				&& loc.getX() < settings.SPAWN_PREY.value.getX()
						- settings.BOUNDARY_WORLD.value) {
			newLoc.setX(loc.getX() + 1);
		}
		if (loc.getX() > settings.SPAWN_HUNTER.value.getX()
				+ settings.BOUNDARY_WORLD.value
				&& loc.getX() > settings.SPAWN_PREY.value.getX()
						+ settings.BOUNDARY_WORLD.value) {
			newLoc.setX(loc.getX() - 1);
		}
		if (loc.getZ() < settings.SPAWN_HUNTER.value.getZ()
				- settings.BOUNDARY_WORLD.value
				&& loc.getZ() < settings.SPAWN_PREY.value.getZ()
						- settings.BOUNDARY_WORLD.value) {
			newLoc.setZ(loc.getZ() + 1);
		}
		if (loc.getZ() > settings.SPAWN_HUNTER.value.getZ()
				+ settings.BOUNDARY_WORLD.value
				&& loc.getZ() > settings.SPAWN_PREY.value.getZ()
						+ settings.BOUNDARY_WORLD.value) {
			newLoc.setZ(loc.getZ() - 1);
		}
		return newLoc;
	}

	public void manageLocators() {
		for (int i = 0; i < locator.size(); i++) {
			if (getLocatorPlayer(i).getItemInHand().getType() != Material.COMPASS
					&& getLocatorStage(i) != 2) {
				if (getLocatorPlayer(i) != null) {
					getLocatorPlayer(i)
							.sendMessage(
									ChatColor.RED
											+ "You're not holding a compass! Prey Locator 9001 cancelled!");
				}
				stopLocator(i);
				return;
			}
			if (getTick() >= getLocatorTick(i)
					- (settings.FINDER_COOLDOWN.value * 20 + 25)
					&& getLocatorStage(i) == 0) {
				if (getLocatorPlayer(i) != null) {
					getLocatorPlayer(i).sendMessage(ChatColor.GOLD + "Got it!");
				}
				setLocatorStage(i, 1);
			} else if (getTick() >= getLocatorTick(i) - settings.FINDER_COOLDOWN.value
					* 20
					&& getLocatorStage(i) == 1) {
				if (getLocatorPlayer(i) == null) {
					return;
				}
				if (getLocatorPlayer(i).getFoodLevel() < locatorFood()) {
					getLocatorPlayer(i)
							.sendMessage(
									ChatColor.RED
											+ "Not enough food to fuel the Prey Locator 9001!");
					getLocatorPlayer(i).sendMessage(
							ChatColor.RED + "You need at least "
									+ locatorFood() / 2 + " food nuggets!");
					stopLocator(i);
					return;
				}
				if (ManhuntUtil.getDistance(getLocatorPlayer(i).getLocation(),
						getLocatorLocation(i)) > 1.5) {
					getLocatorPlayer(i)
							.sendMessage(
									ChatColor.RED
											+ "You've moved too far. Could not get direction!");
					stopLocator(i);
					return;
				}
				if (HuntedAmount(true) == 0) {
					getLocatorPlayer(i).sendMessage(
							ChatColor.RED + "There are no Prey online!");
					stopLocator(i);
					return;
				}
				sendNearestPrey(getLocatorPlayer(i));
				getLocatorPlayer(i).setFoodLevel(
						getLocatorPlayer(i).getFoodLevel() - 4);
				getLocatorPlayer(i).sendMessage(
						ChatColor.YELLOW + "You have lost " + ChatColor.RED
								+ "2 food.");
				setLocatorStage(i, 2);
				HuntedPlugin.getInstance().log(
						Level.INFO,
						getLocatorPlayer(i).getName()
								+ " used the Prey Locator 9001");
			} else if (getTick() >= getLocatorTick(i)) {
				if (getLocatorPlayer(i) != null) {
					getLocatorPlayer(i).sendMessage(
							ChatColor.GOLD + "---[   " + ChatColor.WHITE
									+ "The " + ChatColor.GOLD
									+ "Prey Locator 9001" + ChatColor.WHITE
									+ " is fully charged!" + ChatColor.GOLD
									+ "   ]---");
				}
				stopLocator(getLocatorPlayer(i));
			}
		}
	}

	public void startLocator(Player p) {
		stopLocator(p);
		locator.add(p.getName() + "/" + p.getLocation().getX() + ","
				+ p.getLocation().getY() + "," + p.getLocation().getZ() + "/"
				+ (getTick() + 160 + (settings.FINDER_COOLDOWN.value * 20)) + "/"
				+ "0");
	}

	public long getLocatorTick(int i) {
		try {
			return Long.parseLong(locator.get(i).split("/")[2]);
		} catch (Exception e) {
			return 0;
		}
	}

	public Player getLocatorPlayer(int i) {
		try {
			return Bukkit.getPlayerExact(locator.get(i).split("/")[0]);
		} catch (Exception e) {
			return null;
		}
	}

	public int getLocatorByPlayer(Player p) {
		for (int i = 0; i < locator.size(); i++) {
			if (p == getLocatorPlayer(i)) {
				return i;
			}
		}
		return -1;
	}

	public Location getLocatorLocation(int i) {
		try {
			return new Location(
					HuntedPlugin.getInstance().getWorld(),
					Double.parseDouble(locator.get(i).split("/")[1].split(",")[0]),
					Double.parseDouble(locator.get(i).split("/")[1].split(",")[1]),
					Double.parseDouble(locator.get(i).split("/")[1].split(",")[2]));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public int getLocatorStage(int i) {
		try {
			return Integer.parseInt((locator.get(i).split("/")[3]));
		} catch (Exception e) {
			return -0;
		}
	}

	public void setLocatorStage(int i, int stage) {
		String val = (locator.get(i).subSequence(0,
				locator.get(i).lastIndexOf("/"))).toString()
				+ "/" + stage;
		locator.set(i, val);
	}

	public void stopLocator(Player p) {
		for (int i = 0; i < locator.size(); i++) {
			if (p == getLocatorPlayer(i)) {
				locator.remove(i);
			}
		}
	}

	public void stopLocator(int i) {
		locator.remove(i);
	}

	public void sendNearestPrey(Player p) {
		Player p2 = null;

		for (String prey : getHunted()) {
			if (Bukkit.getPlayerExact(prey) != null) {
				Player p3 = Bukkit.getPlayerExact(prey);
				if (p2 == null) {
					p2 = p3;
				} else {
					if (p3 != null && ManhuntUtil.getDistance(p, p3) < ManhuntUtil.getDistance(p, p2)) {
						p2 = p3;
					}
				}
			}
		}
		if (p2 == null) {
			p.sendMessage(ChatColor.RED
					+ "Could not find prey, because none are online! D:");
			return;
		}

		if (ManhuntUtil.getDistance(p, p2) < 10) {
			p.sendMessage(ChatColor.GOLD + "The nearest prey " + ChatColor.BLUE
					+ "Prey" + ChatColor.GOLD + " is " + ChatColor.BLUE
					+ "very close by" + ChatColor.GOLD + "!");
			return;
		}

		String msg1 = "";
		String msg2 = "";

		double direction = Math.toDegrees(Math
				.acos((p2.getLocation().getZ() - p.getLocation().getZ())
						/ ManhuntUtil.getDistance(p, p2)));
		if (p2.getLocation().getX() < p.getLocation().getX()) {
			direction = 180 - direction + 180;
		}

		if (direction > 338)
			msg1 = "South";
		else if (direction > 293)
			msg1 = "South-West";
		else if (direction > 248)
			msg1 = "West";
		else if (direction > 203)
			msg1 = "North-West";
		else if (direction > 158)
			msg1 = "North";
		else if (direction > 113)
			msg1 = "North-East";
		else if (direction > 68)
			msg1 = "East";
		else if (direction > 23)
			msg1 = "South-East";
		else
			msg1 = "South";

		double relative = p.getLocation().getYaw();
		if (relative < 0)
			relative = relative + 360;
		relative = 360 - relative;
		relative = direction - relative;
		if (relative < 0)
			relative = relative + 360;

		if (relative > 315)
			msg2 = "ahead of you";
		else if (relative > 225)
			msg2 = "to your right";
		else if (relative > 135)
			msg2 = "behind you";
		else if (relative > 45)
			msg2 = "to your left";
		else
			msg2 = "ahead of you";

		p.sendMessage(ChatColor.GOLD + "The nearest Prey is " + ChatColor.BLUE
				+ msg1 + ChatColor.GOLD + " of you! (Somehere "
				+ ChatColor.BLUE + msg2 + ChatColor.GOLD + ".)");
		p2.sendMessage(ChatColor.GOLD + "--[   " + ChatColor.RED + "A "
				+ ChatColor.DARK_RED + "Prey Finder 9000" + ChatColor.RED
				+ " has gotten your location!" + ChatColor.GOLD + "   ]---");
	}

	public int locatorFood() {
		int food = HuntersAmount(false);
		if (food < 4)
			food = 4;
		if (food > 8)
			food = 8;

		return food;
	}

}
