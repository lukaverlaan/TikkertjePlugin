package me.vuxaer.tikkertje.listener;

import me.vuxaer.tikkertje.manager.GameManager;
import me.vuxaer.tikkertje.util.GameState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionListener implements Listener {

    private final GameManager gameManager;
    private final Map<UUID, Long> leaveTimers = new HashMap<>();

    private final long MAX_TIME = 5000; // 5 sec

    public RegionListener(GameManager gameManager) {
        this.gameManager = gameManager;
        new BukkitRunnable() {
            @Override
            public void run() {
                tickPlayers();
            }
        }.runTaskTimer(gameManager.getPlugin(), 0, 10);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (gameManager.getState() != GameState.RUNNING) return;
        if (gameManager.isFreezePhase() && gameManager.isTikker(player)) {
            e.setTo(e.getFrom());
            return;
        }
        if (!gameManager.hasRegion()) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (e.getFrom().distanceSquared(e.getTo()) < 0.01) return;

        boolean inside = gameManager.isInRegion(player);
        if (!inside) {
            leaveTimers.putIfAbsent(player.getUniqueId(), System.currentTimeMillis());
        } else {
            leaveTimers.remove(player.getUniqueId());
        }
    }

    private void tickPlayers() {
        if (gameManager.getState() != GameState.RUNNING) return;
        long now = System.currentTimeMillis();
        for (UUID uuid : new HashMap<>(leaveTimers).keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                leaveTimers.remove(uuid);
                continue;
            }
            if (player.getGameMode() == GameMode.SPECTATOR) {
                leaveTimers.remove(uuid);
                continue;
            }

            long start = leaveTimers.get(uuid);
            long timeLeft = MAX_TIME - (now - start);
            if (timeLeft <= 0) {
                gameManager.removeFromGame(player);
                leaveTimers.remove(uuid);
                continue;
            }

            int seconds = (int) Math.ceil(timeLeft / 1000.0);
            player.sendTitle(
                    "§c§lKEER TERUG!",
                    "§eJe hebt nog " + seconds + (seconds == 1 ? " seconde" : " seconden"),
                    0, 10, 5
            );
            if (seconds > 3) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.2f);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.8f);
            }
        }
    }
}