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

    public void updateAll(int timeLeft, int round) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard board = boards.computeIfAbsent(player, p -> {
                Scoreboard b = Bukkit.getScoreboardManager().getNewScoreboard();
                Objective obj = b.registerNewObjective("tikkertje", "dummy", "§6§lTikkertje");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                p.setScoreboard(b);
                return b;
            });

            Objective obj = board.getObjective("tikkertje");
            if (obj == null) continue;
            for (String entry : new HashSet<>(board.getEntries())) {
                board.resetScores(entry);
            }

            int score = 10;
            obj.getScore("§7Ronde: §e" + round).setScore(score--);
            obj.getScore("§7Tijd: §e" + timeLeft + "s").setScore(score--);
            obj.getScore(" ").setScore(score--);

            PlayerRole role = gameManager.getRole(player);
            String roleText = switch (role) {
                case TIKKER -> "§cTikker";
                case OVERLEVER -> "§aOverlever";
                case SPECTATOR -> "§7Spectator";
            };
            obj.getScore("§7Rol: " + roleText).setScore(score--);
            Player tikker = Bukkit.getOnlinePlayers().stream()
                    .filter(gameManager::isTikker)
                    .findFirst().orElse(null);

            if (tikker != null && !gameManager.isTikker(player)) {
                obj.getScore("§7Tikker: §c" + tikker.getName()).setScore(score--);
            }
            obj.getScore("  ").setScore(score--);
            obj.getScore("§7Spelers: §a" + gameManager.getAlivePlayers()).setScore(score--);
        }
    }

    public void clearAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        boards.clear();
    }
}