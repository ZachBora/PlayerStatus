package com.worldcretornica.playerstatus;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PSPlayerListener extends PlayerListener {
	public static PlayerStatus plugin;
	
	public PSPlayerListener(PlayerStatus instance)
	{
		plugin = instance;
	}
	
	
	/* Commented until find how to prevent players from pushing others 
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		
		ExPlayer ep = plugin.getExPlayer(event.getPlayer());

		if (ep.isAfk)
		{
			plugin.toggleAfk(ep);
		}
		
		super.onPlayerMove(event);
	}*/
	
	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		if (event.isCancelled()) return;
				
		String message = event.getMessage();
		if (message == null) return;
		
		Player[] recipients = event.getRecipients().toArray(new Player[0]);
		Player player = event.getPlayer();
		
		String format = event.getFormat();

		ExPlayer ep = plugin.getExPlayer(player);
		
		if (ep.isMuted || (plugin.isModerated && !plugin.checkPermissions(player, "PlayerStatus.moderate")))
		{
			ep.player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("MsgPlayerMuted"));
			event.setCancelled(true);
			return;
		}
		
		if (ep.isDnd)
		{
			event.setFormat(plugin.configlanguage.getString("DNDPrefix") + format + "");
		}
		if (ep.isAfk)
		{
			plugin.toggleAfk(ep);
		}
		
		for(int r = 0; r < recipients.length; r++)
		{
			ep = plugin.getExPlayer(recipients[r]);
			
			if (ep.isNochat || ep.IsIgnoring(player))
			{
				event.getRecipients().remove(ep.player);
			}
		}
	}
		
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.Broadcast(ChatColor.YELLOW + plugin.configlanguage.getString("PlayerJoin").replace("%player%", event.getPlayer().getName()));
		event.setJoinMessage(null);
		super.onPlayerJoin(event);
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.Broadcast(ChatColor.YELLOW + plugin.configlanguage.getString("PlayerQuit").replace("%player%", event.getPlayer().getName()));
		event.setQuitMessage(null);
		super.onPlayerQuit(event);
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		
		String message = event.getMessage();
		Player player = event.getPlayer();

		if (message.toLowerCase().startsWith("/me ")) {
			
			ExPlayer pSource = plugin.getExPlayer(player);
			if (pSource.isMuted || (plugin.isModerated && !plugin.checkPermissions(player, "PlayerStatus.moderate")))
			{
				pSource.player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("MsgPlayerMuted"));
				event.setCancelled(true);
				return;
			}
			
			String s = message.substring(message.indexOf(" ")).trim();
			
			for (Player t : plugin.getServer().getOnlinePlayers())
			{
				ExPlayer p = plugin.getExPlayer(t);
				if (!p.isNochat && !p.IsIgnoring(player))
				{
					t.sendMessage("* " + player.getName() + " " + s);
				}
			}
			event.setCancelled(true);
		}
		
		if (message.toLowerCase().startsWith("/msg ") || message.toLowerCase().startsWith("/tell ") || message.toLowerCase().startsWith("/r "))
		{
			boolean isReply = message.toLowerCase().startsWith("/r ");
			
			if (!message.contains(" "))
			{
				player.sendMessage(plugin.configlanguage.getString("ErrMsgFormat"));
				event.setCancelled(true);
				return;
			}else{
				message = message.substring(message.indexOf(" ")).trim();
			}
			
			String name;
			String msg;
			
			if (isReply)
			{
				ExPlayer ep = plugin.getExPlayer(player);
				
				if (ep.LastMessenger == null)
				{
					player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("ErrNoReply"));
					event.setCancelled(true);
					return;
				}
				
				name = plugin.getExPlayer(player).LastMessenger.getName();

				msg = message;
			}else{
				if (!message.contains(" "))
				{
					player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("ErrMsgFormat"));
					event.setCancelled(true);
					return;
				}else{
					name = message.substring(0, message.indexOf(" "));
					if (!message.contains(" "))
					{
						player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("ErrMsgFormat"));
						event.setCancelled(true);
						return;
					}else{
						msg = message.substring(message.indexOf(" ")).trim();
					}
				}
			}
			
			List<Player> recipients;
			Player recipient;

			if (name.substring(0, 1).equalsIgnoreCase("@"))
			{

				recipient = plugin.getServer().getPlayerExact(name.substring(1));
			}else{
				recipients = plugin.getServer().matchPlayer(name);

				if (recipients.size() > 1)
				{
					player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("MsgTooManyPlayerFound"));
					event.setCancelled(true);
					return;
				}else if(recipients.size() == 0)
				{
					player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("MsgPlayerNotFound").replace("%player%", name));
					event.setCancelled(true);
					return;
				}

				recipient = recipients.get(0);
			}
			
			if (player.getName().equals(recipient.getName()))
			{
				player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("MsgCantMsgSelf"));
			}else{

				int i = plugin.playerlist.indexOf(plugin.getExPlayer(recipient));
								
				if (i != -1)
				{
					ExPlayer p = plugin.playerlist.get(i);
					
					if (p.isDnd)
					{
						player.sendMessage(ChatColor.GRAY + plugin.configlanguage.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
						player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("MsgPlayerIsDND").replace("%player%", recipient.getName()));
					}else if (p.isAfk)
					{
						player.sendMessage(ChatColor.GRAY + plugin.configlanguage.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
						player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("MsgPlayerIsAFK").replace("%player%", recipient.getName()));
					}else if (p.isNomsg)
					{
						player.sendMessage(ChatColor.GRAY + plugin.configlanguage.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
						if (!player.isOp())
						{
							player.sendMessage(ChatColor.RED + plugin.configlanguage.getString("MsgPlayerIsNoMsg").replace("%player%", recipient.getName()));
						}
					}else{
						player.sendMessage(ChatColor.GRAY + plugin.configlanguage.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
					}
					
					if (!p.isNomsg || player.isOp())
					{
						if (!p.IsIgnoring(player))
						{
							recipient.sendMessage(ChatColor.GRAY + plugin.configlanguage.getString("MsgPrivateFrom").replace("%player%", player.getName()) + " " + ChatColor.WHITE + msg);
							plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " told " + recipient.getName() + ": " + msg);
							
							plugin.getExPlayer(recipient).LastMessenger = player;
							
						}else{
							plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " was ignored by " + recipient.getName() + ": " + msg);
						}
					}else{
						plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " could not tell to " + recipient.getName() + " because of NoMsg status: " + msg);
					}
					
				}else{
					player.sendMessage(ChatColor.GRAY + plugin.configlanguage.getString("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + ChatColor.WHITE + msg);
					recipient.sendMessage(ChatColor.GRAY + plugin.configlanguage.getString("MsgPrivateFrom").replace("%player%", player.getName()) + " " + ChatColor.WHITE + msg);
					plugin.getExPlayer(recipient).LastMessenger = player;
					plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " told " + recipient.getName() + ": " + msg);
				}
			}
			event.setCancelled(true);
		}
	}
	
	
	
}
