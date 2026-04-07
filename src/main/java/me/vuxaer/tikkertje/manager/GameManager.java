package me.vuxaer.tikkertje.manager;

import me.vuxaer.tikkertje.Tikkertje;
import me.vuxaer.tikkertje.util.GameState;
import me.vuxaer.tikkertje.util.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {

    private final Tikkertje plugin = Tikkertje.getInstance();
    private final Random random = new Random();

    private GameState state = GameState.WAITING;

    private final List<Player> players = new ArrayList<>();
    private final Map<UUID, PlayerRole> roles = new HashMap<>();

    private Player currentTikker;

    private BukkitRunnable timerTask;
    private int timeLeft;

    private final TikkertjeScoreboard scoreboard;

    public GameManager() {
        this.scoreboard = new TikkertjeScoreboard(this);
    }

    public GameState getState() {
        return state;
    }

    public boolean startGame(List<Player> playerList) {
        if (state != GameState.WAITING) return false;
        if (playerList.size() < 2) return false;

        players.clear();
        players.addAll(playerList);
        roles.clear();

        for (Player p : players) {
            p.setGameMode(GameMode.ADVENTURE);
            roles.put(p.getUniqueId(), PlayerRole.OVERLEVER);
        }

        currentTikker = players.get(random.nextInt(players.size()));
        roles.put(currentTikker.getUniqueId(), PlayerRole.TIKKER);
        Bukkit.broadcastMessage("§c" + currentTikker.getName() + " is de Tikker!");

        state = GameState.RUNNING;
        startRoundTimer();

        return true;
    }

    private void startRoundTimer() {
        stopTimer();
        timeLeft = 60;
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state != GameState.RUNNING) {
                    cancel();
                    return;
                }
                scoreboard.updateAll(timeLeft);
                if (timeLeft <= 5 && timeLeft > 0) {
                    Bukkit.broadcastMessage("§c" + timeLeft + "...");
                }

                if (timeLeft <= 0) {
                    eliminateTikker();
                    return;
                }
                timeLeft--;
            }
        };

        timerTask.runTaskTimer(plugin, 0, 20);
    }

    private void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void switchTikker(Player newTikker) {
        roles.put(currentTikker.getUniqueId(), PlayerRole.OVERLEVER);
        currentTikker = newTikker;
        roles.put(newTikker.getUniqueId(), PlayerRole.TIKKER);
        Bukkit.broadcastMessage("§c" + newTikker.getName() + " is nu de Tikker!");
    }

    private void eliminateTikker() {
        Bukkit.broadcastMessage("§7" + currentTikker.getName() + " is geëlimineerd!");
        roles.put(currentTikker.getUniqueId(), PlayerRole.SPECTATOR);
        currentTikker.setGameMode(GameMode.SPECTATOR);
        checkWin();
        if (state == GameState.RUNNING) {
            selectNewTikker();
            startRoundTimer();
        }
    }

    private void selectNewTikker() {
        List<Player> alive = players.stream()
                .filter(p -> roles.getOrDefault(p.getUniqueId(), PlayerRole.SPECTATOR) == PlayerRole.OVERLEVER)
                .toList();

        if (alive.isEmpty()) return;
        currentTikker = alive.get(random.nextInt(alive.size()));
        roles.put(currentTikker.getUniqueId(), PlayerRole.TIKKER);

        Bukkit.broadcastMessage("§c" + currentTikker.getName() + " is de nieuwe Tikker!");
    }

    private void checkWin() {
        List<Player> alive = players.stream()
                .filter(p -> roles.getOrDefault(p.getUniqueId(), PlayerRole.SPECTATOR) != PlayerRole.SPECTATOR)
                .toList();

        if (alive.size() == 1) {
            Player winner = alive.get(0);
            Bukkit.broadcastMessage("§a" + winner.getName() + " heeft gewonnen!");
            state = GameState.ENDING;
            stopTimer();
            Bukkit.getScheduler().runTaskLater(plugin, this::resetGame, 100);
        }
    }

    public void resetGame() {
        stopTimer();
        for (Player p : players) {
            p.setGameMode(GameMode.ADVENTURE);
        }

        roles.clear();
        players.clear();
        scoreboard.clearAll();
        state = GameState.WAITING;
    }

    public boolean isTikker(Player player) {
        return roles.getOrDefault(player.getUniqueId(), PlayerRole.SPECTATOR) == PlayerRole.TIKKER;
    }

    public PlayerRole getRole(Player player) {
        return roles.getOrDefault(player.getUniqueId(), PlayerRole.SPECTATOR);
    }

    public int getAlivePlayers() {
        return (int) players.stream()
                .filter(p -> roles.getOrDefault(p.getUniqueId(), PlayerRole.SPECTATOR) != PlayerRole.SPECTATOR)
                .count();
    }
}