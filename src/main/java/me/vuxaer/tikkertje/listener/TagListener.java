package me.vuxaer.tikkertje.listener;

import me.vuxaer.tikkertje.manager.GameManager;
import me.vuxaer.tikkertje.util.GameState;
import me.vuxaer.tikkertje.util.PlayerRole;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;

public class TagListener implements Listener {

    private final GameManager gameManager;
    private final Map<Player, Long> cooldown = new HashMap<>();

    public TagListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onTag(EntityDamageByEntityEvent e) {

        if (!(e.getDamager() instanceof Player damager)) return;
        if (!(e.getEntity() instanceof Player target)) return;

        if (gameManager.getState() != GameState.RUNNING) return;

        e.setCancelled(true);

        if (damager.getGameMode() == GameMode.SPECTATOR) return;
        if (target.getGameMode() == GameMode.SPECTATOR) return;

        if (!gameManager.isTikker(damager)) return;

        if (damager.equals(target)) return;

        if (gameManager.getRole(target) != PlayerRole.OVERLEVER) return;

        long now = System.currentTimeMillis();

        if (cooldown.containsKey(damager)) {
            if (now - cooldown.get(damager) < 1000) return;
        }

        cooldown.put(damager, now);

        gameManager.switchTikker(target);
    }
}