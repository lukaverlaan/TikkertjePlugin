package me.vuxaer.tikkertje.listener;

import me.vuxaer.tikkertje.manager.GameManager;
import me.vuxaer.tikkertje.util.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final GameManager gameManager;

    public QuitListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (gameManager.getState() != GameState.RUNNING) return;

        Player player = e.getPlayer();
        gameManager.eliminatePlayer(player);
    }
}
