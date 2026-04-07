package me.vuxaer.tikkertje.manager;

import me.vuxaer.tikkertje.util.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TikkertjeScoreboard {

    private final GameManager gameManager;
    private final Map<Player, Scoreboard> boards = new HashMap<>();

    public TikkertjeScoreboard(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void updateAll(int timeLeft) {
        for (Player player : Bukkit.getOnlinePlayers()) {

            Scoreboard board = boards.computeIfAbsent(player, p -> {
                Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();
                Objective obj = b.registerNewObjective(
                        "tikkertje",
                        "dummy",
                        "§6§lSprookjesCraft"
                );
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                p.setScoreboard(b);
                return b;
            });

            Objective obj = board.getObjective("tikkertje");
            if (obj == null) continue;
            for (String entry : new HashSet<>(board.getEntries())) {
                board.resetScores(entry);
            }

            int score = 12;
            obj.getScore("§7Tikkertje").setScore(score--);
            obj.getScore("§8§m────────────").setScore(score--);
            PlayerRole role = gameManager.getRole(player);
            String roleText = switch (role) {
                case TIKKER -> "§cTikker";
                case OVERLEVER -> "§aOverlever";
                case SPECTATOR -> "§7Spectator";
            };

            obj.getScore("§7Rol: " + roleText).setScore(score--);
            obj.getScore("§7Resterende tijd: §e" + timeLeft + "s").setScore(score--);
            obj.getScore(" ").setScore(score--);

            Player tikker = Bukkit.getOnlinePlayers().stream()
                    .filter(gameManager::isTikker)
                    .findFirst().orElse(null);

            if (tikker != null) {
                obj.getScore("§7Tikker: §c" + tikker.getName()).setScore(score--);
            }

            obj.getScore("  ").setScore(score--);
            obj.getScore("§7Overlevenden: §a" + gameManager.getAlivePlayers()).setScore(score--);
            obj.getScore("§8").setScore(score--);
            obj.getScore("§8play.sprookjescraft.nl").setScore(score--);
        }
    }

    public void clearAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        boards.clear();
    }
}