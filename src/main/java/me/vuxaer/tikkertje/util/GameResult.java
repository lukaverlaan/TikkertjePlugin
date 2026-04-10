package me.vuxaer.tikkertje.util;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameResult {

    private final List<PlayerData> players;
    private final String winner;
    private final int rounds;
    private final long durationSeconds;

    public GameResult(List<Player> players, Player winner, int rounds, long durationSeconds) {
        this.players = players.stream()
                .map(PlayerData::new)
                .collect(Collectors.toList());

        this.winner = winner != null ? winner.getName() : "none";
        this.rounds = rounds;
        this.durationSeconds = durationSeconds;
    }

    public static class PlayerData {
        private final UUID uuid;
        private final String name;

        public PlayerData(Player player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }
    }

    public List<PlayerData> getPlayers() {
        return players;
    }

    public String getWinner() {
        return winner;
    }

    public int getRounds() {
        return rounds;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }
}
