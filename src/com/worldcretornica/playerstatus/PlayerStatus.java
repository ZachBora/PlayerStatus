package com.worldcretornica.playerstatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PlayerStatus extends JavaPlugin {

	public final PSPlayerListener chatlistener = new PSPlayerListener(this);
	public final PSEntityListener deathlistener = new PSEntityListener(this);
	public final Logger logger = Logger.getLogger("Minecraft");
	public final ArrayList<ExPlayer> playerlist = new ArrayList<ExPlayer>();
	
	public YamlConfiguration configlanguage;
	public YamlConfiguration configmain;
	
	public String pdfdescription;
	private String pdfversion;
	 
	// Permissions
    public PermissionHandler permissions;
    boolean permissions3;
    
    public boolean isModerated = false; 
	
	@Override
	public void onDisable() {
		playerlist.clear();
		this.logger.info(pdfdescription + " disabled.");
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.chatlistener, Event.Priority.Highest, this);
		//pm.registerEvent(Event.Type.PLAYER_MOVE, this.chatlistener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.chatlistener, Event.Priority.Low, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.chatlistener, Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.chatlistener, Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.deathlistener, Event.Priority.Normal, this);
		//pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.deathlistener, Event.Priority.Normal, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		pdfdescription = pdfFile.getName();
		pdfversion = pdfFile.getVersion();
		
		setupPermissions();
		checkConfig();
		
		this.logger.info(pdfdescription + " version " + pdfversion + " is enabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args)
	{
		if (commandlabel.equalsIgnoreCase("afk"))
		{
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.afk"))
			{
				sender.sendMessage(ChatColor.RED + "[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			toggleAfk(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("dnd")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.dnd"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			toggleDnd(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("nochat")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.nochat"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			toggleNochat(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("nomsg")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.nomessage"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			toggleNomsg(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("ignore") && args.length == 0){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.ignore"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			sender.sendMessage(ChatColor.RED + configlanguage.getString("MsgIgnoreSyntax"));
			return true;
		}else if (commandlabel.equalsIgnoreCase("ignore") && args.length == 1){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.ignore"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			
			Player recipient = getPlayerFromString((Player) sender, args[0].toString());
			
			if (recipient != null)
			{
				toggleIgnorePlayer(getExPlayer((Player) sender), recipient);
			}
			return true;
		}else if (commandlabel.equalsIgnoreCase("ignorelist")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.ignore"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			displayIgnoreList(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("mute")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.mute"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			
			Player recipient = getPlayerFromString((Player) sender, args[0].toString());
			
			if (recipient != null)
			{
				toggleMutePlayer(getExPlayer((Player) sender), getExPlayer(recipient), true);
			}
			return true;
		}else if (commandlabel.equalsIgnoreCase("unmute")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.mute"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			
			Player recipient = getPlayerFromString((Player) sender, args[0].toString());
			
			if (recipient != null)
			{
				toggleMutePlayer(getExPlayer((Player) sender), getExPlayer(recipient), false);
			}
			return true;
		}else if (commandlabel.equalsIgnoreCase("playerstatus") && args.length == 0){
			sender.sendMessage(ChatColor.BLUE + pdfdescription + " v" + pdfversion);
			sender.sendMessage(ChatColor.RED + "/playerstatus " + ChatColor.GREEN + "<name> " + ChatColor.WHITE + configlanguage.getString("MsgHelpPlayerStatus"));
			sender.sendMessage(ChatColor.RED + "/afk " + ChatColor.WHITE + configlanguage.getString("MsgHelpAfk") + " (" + ColoredStatus(isPlayerAfk(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
			sender.sendMessage(ChatColor.RED + "/dnd " + ChatColor.WHITE + configlanguage.getString("MsgHelpDnd") + " (" + ColoredStatus(isPlayerDnd(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
			sender.sendMessage(ChatColor.RED + "/nomsg " + ChatColor.WHITE + configlanguage.getString("MsgHelpNomsg") + " (" + ColoredStatus(isPlayerNoMsg(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
			sender.sendMessage(ChatColor.RED + "/nochat " + ChatColor.WHITE + configlanguage.getString("MsgHelpNochat") + " (" + ColoredStatus(isPlayerNoChat(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
			sender.sendMessage(ChatColor.RED + "/ignore <playername> " + ChatColor.WHITE + configlanguage.getString("MsgHelpIgnore"));
			sender.sendMessage(ChatColor.RED + "/ignorelist " + ChatColor.WHITE + configlanguage.getString("MsgHelpIgnorelist"));
			return true;
		}else if (commandlabel.equalsIgnoreCase("playerstatus") && args != null && args.length == 1){
			
			Player recipient = getPlayerFromString((Player) sender, args[0].toString());
			
			if (recipient != null)
			{
				sender.sendMessage(ChatColor.BLUE + configlanguage.getString("MsgStatusOf") + " " + recipient.getName() + ChatColor.WHITE + " : " + getStatus(getExPlayer(recipient)));
			}
			return true;
		}else if (commandlabel.equalsIgnoreCase("playerstatuslang"))
		{
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.config"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			
			if (args == null || args.length != 1)
			{
				sender.sendMessage(ChatColor.RED + configlanguage.getString("ErrSpecifyLanguage"));
				return true;
			}
			
			String language = args[0].toString();
			
			LoadLanguage(language, sender);
			
			return true;
		}else if (commandlabel.equalsIgnoreCase("moderate"))
		{
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.moderate"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + configlanguage.getString("MsgPermissionDenied"));
				return true;
			}
			
			if (isModerated)
			{
				isModerated = false;
				Broadcast(ChatColor.RED + configlanguage.getString("MsgModerationOff"));
			}else{
				isModerated = true;
				Broadcast(ChatColor.RED + configlanguage.getString("MsgModerationOn1"));
				if(configlanguage.getString("MsgModerationOn2") != "")
					Broadcast(ChatColor.RED + configlanguage.getString("MsgModerationOn2"));
			}
			
			return true;
		}else{
			return false;
		}
	}
	
	
	private Player getPlayerFromString(Player sender, String playername)
	{
		List<Player> players;
		Player player;
		
		if (playername.substring(0, 1) == "@")
		{
			player = this.getServer().getPlayerExact(playername.substring(1));
		}else{
			players = this.getServer().matchPlayer(playername);
			if (players.size() > 1)
			{
				sender.sendMessage(ChatColor.RED + configlanguage.getString("MsgTooManyPlayerFound"));
				return null;
			}
			player = players.get(0);
		}
		if (player == null)
			sender.sendMessage(ChatColor.RED + configlanguage.getString("MsgPlayerNotFound").replace("%player%", playername));
		return player;
	}
	
	private void displayIgnoreList(ExPlayer player) {
		String ignorelist = "";
		
		if (playerlist.contains(player))
		{
			for(Player p : playerlist.get(playerlist.indexOf(player)).ignoredplayers)
			{
				ignorelist = ignorelist + p.getName() + ", ";
			}
			if (ignorelist.length() > 2)
				ignorelist = ignorelist.substring(0, ignorelist.length() - ", ".length());
		}
		player.player.sendMessage(configlanguage.getString("MsgCurrentlyIgnored") + " " + ignorelist);
	}

	private void setupPermissions() {
        if(permissions != null)
            return;
        
        Plugin permTest = this.getServer().getPluginManager().getPlugin("Permissions");
        
        // Check to see if Permissions exists
        if (permTest == null) {
        	logger.info("[" + pdfdescription + "] Permissions not found, using SuperPerms");
        	return;
        }
    	// Check if it's a bridge
    	if (permTest.getDescription().getVersion().startsWith("2.7.7")) {
    		logger.info("[" + pdfdescription + "] Found Permissions Bridge. Using SuperPerms");
    		return;
    	}
    	
    	// We're using Permissions
    	permissions = ((Permissions) permTest).getHandler();
    	// Check for Permissions 3
    	permissions3 = permTest.getDescription().getVersion().startsWith("3");
    	logger.info("[" + pdfdescription + "] Permissions " + permTest.getDescription().getVersion() + " found");
    }

	private String getStatus(ExPlayer player) {
		String msg = "";
		
		if (isPlayerAfk(player)) msg = msg + "AFK, ";
		if (isPlayerDnd(player)) msg = msg + "DND, ";
		if (isPlayerNoMsg(player)) msg = msg + "NOMSG, ";
		if (isPlayerNoChat(player)) msg = msg + "NOCHAT, ";
		
		if (msg == "")
			return "None";
		else
			return msg.substring(0, msg.length()-2);
	}
	
	public ExPlayer getExPlayer(Player player)
	{
		for(ExPlayer ep : this.playerlist)
		{
			if (ep.player.equals(player))
				return ep;
		}
		
		ExPlayer e = new ExPlayer(player);
		playerlist.add(e);
			
		return e;
	}
	
	private String ColoredStatus(boolean value)
	{
		if (value)
			return ChatColor.GREEN + configlanguage.getString("MsgEnabled");
		else
			return ChatColor.RED + configlanguage.getString("MsgDisabled");
	}
	
	public void toggleNomsg(ExPlayer player) {
		if (isPlayerNoMsg(player))
		{
			player.isNomsg = false;
			player.player.sendMessage(configlanguage.getString("MsgNoMsgFalse"));
		}else{
			player.isNomsg = true;
			player.player.sendMessage(configlanguage.getString("MsgNoMsgTrue"));
		}
	}

	public void toggleNochat(ExPlayer player) {
		if (isPlayerNoChat(player))
		{
			player.isNochat = false;
			player.player.sendMessage(configlanguage.getString("MsgNoChatFalse"));
		}else{
			player.isNochat = true;
			player.player.sendMessage(configlanguage.getString("MsgNoChatTrue"));
		}
	}

	public void toggleDnd(ExPlayer player) {
		long t = Calendar.getInstance().getTimeInMillis();
		if (isPlayerDnd(player))
		{
			if(player.timeDnded < (t - configmain.getInt("TimeDisableAFKDND",5000)))
			{
				player.isDnd = false;
				player.timeUnset = t;
				Broadcast(ChatColor.YELLOW + configlanguage.getString("MsgNotDnd").replace("%player%", player.player.getDisplayName()));
			}else{
				player.player.sendMessage(ChatColor.RED + configlanguage.getString("ErrDisableDND").replace("%t%", "" + ((player.timeDnded + configmain.getInt("TimeDisableAFKDND",5000) - t) / 1000 + 1)));
			}	
		}else
		{
			if(player.timeUnset < (t - configmain.getInt("TimeBetweenAFKDND",30000)))
			{
				player.isDnd = true;
				player.timeDnded = t;
				Broadcast(ChatColor.YELLOW + configlanguage.getString("MsgDnd").replace("%player%", player.player.getDisplayName()));
			}else{
				player.player.sendMessage(ChatColor.RED + configlanguage.getString("ErrEnableDND").replace("%t%", "" + ((player.timeUnset + configmain.getInt("TimeBetweenAFKDND",5000) - t) / 1000 + 1)));
			}
		}
	}


	
	public void toggleAfk(ExPlayer player) {
		long t = Calendar.getInstance().getTimeInMillis();
		
		if (isPlayerAfk(player))
		{
			player.isAfk = false;
			player.timeUnset = t;
			Broadcast(ChatColor.YELLOW + configlanguage.getString("MsgNotAfk").replace("%player%", player.player.getDisplayName()));
		}else
		{
			if(player.timeUnset < (t - configmain.getInt("TimeBetweenAFKDND",30000)))
			{
				player.isAfk = true;
				player.timeAfked = t;
				Broadcast(ChatColor.YELLOW + configlanguage.getString("MsgAfk").replace("%player%", player.player.getDisplayName()));
			}else{
				player.player.sendMessage(ChatColor.RED + configlanguage.getString("ErrEnableAFK").replace("%t%", "" + ((player.timeUnset + configmain.getInt("TimeBetweenAFKDND",30000) - t) / 1000 + 1)));
			}
		}
	}
	
	public void toggleIgnorePlayer(ExPlayer sender, Player player)
	{
		if (sender.IsIgnoring(player))
		{
			sender.removeIgnoring(player);
			sender.player.sendMessage(ChatColor.YELLOW + configlanguage.getString("MsgNotPlayerIsIgnored").replace("%player%", player.getDisplayName()));
		}
		else
		{
			sender.addIgnoring(player);
			sender.player.sendMessage(ChatColor.YELLOW + configlanguage.getString("MsgPlayerIsIgnored").replace("%player%", player.getDisplayName()));
		}
	}
	
	public void toggleMutePlayer(ExPlayer sender, ExPlayer player, boolean Mute)
	{
		if (!player.isMuted && Mute)
		{
			player.isMuted = true;
			Broadcast(ChatColor.YELLOW + configlanguage.getString("MsgSetPlayerMuted").replace("%player1%", player.player.getDisplayName()).replace("%player2%", sender.player.getDisplayName()));
		}
		else if(player.isMuted && !Mute)
		{
			player.isMuted = false;
			Broadcast(ChatColor.YELLOW + configlanguage.getString("MsgSetPlayerUnmuted").replace("%player1%", player.player.getDisplayName()).replace("%player2%", sender.player.getDisplayName()));
		}else{
			if(Mute)
			{
				sender.player.sendMessage(configlanguage.getString("MsgPlayerAlreadyMuted").replace("%player%", player.player.getDisplayName()));
			}else
			{
				sender.player.sendMessage(configlanguage.getString("MsgPlayerIsNotMuted").replace("%player%", player.player.getDisplayName()));
			}
		}
	}
	
	public boolean isPlayerAfk(ExPlayer player)
	{
		if (this.playerlist.contains(player))
			return player.isAfk;
		else
			return false;
	}
	
	public boolean isPlayerDnd(ExPlayer player)
	{
		if (this.playerlist.contains(player))
			return player.isDnd;
		else
			return false;
	}
	
	public boolean isPlayerNoMsg(ExPlayer player)
	{
		if (this.playerlist.contains(player))
			return player.isNomsg;
		else
			return false;
	}
	
	public boolean isPlayerNoChat(ExPlayer player)
	{
		if (this.playerlist.contains(player))
			return player.isNochat;
		else
			return false;
	}
	
	public boolean isPlayerMuted(ExPlayer player)
	{
		if(this.playerlist.contains(player))
			return player.isMuted;
		else
			return false;
	}
	
	public Boolean checkPermissions(Player player, String node) {
    	// Permissions
        if (this.permissions != null) {
            if (this.permissions.has(player, node))
                return true;
        // SuperPerms
        } else if (player.hasPermission(node)) {
              return true;
        } else if (player.isOp()) {
            return true;
        }
        return false;
    }
	
	public void Broadcast(String message)
	{
		
		for(Player p: getServer().getOnlinePlayers())
		{
			int i = playerlist.indexOf(getExPlayer(p));
			
			if (i != -1)
			{
				ExPlayer ep = playerlist.get(i);
				if (!ep.isNochat)
				{
					p.sendMessage(message);
				}
			}else{
				p.sendMessage(message);
			}
		}
	}
	
	
	private void checkConfig()
	{
		//Create Main configuration file
		File file = new File(this.getDataFolder(), "config.yml");
		
		TreeMap<String, String> properties = new TreeMap<String, String>();
		
		properties.put("Language", "english");
		//properties.put("AFKGod", "true");
		properties.put("TimeBetweenAFKDND", "30000");
		properties.put("TimeDisableDND", "5000");
		
		CreateConfig(file, properties, "PlayerStatus configuration");
		
		configmain = new YamlConfiguration();
		try{
			configmain.load(file);
		} catch (FileNotFoundException e) {
			logger.severe("[" + pdfdescription + "] File not found: " + e.getMessage());
		} catch (IOException e) {
			logger.severe("[" + pdfdescription + "] IO Error: " + e.getMessage());
		} catch (InvalidConfigurationException e) {
			logger.severe("[" + pdfdescription + "] Invalid configuration: " + e.getMessage());
		} catch (NullPointerException e){
			logger.severe("[" + pdfdescription + "] Null pointer: ");
		}
				
		File filelang = new File(this.getDataFolder(), "caption-english.yml");
		
		properties = new TreeMap<String, String>();
		
		properties.put("AFKPrefix", "[AFK]");
		properties.put("DNDPrefix","[DND]");
		properties.put("PlayerJoin","%player% joined the server.");
		properties.put("PlayerQuit","%player% left the server.");
		properties.put("MsgPermissionDenied","Permissions Denied");
		properties.put("MsgHelpPlayerStatus","To display status of that player.");
		properties.put("MsgHelpAfk","To toggle Away From Keyboard.");
		properties.put("MsgHelpDnd","To toggle Do Not Disturb.");
		properties.put("MsgHelpNomsg","To disable Private Messages.");
		properties.put("MsgHelpNochat","To disable chat.");
		properties.put("MsgHelpIgnore","To ignore someone.");
		properties.put("MsgHelpIgnorelist","To list ignored people. The lists reset upon server restart.");
		properties.put("MsgStatusOf","Status of");
		properties.put("MsgTooManyPlayerFound","More than one player found! Use @<name> for exact matching.");
		properties.put("MsgPlayerNotFound","Player %player% not found!");
		properties.put("MsgCurrentlyIgnored","Currently ignored players :");
		properties.put("MsgEnabled","Enabled");
		properties.put("MsgDisabled","Disabled");
		properties.put("MsgNoMsgFalse","You are now receiving messages.");
		properties.put("MsgNoMsgTrue","You are no longer receiving messages.");
		properties.put("MsgNoChatTrue","You are no longer seeing chat.");
		properties.put("MsgNoChatFalse","You are now seeing chat.");
		properties.put("MsgNotDnd","%player% is no longer DND.");
		properties.put("MsgDnd","%player% is now DND.");
		properties.put("MsgNotAfk","%player% is no longer AFK.");
		properties.put("MsgAfk","%player% is now AFK.");
		properties.put("MsgNotPlayerIsIgnored","%player% is no longer being ignored.");
		properties.put("MsgPlayerIsIgnored","%player% is being ignored.");
		properties.put("MsgSetPlayerMuted","%player1% was muted by %player2%.");
		properties.put("MsgSetPlayerUnmuted","%player1% was unmuted by %player2%.");
		properties.put("MsgPlayerAlreadyMuted","%player% is already muted. Use /unmute to unmute.");
		properties.put("MsgPlayerIsNotMuted","%player% is not muted.");
		properties.put("MsgPlayerMuted","You cannot talk you are muted.");
		properties.put("MsgCantMsgSelf","Can't message yourself.");
		properties.put("MsgPrivateTo","(To %player%):");
		properties.put("MsgPlayerIsDND","Player %player% is DND and might not receive your message!");
		properties.put("MsgPlayerIsAFK","Player %player% is AFK and might not receive your message!");
		properties.put("MsgPlayerIsNoMsg","Player %player% is blocking all incoming messages!");
		properties.put("MsgPrivateFrom","(From %player%):");
		properties.put("MsgModerationOff","The Chat is no longer moderated !");
		properties.put("MsgModerationOn1", "!!ATTENTION The Chat is now moderated!!");
		properties.put("MsgModerationOn2", "Only allowed people can speak");
		properties.put("MsgIgnoreSyntax", "Syntax : /ignore <playername>");
		properties.put("ErrMsgFormat","Too few arguments. /msg <target> <message...>");
		properties.put("ErrSpecifyLanguage", "Too few arguments. /playerstatuslang <language>");
		properties.put("ErrDisableDND", "You cannot disable DND this soon. Please wait %t% seconds.");
		properties.put("ErrEnableDND", "You cannot go DND this soon. Please wait %t% seconds.");
		properties.put("ErrEnableAFK", "You cannot go AFK this soon. Please wait %t% seconds.");
		properties.put("ErrNoReply", "Noone to reply to!");
		
		CreateConfig(filelang, properties, "PlayerStatus Caption configuration");
		
		if (configmain.getString("Language","english") != "english")
		{
			filelang = new File(this.getDataFolder(), "caption-" + configmain.getString("Language","english") + ".yml");
			CreateConfig(filelang, properties, "PlayerStatus Caption configuration");
		}
		
		configlanguage = new YamlConfiguration();
		try {
			configlanguage.load(filelang);
		} catch (FileNotFoundException e) {
			logger.severe("[" + pdfdescription + "] File not found: " + e.getMessage());
		} catch (IOException e) {
			logger.severe("[" + pdfdescription + "] IO Error: " + e.getMessage());
		} catch (InvalidConfigurationException e) {
			logger.severe("[" + pdfdescription + "] Invalid configuration: " + e.getMessage());
		}
		
	}
	
	private void LoadLanguage(String lang, CommandSender sender)
	{
		File file = new File(this.getDataFolder(), "caption-" + lang + ".yml");
		if (!file.exists())
		{
			sender.sendMessage(ChatColor.RED + "Language '" + lang + "' does not exist!");
			return;
		}
		
		configlanguage = new YamlConfiguration();
		try{
			configlanguage.load(file);
		} catch (FileNotFoundException e) {
			logger.severe("[" + pdfdescription + "] File not found: " + e.getMessage());
		} catch (IOException e) {
			logger.severe("[" + pdfdescription + "] IO Error: " + e.getMessage());
		} catch (InvalidConfigurationException e) {
			logger.severe("[" + pdfdescription + "] Invalid configuration: " + e.getMessage());
		}
		
		configmain.set("Language", lang);
		try{
			configmain.save("config.yml");
		} catch (FileNotFoundException e) {
			logger.severe("[" + pdfdescription + "] File not found: " + e.getMessage());
		} catch (IOException e) {
			logger.severe("[" + pdfdescription + "] IO Error: " + e.getMessage());
		}
		
		sender.sendMessage("Language '" + lang + "' loaded successfully!");
		
		return;
	}
	
	private void CreateConfig(File file, TreeMap<String, String> properties, String Title)
	{
		if (!file.exists())
		{
			FileWriter writer = null;
			
			try{
				File dir = new File(this.getDataFolder(), "");
				dir.mkdirs();
				
				writer = new FileWriter(file);
				writer.write("# " + Title + "\n");
				
				for(Entry<String, String> e : properties.entrySet())
				{
					writer.write(e.getKey() + ": '" + e.getValue().replace("'", "''") + "'\n");
				}
				
				writer.close();
				
			}catch (IOException e){
				logger.severe("[" + pdfdescription + "] Unable to create config file : " + Title + "!");
				logger.severe(e.getMessage());
			} finally {                      
				if (writer != null) try {
					writer.close();
				} catch (IOException e2) {}
			}
		}else{
			
			YamlConfiguration myconfig = new YamlConfiguration();
			
			BufferedWriter bwriter = null;
			try{
				myconfig.load(file);
				
				bwriter = new BufferedWriter(new FileWriter(file, true));
				
				for(Entry<String, String> e : properties.entrySet())
				{
					if (myconfig.getString(e.getKey()) == null)
						bwriter.write(e.getKey() + ": '" + e.getValue().replace("'", "''") + "'\n");
				}
				
				bwriter.close();
			} catch (FileNotFoundException e) {
				logger.severe("[" + pdfdescription + "] File not found: " + e.getMessage());
			}catch (IOException e){
				logger.severe("[" + pdfdescription + "] Unable to modify config file " + Title + ": " + e.getMessage());
			}catch (InvalidConfigurationException e){
				logger.severe("[" + pdfdescription + "] Unable to modify config file " + Title + ": " + e.getMessage());
			} finally {                       
				if (bwriter != null) try {
					bwriter.close();
				} catch (IOException e2) {}
			}
		}
	}
	
}
