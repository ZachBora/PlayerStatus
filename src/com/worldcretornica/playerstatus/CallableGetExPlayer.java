package com.worldcretornica.playerstatus;

import java.util.concurrent.Callable;

import org.bukkit.entity.Player;

public class CallableGetExPlayer implements Callable<ExPlayer>
{
	private Player player;
	private PlayerStatus plugin;
	
	CallableGetExPlayer(Player p, PlayerStatus ps)
	{
		player = p;
		plugin = ps;
	}

	@Override
	public ExPlayer call() throws Exception {
		return plugin.getExPlayer(player);
	}

}
