package com.bendude56.hunted.game.events;

import com.bendude56.hunted.Manhunt;
import com.bendude56.hunted.lobby.Team;

public class BroadcastAction implements Action
{

	private long lobby_id;
	private String message;
	private Team[] teams;
	
	public BroadcastAction(long lobbyId, String message, Team...teams)
	{
		this.lobby_id = lobbyId;
		this.message = message;
		this.teams = teams;
	}
	
	@Override
	public void execute()
	{
		Manhunt.getLobby(lobby_id).broadcast(message, teams);
	}

}