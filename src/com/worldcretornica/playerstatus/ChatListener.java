package com.worldcretornica.playerstatus;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener extends PlayerListener {
	public static PlayerStatus plugin;
	
	public ChatListener(PlayerStatus instance)
	{
		plugin = instance;
	}
	
	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		if (event.isCancelled()) return;
				
		String message = event.getMessage();
		if (message == null) return;
		
		Player[] recipients = event.getRecipients().toArray(new Player[0]);
		Player player = event.getPlayer();
		
		String format = event.getFormat();

		int i = plugin.playerlist.indexOf(plugin.getExPlayer(player));
		
		if (i != -1)
		{
			ExPlayer p = plugin.playerlist.get(i);
			if (p.isMuted)
			{
				p.player.sendMessage(ChatColor.RED + plugin.config.getString("MsgPlayerMuted"));
				event.setCancelled(true);
				return;
			}
			
			if (p.isDnd)
			{
				event.setFormat(plugin.config.getString("DNDPrefix") + format + "");
			}else if (p.isAfk)
			{
				event.setFormat(plugin.config.getString("AFKPrefix") + format + "");
			}
		}
		
		for(int r = 0; r < recipients.length; r++)
		{
			i = plugin.playerlist.indexOf(plugin.getExPlayer(recipients[r]));
			
			if (i != -1)
			{
				ExPlayer ep = plugin.playerlist.get(i);
				if (ep.isNochat || ep.IsIgnoring(player))
				{
					event.getRecipients().remove(ep.player);
				}
			}
		}
	}
		
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.Broadcast(ChatColor.YELLOW + plugin.config.getString("PlayerJoin").replace("%player%", event.getPlayer().getName()));
		event.setJoinMessage(null);
		super.onPlayerJoin(event);
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.Broadcast(ChatColor.YELLOW + plugin.config.getString("PlayerQuit").replace("%player%", event.getPlayer().getName()));
		event.setQuitMessage(null);
		super.onPlayerQuit(event);
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		
		String message = event.getMessage();
		Player player = event.getPlayer();

		if (message.toLowerCase().startsWith("/me ")) {
			
			int iSource = plugin.playerlist.indexOf(plugin.getExPlayer(player));
			
			if (iSource != -1)
			{
				ExPlayer pSource = plugin.playerlist.get(iSource);
				if (pSource.isMuted)
				{
					event.setCancelled(true);
					return;
				}
			}
			
			String s = message.substring(message.indexOf(" ")).trim();
			
			for (Player t : plugin.getServer().getOnlinePlayers())
			{
				int iDest = plugin.playerlist.indexOf(plugin.getExPlayer(t));
				
				if (iDest != -1)
				{
					ExPlayer p = plugin.playerlist.get(iDest);
					if (!p.isNochat && !p.IsIgnoring(player))
					{
						t.sendMessage("* " + player.getName() + " " + s);
					}
				}else{
					t.sendMessage("* " + player.getName() + " " + s);
				}
			}
			event.setCancelled(true);
		}
		
		if (message.toLowerCase().startsWith("/msg ") || message.toLowerCase().startsWith("/tell "))
		{
			message = message.substring(message.indexOf(" ")).trim();
			String name = message.substring(0, message.indexOf(" "));
			String msg = message.substring(message.indexOf(" ")).trim();
			
			List<Player> recipients;
			Player recipient;

			if (name.substring(0, 1).equalsIgnoreCase("@"))
			{

				recipient = plugin.getServer().getPlayerExact(name.substring(1));
			}else{
				recipients = plugin.getServer().matchPlayer(name);

				if (recipients.size() > 1)
				{
					player.sendMessage(ChatColor.RED + plugin.config.getString("MsgTooManyPlayerFound"));
					event.setCancelled(true);
					return;
				}else if(recipients.size() == 0)
				{
					player.sendMessage(ChatColor.RED + plugin.config.getString("MsgPlayerNotFound").replace("%player%", name));
					event.setCancelled(true);
					return;
				}

				recipient = recipients.get(0);
			}
			
			if (player.getName().equals(recipient.getName()))
			{
				player.sendMessage(ChatColor.RED + plugin.config.getString("MsgCantMsgSelf"));
			}else{

				int i = plugin.playerlist.indexOf(plugin.getExPlayer(recipient));
								
				if (i != -1)
				{
					ExPlayer p = plugin.playerlist.get(i);
					
					if (p.isDnd)
					{
						player.sendMessage(ChatColor.GRAY + plugin.config.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
						player.sendMessage(ChatColor.RED + plugin.config.getString("MsgPlayerIsDND").replace("%player%", recipient.getName()));
					}else if (p.isAfk)
					{
						player.sendMessage(ChatColor.GRAY + plugin.config.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
						player.sendMessage(ChatColor.RED + plugin.config.getString("MsgPlayerIsAFK").replace("%player%", recipient.getName()));
					}else if (p.isNomsg)
					{
						player.sendMessage(ChatColor.GRAY + plugin.config.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
						if (!player.isOp())
						{
							player.sendMessage(ChatColor.RED + plugin.config.getString("MsgPlayerIsNoMsg").replace("%player%", recipient.getName()));
						}
					}else{
						player.sendMessage(ChatColor.GRAY + plugin.config.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
					}
					
					if (!p.isNomsg || player.isOp())
					{
						if (!p.IsIgnoring(player))
						{
							recipient.sendMessage(ChatColor.GRAY + plugin.config.getString("MsgPrivateFrom").replace("%player%", player.getName()) + " " + ChatColor.WHITE + msg);
							plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " told " + recipient.getName() + ": " + msg);
						}else{
							plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " was ignored by " + recipient.getName() + ": " + msg);
						}
					}else{
						plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " could not tell to " + recipient.getName() + " because of NoMsg status: " + msg);
					}
					
				}else{
					player.sendMessage(ChatColor.GRAY + plugin.config.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
					recipient.sendMessage(ChatColor.GRAY + plugin.config.getString("MsgPrivateFrom").replace("%player%", player.getName()) + " " + ChatColor.WHITE + msg);
					plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " told " + recipient.getName() + ": " + msg);
				}
			}
			event.setCancelled(true);
		}
	}
	
	
	
}
