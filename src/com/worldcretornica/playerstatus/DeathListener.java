package com.worldcretornica.playerstatus;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener extends EntityListener {
	
	public static PlayerStatus plugin;
	
	public DeathListener(PlayerStatus instance) {
		plugin = instance;
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		if (event instanceof PlayerDeathEvent) {
            PlayerDeathEvent e = (PlayerDeathEvent) event;
            e.setDeathMessage(null);
            e.setDroppedExp(0);
        }
	}
}
