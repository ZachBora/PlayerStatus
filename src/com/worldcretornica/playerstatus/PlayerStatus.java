package com.worldcretornica.playerstatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.server.EntityPlayer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.yaml.snakeyaml.Yaml;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PlayerStatus extends JavaPlugin {

	public final ChatListener chatlistener = new ChatListener(this);
	public final DeathListener deathlistener = new DeathListener(this);
	public final Logger logger = Logger.getLogger("Minecraft");
	public final ArrayList<ExPlayer> playerlist = new ArrayList<ExPlayer>();
	
	public Configuration config;
	
	public String pdfdescription;
	private String pdfversion;
	 
	// Permissions
    public PermissionHandler permissions;
    boolean permissions3;
	
	@Override
	public void onDisable() {
		playerlist.clear();
		this.logger.info(pdfdescription + " disabled.");
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.chatlistener, Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.chatlistener, Event.Priority.Low, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.chatlistener, Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.chatlistener, Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.deathlistener, Event.Priority.Normal, this);
		
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
				sender.sendMessage(ChatColor.RED + "[" + pdfdescription + "] " + config.getString("MsgPermissionDenied"));
				return true;
			}
			toggleAfk(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("dnd")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.dnd"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + config.getString("MsgPermissionDenied"));
				return true;
			}
			toggleDnd(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("nochat")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.nochat"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + config.getString("MsgPermissionDenied"));
				return true;
			}
			toggleNochat(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("nomsg")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.nomessage"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + config.getString("MsgPermissionDenied"));
				return true;
			}
			toggleNomsg(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("ignore") && args.length == 0){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.ignore"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + config.getString("MsgPermissionDenied"));
				return true;
			}
			sender.sendMessage(ChatColor.RED + "Syntax : /ignore <playername>");
			return true;
		}else if (commandlabel.equalsIgnoreCase("ignore") && args.length == 1){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.ignore"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + config.getString("MsgPermissionDenied"));
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
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + config.getString("MsgPermissionDenied"));
				return true;
			}
			displayIgnoreList(getExPlayer((Player) sender));
			return true;
		}else if (commandlabel.equalsIgnoreCase("mute")){
			if (sender instanceof Player && !this.checkPermissions((Player) sender, "PlayerStatus.mute"))
			{
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + config.getString("MsgPermissionDenied"));
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
				sender.sendMessage(ChatColor.RED +"[" + pdfdescription + "] " + config.getString("MsgPermissionDenied"));
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
			sender.sendMessage(ChatColor.RED + "/playerstatus " + ChatColor.GREEN + "<name> " + ChatColor.WHITE + config.getString("MsgHelpPlayerStatus"));
			sender.sendMessage(ChatColor.RED + "/afk " + ChatColor.WHITE + config.getString("MsgHelpAfk") + " (" + ColoredStatus(isPlayerAfk(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
			sender.sendMessage(ChatColor.RED + "/dnd " + ChatColor.WHITE + config.getString("MsgHelpDnd") + " (" + ColoredStatus(isPlayerDnd(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
			sender.sendMessage(ChatColor.RED + "/nomsg " + ChatColor.WHITE + config.getString("MsgHelpNomsg") + " (" + ColoredStatus(isPlayerNoMsg(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
			sender.sendMessage(ChatColor.RED + "/nochat " + ChatColor.WHITE + config.getString("MsgHelpNochat") + " (" + ColoredStatus(isPlayerNoChat(getExPlayer((Player) sender))) + ChatColor.WHITE + ")");
			sender.sendMessage(ChatColor.RED + "/ignore <playername> " + ChatColor.WHITE + config.getString("MsgHelpIgnore"));
			sender.sendMessage(ChatColor.RED + "/ignorelist " + ChatColor.WHITE + config.getString("MsgHelpIgnorelist"));
			return true;
		}else if (commandlabel.equalsIgnoreCase("playerstatus") && args != null && args.length == 1){
			
			Player recipient = getPlayerFromString((Player) sender, args[0].toString());
			
			if (recipient != null)
			{
				sender.sendMessage(ChatColor.BLUE + config.getString("MsgStatusOf") + " " + recipient.getName() + ChatColor.WHITE + " : " + getStatus(getExPlayer(recipient)));
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
				sender.sendMessage(ChatColor.RED + config.getString("MsgTooManyPlayerFound"));
				return null;
			}
			player = players.get(0);
		}
		if (player == null)
			sender.sendMessage(ChatColor.RED + config.getString("MsgPlayerNotFound").replace("%player%", playername));
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
		player.player.sendMessage(config.getString("MsgCurrentlyIgnored") + " " + ignorelist);
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
			return ChatColor.GREEN + config.getString("MsgEnabled");
		else
			return ChatColor.RED + config.getString("MsgDisabled");
	}
	
	private void toggleNomsg(ExPlayer player) {
		if (isPlayerNoMsg(player))
		{
			player.isNomsg = false;
			player.player.sendMessage(config.getString("MsgNoMsgFalse"));
		}else{
			player.isNomsg = true;
			player.player.sendMessage(config.getString("MsgNoMsgTrue"));
		}
	}

	private void toggleNochat(ExPlayer player) {
		if (isPlayerNoChat(player))
		{
			player.isNochat = false;
			player.player.sendMessage(config.getString("MsgNoChatFalse"));
		}else{
			player.isNochat = true;
			player.player.sendMessage(config.getString("MsgNoChatTrue"));
		}
	}

	private void toggleDnd(ExPlayer player) {
		if (isPlayerDnd(player))
		{
			player.isDnd = false;
			Broadcast(ChatColor.YELLOW + config.getString("MsgNotDnd").replace("%player%", player.player.getDisplayName()));
		}else
		{
			player.isDnd = true;
			Broadcast(ChatColor.YELLOW + config.getString("MsgDnd").replace("%player%", player.player.getDisplayName()));
		}
	}

	private void toggleAfk(ExPlayer player) {
		if (isPlayerAfk(player))
		{
			player.isAfk = false;
			Broadcast(ChatColor.YELLOW + config.getString("MsgNotAfk").replace("%player%", player.player.getDisplayName()));
		}else
		{
			player.isAfk = true;
			Broadcast(ChatColor.YELLOW + config.getString("MsgAfk").replace("%player%", player.player.getDisplayName()));
		}
	}
	
	public void toggleIgnorePlayer(ExPlayer sender, Player player)
	{
		if (sender.IsIgnoring(player))
		{
			sender.removeIgnoring(player);
			sender.player.sendMessage(ChatColor.YELLOW + config.getString("MsgNotPlayerIsIgnored").replace("%player%", player.getDisplayName()));
		}
		else
		{
			sender.addIgnoring(player);
			sender.player.sendMessage(ChatColor.YELLOW + config.getString("MsgPlayerIsIgnored").replace("%player%", player.getDisplayName()));
		}
	}
	
	public void toggleMutePlayer(ExPlayer sender, ExPlayer player, boolean Mute)
	{
		if (!player.isMuted && Mute)
		{
			player.isMuted = true;
			Broadcast(ChatColor.YELLOW + 
					config.getString("MsgSetPlayerMuted").replace("%player1%", player.player.getDisplayName()).replace("%player2%", sender.player.getDisplayName()));
		}
		else if(player.isMuted && !Mute)
		{
			player.isMuted = false;
			Broadcast(ChatColor.YELLOW + 
					config.getString("MsgSetPlayerUnmuted").replace("%player1%", player.player.getDisplayName()).replace("%player2%", sender.player.getDisplayName()));
		}else{
			if(Mute)
			{
				sender.player.sendMessage(config.getString("MsgSetPlayerUnmuted").replace("%player%", player.player.getDisplayName()));
			}else
			{
				sender.player.sendMessage(config.getString("MsgPlayerIsNotMuted").replace("%player%", player.player.getDisplayName()));
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
		File file = new File(this.getDataFolder(), "config.yml");
		
		if (!file.exists())
		{
			FileWriter writer = null;
			
			try{
				File dir = new File(this.getDataFolder(), "");
				dir.mkdirs();
				
				writer = new FileWriter(file);
				writer.write("# PlayerStatus configuration\n");
				writer.write("AFKPrefix: '[AFK]'\n");
				writer.write("DNDPrefix: '[DND]'\n");
				writer.write("PlayerJoin: '%player% joined the server.'\n");
				writer.write("PlayerQuit: '%player% left the server.'\n");
				writer.write("\n");
				writer.write("# Message list\n");
				writer.write("MsgPermissionDenied: 'Permissions Denied'\n");
				writer.write("MsgHelpPlayerStatus: 'To display status of that player.'\n");
				writer.write("MsgHelpAfk: 'To toggle Away From Keyboard.'\n");
				writer.write("MsgHelpDnd: 'To toggle Do Not Disturb.'\n");
				writer.write("MsgHelpNomsg: 'To disable Private Messages.'\n");
				writer.write("MsgHelpNochat: 'To disable chat.'\n");
				writer.write("MsgHelpIgnore: 'To ignore someone.'\n");
				writer.write("MsgHelpIgnorelist: 'To list ignored people. The lists reset upon server restart.'\n");
				writer.write("MsgStatusOf: 'Status of'\n");
				writer.write("MsgTooManyPlayerFound: 'More than one player found! Use @<name> for exact matching.'\n");
				writer.write("MsgPlayerNotFound: 'Player %player% not found!'\n");
				writer.write("MsgCurrentlyIgnored: 'Currently ignored players :'\n");
				writer.write("MsgEnabled: 'Enabled'\n");
				writer.write("MsgDisabled: 'Disabled'\n");
				writer.write("MsgNoMsgFalse: 'You are now receiving messages.'\n");
				writer.write("MsgNoMsgTrue: 'You are no longer receiving messages.'\n");
				writer.write("MsgNoChatTrue: 'You are no longer seeing chat.'\n");
				writer.write("MsgNoChatFalse: 'You are now seeing chat.'\n");
				writer.write("MsgNotDnd: '%player% is no longer DND.'\n");
				writer.write("MsgDnd: '%player% is now DND.'\n");
				writer.write("MsgNotAfk: '%player% is no longer AFK.'\n");
				writer.write("MsgAfk: '%player% is now AFK.'\n");
				writer.write("MsgNotPlayerIsIgnored: '%player% is no longer being ignored.'\n");
				writer.write("MsgPlayerIsIgnored: '%player% is being ignored.'\n");
				writer.write("MsgSetPlayerMuted: '%player1% was muted by %player2%.'\n");
				writer.write("MsgSetPlayerUnmuted: '%player1% was unmuted by %player2%.'\n");
				writer.write("MsgPlayerAlreadyMuted: '%player% is already muted. Use /unmute to unmute.'\n");
				writer.write("MsgPlayerIsNotMuted: '%player% is not muted.'\n");
				writer.write("MsgPlayerMuted: 'You cannot talk you are muted.'\n");
				writer.write("MsgCantMsgSelf: 'Can''t message yourself.'\n");
				writer.write("MsgPrivateTo: '(To %player%):'\n");
				writer.write("MsgPlayerIsDND: 'Player %player% is DND and might not receive your message!'\n");
				writer.write("MsgPlayerIsAFK: 'Player %player% is AFK and might not receive your message!'\n");
				writer.write("MsgPlayerIsNoMsg: 'Player %player% is blocking all incoming messages!'\n");
				writer.write("MsgPrivateFrom: '(From %player%):'\n");
				writer.close();
			}catch (IOException e){
				logger.severe("[" + pdfdescription + "] Unable to create config file!");
				logger.severe(e.getMessage());
			}
		}
		
		config = new Configuration(file);
		
		config.load();
	}
}
