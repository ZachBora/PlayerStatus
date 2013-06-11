package com.worldcretornica.playerstatus;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PSPlayerListener implements Listener
{
	
	public static PlayerStatus plugin;
	
	public PSPlayerListener(PlayerStatus instance)
	{
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		if (event.isCancelled()) return;
				
		String message = event.getMessage();
		if (message == null) return;
		
		Player[] recipients = event.getRecipients().toArray(new Player[0]);
		Player player = event.getPlayer();
		
		String format = event.getFormat();
		
		ExPlayer ep = PlayerStatus.playerlist.get(player.getName());
		
		if(ep == null)
			ep = new ExPlayer();
				
		if (ep.isMuted || (plugin.isModerated && !plugin.checkPermissions(player, "PlayerStatus.moderate")))
		{
			player.sendMessage(ChatColor.RED + plugin.getLangConfig("MsgPlayerMuted"));
			event.setCancelled(true);
			return;
		}
		
		if (ep.isDnd)
		{
			event.setFormat(plugin.getLangConfig("DNDPrefix") + format + "");
		}
		if (ep.isAfk)
		{
			plugin.toggleAfk(player, ep);
		}
		
		for(int r = 0; r < recipients.length; r++)
		{			
			ep = PlayerStatus.playerlist.get(recipients[r].getName());
			
			if (ep != null && (ep.isNochat || ep.IsIgnoring(player.getName())))
			{
				event.getRecipients().remove(recipients[r]);
			}
		}
	}
		
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent event) 
	{
		if(plugin.configmain.getString("EnableLoginMessages").equalsIgnoreCase("true"))
		{
			if(plugin.configmain.getString("CustomLoginMessages").equalsIgnoreCase("true"))
			{
				Random rand = new Random();
				
				int line = rand.nextInt(plugin.loginquote.size());
				
				for(Player p : Bukkit.getServer().getOnlinePlayers())
				{
					if(p.canSee(event.getPlayer()))
						p.sendMessage(ChatColor.YELLOW + plugin.addColor(plugin.loginquote.get(line).replace("%player%", event.getPlayer().getDisplayName())));
				}
			}
			else
			{
				for(Player p : Bukkit.getServer().getOnlinePlayers())
				{
					if(p.canSee(event.getPlayer()))
						p.sendMessage(ChatColor.YELLOW + plugin.getLangConfig("PlayerJoin").replace("%player%", event.getPlayer().getName()));
				}
			}
			
			event.setJoinMessage(null);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if(plugin.configmain.getString("EnableQuitMessages").equalsIgnoreCase("true"))
		{
			if(plugin.configmain.getString("CustomQuitMessages").equalsIgnoreCase("true"))
			{
				Random rand = new Random();
				
				int line = rand.nextInt(plugin.quitquote.size());
				
				for(Player p : Bukkit.getServer().getOnlinePlayers())
				{
					if(p.canSee(event.getPlayer()))
						p.sendMessage(ChatColor.YELLOW + plugin.addColor(plugin.quitquote.get(line).replace("%player%", event.getPlayer().getDisplayName())));
				}
				
			}
			else
			{
				for(Player p : Bukkit.getServer().getOnlinePlayers())
				{
					if(p.canSee(event.getPlayer()))
						p.sendMessage(ChatColor.YELLOW + plugin.getLangConfig("PlayerQuit").replace("%player%", event.getPlayer().getName()));
				}
			}
			
			event.setQuitMessage(null);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		
		String message = event.getMessage();
		Player player = event.getPlayer();

		if (message.toLowerCase().startsWith("/me ")) {
			
			ExPlayer pSource = plugin.getExPlayer(player);
			if (pSource.isMuted || (plugin.isModerated && !plugin.checkPermissions(player, "PlayerStatus.moderate")))
			{
				player.sendMessage(ChatColor.RED + plugin.getLangConfig("MsgPlayerMuted"));
				event.setCancelled(true);
				return;
			}
			
			String s = message.substring(message.indexOf(" ")).trim();
			
			for (Player t : plugin.getServer().getOnlinePlayers())
			{
				ExPlayer p = plugin.getExPlayer(t);
				if (!p.isNochat && !p.IsIgnoring(player.getName()))
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
				player.sendMessage(plugin.getLangConfig("ErrMsgFormat"));
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
					player.sendMessage(ChatColor.RED + plugin.getLangConfig("ErrNoReply"));
					event.setCancelled(true);
					return;
				}
				
				name = plugin.getExPlayer(player).LastMessenger.getName();

				msg = message;
			}else{
				if (!message.contains(" "))
				{
					player.sendMessage(ChatColor.RED + plugin.getLangConfig("ErrMsgFormat"));
					event.setCancelled(true);
					return;
				}else{
					name = message.substring(0, message.indexOf(" "));
					if (!message.contains(" "))
					{
						player.sendMessage(ChatColor.RED + plugin.getLangConfig("ErrMsgFormat"));
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
					player.sendMessage(ChatColor.RED + plugin.getLangConfig("MsgTooManyPlayerFound"));
					event.setCancelled(true);
					return;
				}else if(recipients.size() == 0)
				{
					player.sendMessage(ChatColor.RED + plugin.getLangConfig("MsgPlayerNotFound").replace("%player%", name));
					event.setCancelled(true);
					return;
				}

				recipient = recipients.get(0);
			}
			
			if (player.getName().equals(recipient.getName()))
			{
				player.sendMessage(ChatColor.RED + plugin.getLangConfig("MsgCantMsgSelf"));
			}else{
								
				if (PlayerStatus.playerlist.containsKey(recipient.getName()))
				{
					ExPlayer p = PlayerStatus.playerlist.get(recipient.getName());
					
					if (p.isDnd)
					{
						player.sendMessage(ChatColor.GRAY + plugin.getLangConfig("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + msg);
						player.sendMessage(ChatColor.RED + plugin.getLangConfig("MsgPlayerIsDND").replace("%player%", recipient.getName()));
					}else if (p.isAfk)
					{
						player.sendMessage(ChatColor.GRAY + plugin.getLangConfig("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + msg);
						player.sendMessage(ChatColor.RED + plugin.getLangConfig("MsgPlayerIsAFK").replace("%player%", recipient.getName()));
					}else if (p.isNomsg)
					{
						player.sendMessage(ChatColor.GRAY + plugin.getLangConfig("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + msg);
						if (!player.isOp())
						{
							player.sendMessage(ChatColor.RED + plugin.getLangConfig("MsgPlayerIsNoMsg").replace("%player%", recipient.getName()));
						}
					}else{
						player.sendMessage(ChatColor.GRAY + plugin.getLangConfig("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + msg);
					}
					
					if (!p.isNomsg || player.isOp())
					{
						if (!p.IsIgnoring(player.getName()))
						{
							recipient.sendMessage(ChatColor.GRAY + plugin.getLangConfig("MsgPrivateFrom").replace("%player%", player.getName()) + " " + msg);
							plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " told " + recipient.getName() + ": " + msg);
							
							plugin.getExPlayer(recipient).LastMessenger = player;
							
						}else{
							plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " was ignored by " + recipient.getName() + ": " + msg);
						}
					}else{
						plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " could not tell to " + recipient.getName() + " because of NoMsg status: " + msg);
					}
					
				}else{
					player.sendMessage(ChatColor.GRAY + plugin.getLangConfig("MsgPrivateTo").replace("%player%", recipient.getName()) + " " + msg);
					recipient.sendMessage(ChatColor.GRAY + plugin.getLangConfig("MsgPrivateFrom").replace("%player%", player.getName()) + " " + msg);
					plugin.getExPlayer(recipient).LastMessenger = player;
					plugin.logger.info("[" + plugin.pdfdescription + "]" + player.getName() + " told " + recipient.getName() + ": " + msg);
				}
			}
			event.setCancelled(true);
		}
	}
	
	
	
}
