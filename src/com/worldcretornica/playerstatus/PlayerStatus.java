package com.worldcretornica.playerstatus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerStatus extends JavaPlugin {

	public final PSPlayerListener chatlistener = new PSPlayerListener(this);
	public final PSEntityListener deathlistener = new PSEntityListener(this);
	public final Logger logger = Logger.getLogger("Minecraft");
	public static final ConcurrentHashMap<String,ExPlayer> playerlist = new ConcurrentHashMap<String, ExPlayer>();
	
	public YamlConfiguration configlanguage;
	public YamlConfiguration configmain;
	
	public ArrayList<String> loginquote = new ArrayList<String>();
	public ArrayList<String> quitquote = new ArrayList<String>();
	
	public String pdfdescription;
	private String pdfversion;
	     
    public boolean isModerated = false;
    
    public Plugin censorit = null;
	
	@Override
	public void onDisable()
	{
		playerlist.clear();
		this.logger.info(pdfdescription + " disabled.");
	}

	@Override
	public void onEnable()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.chatlistener, this);
		pm.registerEvents(this.deathlistener, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		pdfdescription = pdfFile.getName();
		pdfversion = pdfFile.getVersion();

		checkConfig();
		
		if(this.getServer().getPluginManager().isPluginEnabled("CensorIt"))
		{
			censorit = this.getServer().getPluginManager().getPlugin("CensorIt");
		}
		
		this.logger.info(pdfdescription + " version " + pdfversion + " is enabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args)
	{
		if (commandlabel.equalsIgnoreCase("afk"))
		{
			if (!this.checkPermissions(sender, "PlayerStatus.afk"))
			{				
				sender.sendMessage(ChatColor.RED + "[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			
			if (sender instanceof Player)
				toggleAfk((Player) sender, getExPlayer((Player) sender));
			
			return true;
		}else if (commandlabel.equalsIgnoreCase("dnd")){
			if (!this.checkPermissions(sender, "PlayerStatus.dnd"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			if (sender instanceof Player)
				toggleDnd((Player) sender, getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("nochat")){
			if (!this.checkPermissions(sender, "PlayerStatus.nochat"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			if (sender instanceof Player)
				toggleNochat((Player) sender, getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("nomsg")){
			if (!this.checkPermissions(sender, "PlayerStatus.nomessage"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			if (sender instanceof Player)
				toggleNomsg((Player) sender, getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("ignore") && args.length == 0){
			if (!this.checkPermissions(sender, "PlayerStatus.ignore"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			if (sender instanceof Player)
				sender.sendMessage(ChatColor.RED + getLangConfig("MsgIgnoreSyntax"));
			return true;
		}else if (commandlabel.equalsIgnoreCase("ignore") && args.length == 1){
			if (!this.checkPermissions(sender, "PlayerStatus.ignore"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
		
			if (sender instanceof Player)
			{
				Player recipient = getPlayerFromString(sender, args[0].toString());
				
				if (recipient != null)
				{
					toggleIgnorePlayer((Player) sender, getExPlayer((Player) sender), recipient);
				}
			}
			return true;
		}else if (commandlabel.equalsIgnoreCase("ignorelist")){
			if (!this.checkPermissions(sender, "PlayerStatus.ignore"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			if (sender instanceof Player)
				displayIgnoreList((Player) sender, getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("mute")){
			if (!this.checkPermissions(sender, "PlayerStatus.mute"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			
			Player recipient = getPlayerFromString(sender, args[0].toString());
			
			if (recipient != null)
			{
				toggleMutePlayer(sender, getExPlayer((Player) recipient), recipient, true);
			}
			
			return true;
		}else if (commandlabel.equalsIgnoreCase("unmute")){
			if (!this.checkPermissions(sender, "PlayerStatus.mute"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			
			Player recipient = getPlayerFromString(sender, args[0].toString());
			
			if (recipient != null)
			{
				toggleMutePlayer(sender, getExPlayer((Player) recipient), recipient, false);
			}
			return true;
			
		}else if (commandlabel.equalsIgnoreCase("playerstatus") && args.length == 0){
			sender.sendMessage(ChatColor.BLUE + pdfdescription + " v" + pdfversion);
			sender.sendMessage(ChatColor.RED + "/playerstatus " + ChatColor.GREEN + "<name> " + ChatColor.WHITE + getLangConfig("MsgHelpPlayerStatus"));
			if (sender instanceof Player)
			{
				sender.sendMessage(ChatColor.RED + "/afk " + ChatColor.WHITE + getLangConfig("MsgHelpAfk") + " (" + ColoredStatus(isPlayerAfk(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
				sender.sendMessage(ChatColor.RED + "/dnd " + ChatColor.WHITE + getLangConfig("MsgHelpDnd") + " (" + ColoredStatus(isPlayerDnd(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
				sender.sendMessage(ChatColor.RED + "/nomsg " + ChatColor.WHITE + getLangConfig("MsgHelpNomsg") + " (" + ColoredStatus(isPlayerNoMsg(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
				sender.sendMessage(ChatColor.RED + "/nochat " + ChatColor.WHITE + getLangConfig("MsgHelpNochat") + " (" + ColoredStatus(isPlayerNoChat(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
			}
			sender.sendMessage(ChatColor.RED + "/ignore <playername> " + ChatColor.WHITE + getLangConfig("MsgHelpIgnore"));
			sender.sendMessage(ChatColor.RED + "/ignorelist " + ChatColor.WHITE + getLangConfig("MsgHelpIgnorelist"));
			return true;
		}else if (commandlabel.equalsIgnoreCase("playerstatus") && args != null && args.length > 0 && args[0].equalsIgnoreCase("reload")){
			
			if (!this.checkPermissions(sender, "PlayerStatus.reload"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			
			checkConfig();
			
			sender.sendMessage("PlayerStatus reloaded");
			return true;
			
		}else if (commandlabel.equalsIgnoreCase("playerstatus") && args != null && args.length == 1){
			
			Player recipient = getPlayerFromString(sender, args[0].toString());
			
			if (recipient != null)
			{
				sender.sendMessage(ChatColor.BLUE + getLangConfig("MsgStatusOf") + " " + recipient.getName() + ChatColor.WHITE + " : " + getStatus(getExPlayer(recipient)));
			}
			return true;
		}else if (commandlabel.equalsIgnoreCase("playerstatuslang"))
		{
			if (!this.checkPermissions(sender, "PlayerStatus.config"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			
			if (args == null || args.length != 1)
			{
				sender.sendMessage(ChatColor.RED + getLangConfig("ErrSpecifyLanguage"));
				return true;
			}
			
			String language = args[0].toString();
			
			LoadLanguage(language, sender);
			
			return true;
		}else if (commandlabel.equalsIgnoreCase("moderate"))
		{
			if (!this.checkPermissions(sender, "PlayerStatus.moderate"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + getLangConfig("MsgPermissionDenied"));
				return true;
			}
			
			if (isModerated)
			{
				isModerated = false;
				Broadcast(ChatColor.RED + getLangConfig("MsgModerationOff"));
			}else{
				isModerated = true;
				Broadcast(ChatColor.RED + getLangConfig("MsgModerationOn1"));
				if(getLangConfig("MsgModerationOn2") != "")
					Broadcast(ChatColor.RED + getLangConfig("MsgModerationOn2"));
			}
			
			return true;
		}else if (commandlabel.equalsIgnoreCase("loginquote") && args.length == 0)
		{
			sender.sendMessage("Usage /loginquote <number>");
			return true;
		}else if (commandlabel.equalsIgnoreCase("loginquote") && args.length == 1)
		{
			try  
		    { 
				int line = Integer.parseInt(args[0]);
				if (sender instanceof Player)
					sender.sendMessage(addColor(loginquote.get(line).replace("%player%", ((Player) sender).getDisplayName())));
				else
					sender.sendMessage(loginquote.get(line));
		    }catch(NumberFormatException nfe)
		    {
		    	sender.sendMessage("Usage /loginquote <number>");
		    }
			return true;
		}else if (commandlabel.equalsIgnoreCase("quitquote") && args.length == 0)
		{
			sender.sendMessage("Usage /quitquote <number>");
			return true;
		}else if (commandlabel.equalsIgnoreCase("quitquote") && args.length == 1)
		{
			try  
		    { 
				int line = Integer.parseInt(args[0]);
				if (sender instanceof Player)
					sender.sendMessage(addColor(loginquote.get(line).replace("%player%", ((Player) sender).getDisplayName())));
				else
					sender.sendMessage(loginquote.get(line));
		    }catch(NumberFormatException nfe)
		    {
		    	sender.sendMessage("Usage /quitquote <number>");
		    }
			return true;
		}else{
			return false;
		}
	}
	
	
	private Player getPlayerFromString(CommandSender sender, String playername)
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
				sender.sendMessage(ChatColor.RED + getLangConfig("MsgTooManyPlayerFound"));
				return null;
			}
			if (players.size() == 0)
			{
				return null;
			}
			player = players.get(0);
		}
		if (player == null)
			sender.sendMessage(ChatColor.RED + getLangConfig("MsgPlayerNotFound").replace("%player%", playername));
		return player;
	}
	
	private void displayIgnoreList(Player player, ExPlayer ep) {
		String ignorelist = "";
		
		if (playerlist.containsKey(player.getName()))
		{
			for(String p : playerlist.get(player.getName()).ignoredplayers)
			{
				ignorelist = ignorelist + p + ", ";
			}
			if (ignorelist.length() > 2)
				ignorelist = ignorelist.substring(0, ignorelist.length() - ", ".length());
		}
		player.sendMessage(getLangConfig("MsgCurrentlyIgnored") + " " + ignorelist);
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
		if(playerlist.containsKey(player.getName()))
			return playerlist.get(player.getName());
		else
		{
			ExPlayer e = new ExPlayer();
			playerlist.putIfAbsent(player.getName(), e);
			return e;
		}
	}
	
	private String ColoredStatus(boolean value)
	{
		if (value)
			return ChatColor.GREEN + getLangConfig("MsgEnabled");
		else
			return ChatColor.RED + getLangConfig("MsgDisabled");
	}
	
	public void toggleNomsg(Player player, ExPlayer ep) {
		if (isPlayerNoMsg(ep))
		{
			ep.isNomsg = false;
			player.sendMessage(getLangConfig("MsgNoMsgFalse"));
		}else{
			ep.isNomsg = true;
			player.sendMessage(getLangConfig("MsgNoMsgTrue"));
		}
	}

	public void toggleNochat(Player player, ExPlayer ep) {
		if (isPlayerNoChat(ep))
		{
			ep.isNochat = false;
			player.sendMessage(getLangConfig("MsgNoChatFalse"));
		}else{
			ep.isNochat = true;
			player.sendMessage(getLangConfig("MsgNoChatTrue"));
		}
	}

	public void toggleDnd(Player player, ExPlayer ep) {
		long t = Calendar.getInstance().getTimeInMillis();
		if (isPlayerDnd(ep))
		{
			if(ep.timeDnded < (t - configmain.getInt("TimeDisableAFKDND",5000)))
			{
				ep.isDnd = false;
				ep.timeUnset = t;
				Broadcast(ChatColor.YELLOW + getLangConfig("MsgNotDnd").replace("%player%", player.getDisplayName()));
			}else{
				player.sendMessage(ChatColor.RED + getLangConfig("ErrDisableDND").replace("%t%", "" + ((ep.timeDnded + configmain.getInt("TimeDisableAFKDND",5000) - t) / 1000 + 1)));
			}	
		}else
		{
			if(ep.timeUnset < (t - configmain.getInt("TimeBetweenAFKDND",5000)))
			{
				ep.isDnd = true;
				ep.timeDnded = t;
				Broadcast(ChatColor.YELLOW + getLangConfig("MsgDnd").replace("%player%", player.getDisplayName()));
			}else{
				player.sendMessage(ChatColor.RED + getLangConfig("ErrEnableDND").replace("%t%", "" + ((ep.timeUnset + configmain.getInt("TimeBetweenAFKDND",5000) - t) / 1000 + 1)));
			}
		}
	}


	
	public void toggleAfk(Player player, ExPlayer ep) {
		long t = Calendar.getInstance().getTimeInMillis();
		
		if (isPlayerAfk(ep))
		{
			ep.isAfk = false;
			ep.timeUnset = t;
			Broadcast(ChatColor.YELLOW + getLangConfig("MsgNotAfk").replace("%player%", player.getDisplayName()));
		}else
		{
			if(ep.timeUnset < (t - configmain.getInt("TimeBetweenAFKDND",30000)))
			{
				ep.isAfk = true;
				ep.timeAfked = t;
				Broadcast(ChatColor.YELLOW + getLangConfig("MsgAfk").replace("%player%", player.getDisplayName()));
			}else{
				player.sendMessage(ChatColor.RED + getLangConfig("ErrEnableAFK").replace("%t%", "" + ((ep.timeUnset + configmain.getInt("TimeBetweenAFKDND",30000) - t) / 1000 + 1)));
			}
		}
	}
	
	public void toggleIgnorePlayer(Player player, ExPlayer ep, Player ignored)
	{
		if (ep.IsIgnoring(ignored.getName()))
		{
			ep.removeIgnoring(ignored.getName());
			player.sendMessage(ChatColor.YELLOW + getLangConfig("MsgNotPlayerIsIgnored").replace("%player%", ignored.getDisplayName()));
		}
		else
		{
			ep.addIgnoring(ignored.getName());
			player.sendMessage(ChatColor.YELLOW + getLangConfig("MsgPlayerIsIgnored").replace("%player%", ignored.getDisplayName()));
		}
	}
	
	public void toggleMutePlayer(CommandSender player, ExPlayer ep, Player muteep, boolean Mute)
	{
		if (!ep.isMuted && Mute)
		{
			ep.isMuted = true;
			Broadcast(ChatColor.YELLOW + getLangConfig("MsgSetPlayerMuted").replace("%player1%", muteep.getDisplayName()).replace("%player2%", player.getName()));
		}
		else if(ep.isMuted && !Mute)
		{
			ep.isMuted = false;
			Broadcast(ChatColor.YELLOW + getLangConfig("MsgSetPlayerUnmuted").replace("%player1%", muteep.getDisplayName()).replace("%player2%", player.getName()));
		}else{
			if(Mute)
			{
				player.sendMessage(getLangConfig("MsgPlayerAlreadyMuted").replace("%player%", muteep.getDisplayName()));
			}else
			{
				player.sendMessage(getLangConfig("MsgPlayerIsNotMuted").replace("%player%", muteep.getDisplayName()));
			}
		}
	}
	
	public boolean isPlayerAfk(ExPlayer player)
	{
		return player.isAfk;
	}
	
	public boolean isPlayerDnd(ExPlayer player)
	{
		return player.isDnd;
	}
	
	public boolean isPlayerNoMsg(ExPlayer player)
	{
		return player.isNomsg;
	}
	
	public boolean isPlayerNoChat(ExPlayer player)
	{
		return player.isNochat;
	}
	
	public boolean isPlayerMuted(ExPlayer player)
	{
		return player.isMuted;
	}
	
	public Boolean checkPermissions(CommandSender sender, String node) {
        // SuperPerms
        if (sender.hasPermission(node) || sender.hasPermission(pdfdescription + ".*") || sender.hasPermission("*")) {
              return true;
        } else if (sender.isOp()) {
            return true;
        }
        return false;
    }
	
	public void Broadcast(String message)
	{
		
		for(Player p: getServer().getOnlinePlayers())
		{			
			if (playerlist.containsKey(p.getName()))
			{
				ExPlayer ep = playerlist.get(p.getName());
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
		properties.put("CustomLoginMessages", "true");
		properties.put("CustomQuitMessages", "true");
		properties.put("EnableLoginMessages", "true");
		properties.put("EnableQuitMessages", "true");
		
		
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
		
		//properties.put("AFKPrefix", "[AFK]");
		properties.put("DNDPrefix","&f[DND]");
		properties.put("PlayerJoin","&e%player% &ejoined the server.");
		properties.put("PlayerQuit","&e%player% &eleft the server.");
		properties.put("MsgPermissionDenied","&cPermissions Denied");
		properties.put("MsgHelpPlayerStatus","&fTo display status of that player.");
		properties.put("MsgHelpAfk","&fTo toggle Away From Keyboard.");
		properties.put("MsgHelpDnd","&fTo toggle Do Not Disturb.");
		properties.put("MsgHelpNomsg","&fTo disable Private Messages.");
		properties.put("MsgHelpNochat","&fTo disable chat.");
		properties.put("MsgHelpIgnore","&fTo ignore someone.");
		properties.put("MsgHelpIgnorelist","&fTo list ignored people. The lists reset upon server restart.");
		properties.put("MsgStatusOf","&9Status of");
		properties.put("MsgTooManyPlayerFound","&cMore than one player found! Use @<name> for exact matching.");
		properties.put("MsgPlayerNotFound","&cPlayer %player% not found!");
		properties.put("MsgCurrentlyIgnored","&fCurrently ignored players :");
		properties.put("MsgEnabled","&aEnabled");
		properties.put("MsgDisabled","&cDisabled");
		properties.put("MsgNoMsgFalse","&fYou are now receiving messages.");
		properties.put("MsgNoMsgTrue","&fYou are no longer receiving messages.");
		properties.put("MsgNoChatTrue","&fYou are no longer seeing chat.");
		properties.put("MsgNoChatFalse","&fYou are now seeing chat.");
		properties.put("MsgNotDnd","&e%player% &eis no longer DND.");
		properties.put("MsgDnd","&e%player% &eis now DND.");
		properties.put("MsgNotAfk","&e%player% &eis no longer AFK.");
		properties.put("MsgAfk","&e%player% &eis now AFK.");
		properties.put("MsgNotPlayerIsIgnored","&e%player% &eis no longer being ignored.");
		properties.put("MsgPlayerIsIgnored","&e%player% &eis being ignored.");
		properties.put("MsgSetPlayerMuted","&e%player1% &ewas muted by %player2%&e.");
		properties.put("MsgSetPlayerUnmuted","&e%player1% &ewas unmuted by %player2%&e.");
		properties.put("MsgPlayerAlreadyMuted","%player% &eis already muted. Use /unmute to unmute.");
		properties.put("MsgPlayerIsNotMuted","%player% &eis not muted.");
		properties.put("MsgPlayerMuted","&cYou cannot talk you are muted.");
		properties.put("MsgCantMsgSelf","&cCan't message yourself.");
		properties.put("MsgPrivateTo","&7(To %player%&7):&f");
		properties.put("MsgPlayerIsDND","&ePlayer %player% &eis DND and might not receive your message!");
		properties.put("MsgPlayerIsAFK","&ePlayer %player% &eis AFK and might not receive your message!");
		properties.put("MsgPlayerIsNoMsg","&ePlayer %player% &eis blocking all incoming messages!");
		properties.put("MsgPrivateFrom","&7(From %player%&7):&f");
		properties.put("MsgModerationOff","&cThe Chat is no longer moderated !");
		properties.put("MsgModerationOn1", "&c!!ATTENTION The Chat is now moderated!!");
		properties.put("MsgModerationOn2", "&cOnly allowed people can speak");
		properties.put("MsgIgnoreSyntax", "&cSyntax : /ignore <playername>");
		properties.put("ErrMsgFormat","&cToo few arguments. /msg <target> <message...>");
		properties.put("ErrSpecifyLanguage", "&cToo few arguments. /playerstatuslang <language>");
		properties.put("ErrDisableDND", "&cYou cannot disable DND this soon. Please wait &f%t% &cseconds.");
		properties.put("ErrEnableDND", "&cYou cannot go DND this soon. Please wait &f%t% &cseconds.");
		properties.put("ErrEnableAFK", "&cYou cannot go AFK this soon. Please wait &f%t% &cseconds.");
		properties.put("ErrNoReply", "&cNoone to reply to!");
		
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
		
		File filequit = new File(this.getDataFolder(), "quit-english.txt");
		if(!filequit.exists())
		{
			CreateQuitFile(filequit);
		}else if (configmain.getString("Language","english") == "english")
		{
			LoadQuote(filequit, quitquote);
		}
		
		if (configmain.getString("Language","english") != "english")
		{
			filequit = new File(this.getDataFolder(), "quit-" + configmain.getString("Language","english") + ".txt");
			if(!filequit.exists())
			{
				CreateQuitFile(filequit);
			}else{
				LoadQuote(filequit, quitquote);
			}
		}
				
		File filelogin = new File(this.getDataFolder(), "login-english.txt");
		if(!filelogin.exists())
		{
			CreateLoginFile(filelogin);
		}else if (configmain.getString("Language","english") == "english")
		{
			LoadQuote(filelogin, loginquote);
		}
		
		if (configmain.getString("Language","english") != "english")
		{
			filelogin = new File(this.getDataFolder(), "login-" + configmain.getString("Language","english") + ".txt");
			if(!filelogin.exists())
			{
				CreateLoginFile(filelogin);
			}else{
				LoadQuote(filelogin, loginquote);
			}
		}
		
	}
	
	private void LoadQuote(File file, ArrayList<String> quote) {
		FileReader reader = null;
		BufferedReader br = null;
		
		try{
			reader = new FileReader(file);
			br = new BufferedReader(reader);
			
			quote.clear();
			
			String line;
		    while((line = br.readLine()) != null)
		    	quote.add(line);
		    br.close();
		    reader.close();
			
		}catch (IOException e){
			logger.severe("[" + pdfdescription + "] Unable to read quote config file!");
			logger.severe(e.getMessage());
		} finally {     
			if (br != null) try{
				br.close();
			} catch (IOException e2) {}
			if (reader != null) try {
				reader.close();
			} catch (IOException e2) {}
		}
	}

	private void CreateQuitFile(File file) {
		FileWriter writer = null;
		
		try{
			File dir = new File(this.getDataFolder(), "");
			dir.mkdirs();
			
			writer = new FileWriter(file);
			
			quitquote.clear();
			
			quitquote.add("&e%player% &ehit ALT+F4");
			quitquote.add("&e%player% &etried to divide by 0");
			quitquote.add("&e%player% &eleft, we can talk behind his back now");
			quitquote.add("&e%player% &eprematurely departed");
			quitquote.add("&e%player% &efell down a bottomless pit");
			quitquote.add("&e%player% &ewarped to another dimension");
			quitquote.add("&e%player% &ehad cake waiting");
			quitquote.add("&e%player% &evanished in thin air");
			quitquote.add("&e%player% &echose not to be");
			quitquote.add("&e%player% &esays \"GG\"");
			quitquote.add("&e%player% &edidn't survive the zergling rush");
			quitquote.add("&e%player% &efound something better to do");
			quitquote.add("&e%player% &estopped believing in the god of cubes");
			quitquote.add("&e%player% &estumbled on a round block and couldn't compute");
			quitquote.add("&e%player% &elost his happy thought");
			quitquote.add("&eNo, %player%&e. I expect you to die");
			quitquote.add("&eIf %player% &eis not back in five minutes… wait longer!");
			quitquote.add("&eThere was an un expected error with %player%");
			quitquote.add("&e%player% &ehas entered orbit");
			quitquote.add("&e%player% &ewill be back after these messages");
			quitquote.add("&e%player% &eis not always right");
			quitquote.add("&eThe doctors say %player% &ehas a 50-50 chance of surviving, but there’s only a ten percent chance of that.");
			quitquote.add("&eUnable to download %player%");
			quitquote.add("&e%player% &esuccessfully unloaded");
			quitquote.add("&e%player% &ecore dumped");
			quitquote.add("&e%player% &ehas experienced a 404 error");
			quitquote.add("&e%player% &ereceived the blue screen of death");
			quitquote.add("&e%player% &ewas given item #0");
			quitquote.add("&eHasta la vista, %player%");
			quitquote.add("&e%player% &ehas been slimed");
			quitquote.add("&e%player% &edanced with the Devil in the pale moonlight");
			quitquote.add("&e%player% &ecan't handle the truth!");
			quitquote.add("&eDon't %player%&e! I have the high ground!");
			quitquote.add("&e%player% &ewill be back");
			quitquote.add("&e%player%&e, please make sure you wash behind your ears after leaving");
			quitquote.add("&e%player%&e. %player%&e. Come back!");
						
			for(String str: quitquote)
			{
				writer.write(str + "\n");
			}
			
			writer.close();
			
		}catch (IOException e){
			logger.severe("[" + pdfdescription + "] Unable to create quit config file!");
			logger.severe(e.getMessage());
		} finally {                      
			if (writer != null) try {
				writer.close();
			} catch (IOException e2) {}
		}
	}
	
	private void CreateLoginFile(File file) {
		FileWriter writer = null;
		
		try{
			File dir = new File(this.getDataFolder(), "");
			dir.mkdirs();
			
			writer = new FileWriter(file);
			
			loginquote.clear();
			
			loginquote.add("&eHide your wife, %player%&e just got on!");
			loginquote.add("&eDid someone order a %player%&e?");
			loginquote.add("&ePlease insert %player% &eto proceed");
			loginquote.add("&eWarning, %player% &edetected");
			loginquote.add("&e%player% &ehas arrived");
			loginquote.add("&e%player%&e, you are the winning visitor!");
			loginquote.add("&eNotch entered the server. Nope, just %player%&e!");
			loginquote.add("&eI knew %player% &ewasn't gone forever!");
			loginquote.add("&e%player% &eloves you all!");
			loginquote.add("&e%player%&e, you have entered the door to the north. You are now by yourself, standing in a dark room. The pungent stench of mildew eminates from the wet dungeon walls.");
			loginquote.add("&eI shall call %player% &esquishy and he shall be mine and he shall be my squishy.");
			loginquote.add("&eOh no it's %player% &eagain");
			loginquote.add("&eWe seem to have created a %player%");
			loginquote.add("&e%player%&e! Why did it have to be %player%&e?");
			loginquote.add("&eHi, %player%&e, kill anyone today?");
			loginquote.add("&e%player% &eis bigger than you and higher up the food chain. Get in %player%&e's my belly.");
			loginquote.add("&eSay hello to my little friend %player%");
			loginquote.add("&eFirst rule of the server is... you don't talk about %player%");
			loginquote.add("&eValium, prozac, and %player%&e. Breakfast of champions.");
			loginquote.add("&e*ding* your %player% &eis ready");
			loginquote.add("&eGood Morning, %player%!");
			loginquote.add("&eHello, gorgeous %player%");
			loginquote.add("&e%player%&e, I am your Father!");
			loginquote.add("&e%player% &eknows Kung Fu.");
			loginquote.add("&eAlways let the %player% &ewin");
			loginquote.add("&e%player% &eis queen of the world!");
			loginquote.add("&eBond. %player% &eBond.");
			loginquote.add("&eYou're the disease, and %player% &eis the cure.");
			loginquote.add("&eHere's %player%&e!");
			loginquote.add("&e%player% &ehad me at 'Hello'");
			loginquote.add("&eMay the Force be with %player%");
			loginquote.add("&eA %player% &ea day keeps the zombies at bay");
			loginquote.add("&eHere is your daily %player%");
			loginquote.add("&eElementary, my dear %player%");
			loginquote.add("&eYo, %player%&e!");
			loginquote.add("&eRUN! it's %player%");
			loginquote.add("&eOne, two, %player%&e's coming for you…");
			loginquote.add("&e%player%&e, is that a sword in your pocket or are you just happy to see me?");
			loginquote.add("&e%player%&e, you complete me");
			loginquote.add("&e%player% &ewill always triumph over good because good is dumb");
			loginquote.add("&e%player%&e... very powerful stuff");
			loginquote.add("&eGood Morning, good morning... To %player%&e, and you and youuuuu");
			loginquote.add("&eMy name is %player%&e. And, no, I'm not a licensed digger, but I have been touched by your blocks. And I'm pretty sure I've touched them.");
			
			for(String str: loginquote)
			{
				writer.write(str + "\n");
			}
			
			writer.close();
			
		}catch (IOException e){
			logger.severe("[" + pdfdescription + "] Unable to create login config file!");
			logger.severe(e.getMessage());
		} finally {                      
			if (writer != null) try {
				writer.close();
			} catch (IOException e2) {}
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
	
	public String addColor(String string) {
        return string.replaceAll("(&([a-f0-9]))", "\u00A7$2");
    }
	
	public String getLangConfig(String s)
	{
		return addColor(configlanguage.getString(s));
	}
	
}

