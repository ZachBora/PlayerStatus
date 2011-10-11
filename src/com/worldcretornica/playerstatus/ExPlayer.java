package com.worldcretornica.playerstatus;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

public class ExPlayer  {
	
	public Player player;
	
	public ExPlayer(Player p) {
		player = p;
		isAfk = false;
		isDnd = false;
		isNomsg = false;
		isNochat = false;
		isMuted = false;
		ignoredplayers = new HashSet<Player>();
	}
			
	public boolean isAfk;
	public boolean isDnd;
	public boolean isNomsg;
	public boolean isNochat;
	public boolean isMuted;
	public Player LastMessenger;
	public long timeAfked;
	public long timeDnded;
	public long timeUnset;
	
	public Set<Player> ignoredplayers;
	
	public boolean IsIgnoring(Player player)
	{
		return ignoredplayers.contains(player);
	}
	public void addIgnoring(Player player)
	{
		ignoredplayers.add(player);
	}
	public void removeIgnoring(Player player)
	{
		ignoredplayers.remove(player);
	}

}
