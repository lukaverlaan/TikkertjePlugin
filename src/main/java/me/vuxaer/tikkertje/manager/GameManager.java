package me.vuxaer.tikkertje.manager;

import me.vuxaer.tikkertje.Tikkertje;
import me.vuxaer.tikkertje.util.GameState;
import me.vuxaer.tikkertje.util.PlayerRole;
import me.vuxaer.tikkertje.util.WorldGuardHook;
import org.bukkit.*;
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

    private final int ROUND_TIME = 60;
    private final int BETWEEN_TIME = 5;
    private int round = 0;

    private final TikkertjeScoreboard scoreboard;

    private String regionName = null;

    public GameManager() {
        this.scoreboard = new TikkertjeScoreboard(this);
    }

    public GameState getState() {
        return state;
    }

    public boolean startGame(List<Player> playerList) {
        if (state != GameState.WAITING) return false;
        if (playerList.size() < 2) return false;

        if (!hasAllSpawns()) {
            Bukkit.broadcastMessage("§cOntbrekende spawns: §e" + getMissingSpawns());
            return false;
        }

        players.clear();
        players.addAll(playerList);
        roles.clear();

        var spawn = plugin.getSpawnManager().getSpawn("game");
        for (Player p : players) {
            if (spawn != null) p.teleport(spawn);

            p.setGameMode(GameMode.ADVENTURE);
            p.setGlowing(false);
            roles.put(p.getUniqueId(), PlayerRole.OVERLEVER);
        }
        state = GameState.RUNNING;
        startNewRound();
        return true;
    }

    private void startNewRound() {
        if (getAlivePlayers() <= 1) {
            endGame();
            return;
        }

        round++;

        Bukkit.broadcastMessage("§6Nieuwe ronde gestart!");
        Player newTikker = getRandomAlivePlayer();
        setTikker(newTikker);
        startRoundTimer();
    }

    private void setTikker(Player player) {
        if (currentTikker != null) {
            currentTikker.setGlowing(false);
            currentTikker.removePotionEffect(org.bukkit.potion.PotionEffectType.SPEED);

            roles.put(currentTikker.getUniqueId(), PlayerRole.OVERLEVER);
        }

        currentTikker = player;
        roles.put(player.getUniqueId(), PlayerRole.TIKKER);

        player.setGlowing(true);
        player.sendTitle("§c§lJIJ BENT DE TIKKER!", "§7Zorg dat je snel iemand anders tikt!", 5, 50, 10);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
        Bukkit.broadcastMessage("§6⚡ §e" + player.getName() + " is nu de Tikker!");
    }

    private void startRoundTimer() {
        stopTimer();
        timeLeft = ROUND_TIME;
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state != GameState.RUNNING) {
                    cancel();
                    return;
                }

                scoreboard.updateAll(timeLeft, round);
                if (timeLeft == 60 || timeLeft == 45 || timeLeft == 30 || timeLeft == 15 || timeLeft == 10) {
                    Bukkit.broadcastMessage("§eNog §6" + timeLeft + " §eseconden!");
                }

                if (timeLeft <= 5 && timeLeft > 0) {

                    Bukkit.broadcastMessage("§c" + timeLeft + "...");

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle("§c§l" + timeLeft, "§7Tikker wordt bijna geëlimineerd!", 0, 20, 0);
                        float pitch = 1f + (5 - timeLeft) * 0.2f;
                        p.playSound(
                                p.getLocation(),
                                Sound.BLOCK_NOTE_BLOCK_HAT,
                                1f,
                                pitch
                        );
                    }
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

    private void eliminateTikker() {
        Player eliminated = currentTikker;
        Bukkit.broadcastMessage("§7" + eliminated.getName() + " is geëlimineerd!");

        roles.put(eliminated.getUniqueId(), PlayerRole.SPECTATOR);
        eliminated.setGameMode(GameMode.SPECTATOR);
        eliminated.setGlowing(false);

        currentTikker = null;
        checkWin();

        if (state == GameState.RUNNING) {
            startCooldown();
        }
    }

    private void startCooldown() {
        stopTimer();
        new BukkitRunnable() {
            int time = BETWEEN_TIME;
            @Override
            public void run() {
                if (state != GameState.RUNNING) {
                    cancel();
                    return;
                }
                scoreboard.updateAll(time, round);
                if (time <= 0) {
                    cancel();
                    startNewRound();
                    return;
                }
                if (time <= 3) {
                    Bukkit.broadcastMessage("§eNieuwe ronde in " + time + "...");
                }
                time--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void switchTikker(Player newTikker) {
        Player old = currentTikker;
        setTikker(newTikker);
        newTikker.sendTitle("§cJIJ BENT DE TIKKER!", "§7Zorg dat je snel iemand anders tikt!", 5, 40, 10);
        newTikker.playSound(newTikker.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

        if (old != null) {
            old.sendTitle("§aJe bent vrij!", "§7Ren snel verder!", 5, 40, 10);
        }
    }

    public void checkWin() {
        List<Player> alive = players.stream()
                .filter(p -> roles.getOrDefault(p.getUniqueId(), PlayerRole.SPECTATOR) != PlayerRole.SPECTATOR)
                .toList();

        if (alive.size() == 1) {
            Player winner = alive.get(0);
            Bukkit.broadcastMessage("§a" + winner.getName() + " heeft gewonnen!");
            endGame();
        }
    }

    private void endGame() {
        state = GameState.ENDING;
        stopTimer();
        Bukkit.getScheduler().runTaskLater(plugin, this::resetGame, 100);
    }

    public void eliminatePlayer(Player player) {
        roles.put(player.getUniqueId(), PlayerRole.SPECTATOR);
        player.setGameMode(GameMode.SPECTATOR);
        player.setGlowing(false);
        players.remove(player);
        if (player.equals(currentTikker)) {
            currentTikker = null;
        }
        checkWin();
    }

    public void removeFromGame(Player player) {
        boolean wasTikker = player.equals(currentTikker);
        players.remove(player);
        roles.remove(player.getUniqueId());
        player.setGameMode(GameMode.ADVENTURE); // of SURVIVAL als je wil
        player.setGlowing(false);
        player.setPlayerListName(player.getName());
        player.setDisplayName(player.getName());
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.sendMessage("§cJe hebt het spel verlaten!");

        if (getAlivePlayers() <= 1) {
            checkWin();
            return;
        }
        if (wasTikker) {
            Player newTikker = getRandomAlivePlayer();
            setTikker(newTikker);
        }
        checkWin();
    }

    private void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void resetGame() {
        stopTimer();

        var lobby = plugin.getSpawnManager().getSpawn("lobby");

        for (Player p : players) {
            if (lobby != null) p.teleport(lobby);

            p.setGameMode(GameMode.ADVENTURE);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.getInventory().clear();
            p.setGlowing(false);
        }
        roles.clear();
        players.clear();
        scoreboard.clearAll();
        state = GameState.WAITING;
        round = 0;
    }

    public void stopGame(String reason) {
        if (state != GameState.RUNNING) return;

        state = GameState.ENDING;
        stopTimer();

        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage("§cSpel gestopt §7(door: §e" + reason + "§7)");
        Bukkit.broadcastMessage(" ");

        Bukkit.getScheduler().runTaskLater(plugin, this::resetGame, 40);
    }

    private void resetPlayer(Player p, Location lobby) {
        if (lobby != null) p.teleport(lobby);

        p.setGameMode(GameMode.ADVENTURE);
        p.setHealth(20);
        p.setFoodLevel(20);
        p.getInventory().clear();
        p.setGlowing(false);
    }

    private Player getRandomAlivePlayer() {
        List<Player> alive = players.stream()
                .filter(p -> roles.getOrDefault(p.getUniqueId(), PlayerRole.SPECTATOR) == PlayerRole.OVERLEVER)
                .toList();
        return alive.get(random.nextInt(alive.size()));
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

    public Tikkertje getPlugin() {
        return plugin;
    }

    public void setRegion(String regionName) {
        this.regionName = regionName;
    }

    public String getRegion() {
        return regionName;
    }

    public boolean hasRegion() {
        return regionName != null;
    }

    public boolean isInRegion(Player player) {
        if (!hasRegion()) return true;
        return WorldGuardHook.isInRegion(player, regionName);
    }

    public boolean hasWorldGuard() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    private String getMissingSpawns() {
        var sm = plugin.getSpawnManager();
        List<String> missing = new ArrayList<>();

        if (sm.getSpawn("lobby") == null) missing.add("lobby");
        if (sm.getSpawn("game") == null) missing.add("game");

        return String.join(", ", missing);
    }

    private boolean hasAllSpawns() {
        return getMissingSpawns().isEmpty();
    }
}