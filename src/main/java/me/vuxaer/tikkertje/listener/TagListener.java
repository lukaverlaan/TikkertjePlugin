package me.vuxaer.tikkertje.listener;

import me.vuxaer.tikkertje.manager.GameManager;
import me.vuxaer.tikkertje.util.GameState;
import me.vuxaer.tikkertje.util.PlayerRole;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.md_5.bungee.api.ChatMessageType.ACTION_BAR;

public class TagListener implements Listener {

    private final GameManager gameManager;
    private final Map<Player, Long> cooldown = new HashMap<>();
    private final Map<UUID, Long> noTagCooldown = new HashMap<>();

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
            if (now - cooldown.get(damager) < 500) return;
        }
        if (noTagCooldown.containsKey(damager.getUniqueId())) {
            if (now < noTagCooldown.get(damager.getUniqueId())) {
                return;
            }
        }
        cooldown.put(damager, now);

        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f);
        damager.playSound(damager.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        target.sendTitle("§cJe bezit de vloek!", "§7Ren!", 5, 30, 10);

        long duration = 5000;
        noTagCooldown.put(target.getUniqueId(), now + duration);
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SLOW,
                (int) (duration / 50),
                1,
                false,
                false,
                false
        ));
        startCooldownBar(target, duration);
        gameManager.switchTikker(target);
    }

    private void startCooldownBar(Player player, long durationMs) {
        long endTime = System.currentTimeMillis() + durationMs;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                long remaining = endTime - System.currentTimeMillis();
                if (remaining <= 0) {
                    player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
                    player.spigot().sendMessage(
                            ACTION_BAR,
                            new TextComponent("")
                    );
                    cancel();
                    return;
                }

                int seconds = (int) Math.ceil(remaining / 1000.0);
                String tijd = seconds == 1 ? "seconde" : "seconden";
                player.spigot().sendMessage(
                        ACTION_BAR,
                        new TextComponent("§cJe kunt weer tikken over §e" + seconds + " " + tijd + "§c!")
                );
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.8f);
            }
        }.runTaskTimer(gameManager.getPlugin(), 0, 10);
    }
}