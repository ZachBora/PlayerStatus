package com.worldcretornica.playerstatus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PSEntityListener implements Listener {
	
	
	public PSEntityListener(PlayerStatus instance) {
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(final EntityDeathEvent event) {
		if (event instanceof PlayerDeathEvent) {
            PlayerDeathEvent e = (PlayerDeathEvent) event;
            e.setDeathMessage(null);
            e.setDroppedExp(0);
        }
	}
	
	
	/*
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		ExPlayer ep;
		
		if(event.getEntity() instanceof Player)
		{
			ep = plugin.getExPlayer((Player) event.getEntity());
			
			
			if(ep.isAfk && plugin.configmain.getBoolean("AFKGod", false))
			{
				event.setDamage(0);
			}
		}
		
		super.onEntityDamage(event);
	}*/
}
