package me.vuxaer.tikkertje.command;

import me.vuxaer.tikkertje.Tikkertje;
import me.vuxaer.tikkertje.manager.GameManager;
import me.vuxaer.tikkertje.util.GameState;
import me.vuxaer.tikkertje.util.WorldGuardHook;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TikkertjeCommand implements CommandExecutor {

    private final Tikkertje plugin = Tikkertje.getInstance();
    private final GameManager gameManager;

    public TikkertjeCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        if (!player.hasPermission("tikkertje.admin")) {
            player.sendMessage("§cJe hebt geen permissie om dit commando uit te voeren!");
            return true;
        }

        GameState state = gameManager.getState();
        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(player);
            case "start" -> {
                if (state == GameState.RUNNING) {
                    player.sendMessage("§cEr is al een spel bezig!");
                    return true;
                }

                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                if (players.size() < 2) {
                    player.sendMessage("§cMinimaal 2 spelers nodig!");
                    return true;
                }
                gameManager.startGame(players, player);
            }
            case "stop" -> {
                if (state != GameState.RUNNING) {
                    player.sendMessage("§cEr is geen spel bezig!");
                    return true;
                }
                gameManager.stopGame(player.getName());
            }
            case "reload" -> {
                plugin.reloadConfig();
                String region = plugin.getConfig().getString("region");
                gameManager.setRegion((region == null || region.isEmpty()) ? null : region);
                player.sendMessage("§aConfig succesvol herladen!");
            }
            case "setregion" -> {
                if (isGameRunning(player, "§cJe kunt de region niet aanpassen tijdens een spel!")) return true;
                if (!checkWorldGuard(player)) return true;
                if (args.length <= 1) {
                    player.sendMessage("§cGebruik: /tikkertje setregion <naam>");
                    return true;
                }

                String region = args[1].toLowerCase();
                if (!WorldGuardHook.regionExists(player.getWorld(), region)) {
                    player.sendMessage("§cDeze region bestaat niet!");
                    return true;
                }

                gameManager.setRegion(region);
                plugin.getConfig().set("region", region);
                plugin.saveConfig();

                player.sendMessage("§aRegion ingesteld op: §e" + region);
            }
            case "clearregion" -> {
                if (isGameRunning(player, "§cJe kunt de region niet aanpassen tijdens een spel!")) return true;
                if (!checkWorldGuard(player)) return true;
                if (!gameManager.hasRegion()) {
                    player.sendMessage("§cEr is geen region ingesteld!");
                    return true;
                }

                gameManager.setRegion(null);
                plugin.getConfig().set("region", null);
                plugin.saveConfig();

                player.sendMessage("§cRegion verwijderd!");
            }
            case "setspawn" -> {
                if (isGameRunning(player, "§cJe kunt de spawns niet aanpassen tijdens een spel!")) return true;
                if (args.length <= 1) {
                    player.sendMessage("§cGebruik: /tikkertje setspawn <lobby|game>");
                    return true;
                }

                String type = args[1].toLowerCase();
                if (!type.equals("lobby") && !type.equals("game")) {
                    player.sendMessage("§cGebruik: lobby of game");
                    return true;
                }

                plugin.getSpawnManager().setSpawn(type, player.getLocation());
                plugin.saveConfig();
                player.sendMessage("§aSpawn gezet voor: §e" + type);
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private boolean isGameRunning(Player player, String message) {
        if (gameManager.getState() == GameState.RUNNING) {
            player.sendMessage(message);
            return true;
        }
        return false;
    }

    private boolean checkWorldGuard(Player player) {
        if (!gameManager.hasWorldGuard()) {
            player.sendMessage("§cWorldGuard is niet geïnstalleerd!");
            return false;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage("§6§lTikkertje Commands");
        sender.sendMessage(" ");
        sender.sendMessage("§e/tikkertje start §7- Start een spel");
        sender.sendMessage("§e/tikkertje stop §7- Stop het spel");
        sender.sendMessage("§e/tikkertje help §7- Toon dit menu");
        sender.sendMessage("§e/tikkertje reload §7- Herlaad de config");
        sender.sendMessage("§e/tikkertje setregion <naam> §7- Stel een region in");
        sender.sendMessage("§e/tikkertje clearregion §7- Verwijder de huidige region");
        sender.sendMessage("§e/tikkertje setspawn <lobby|game> §7- Stel een lobby/game spawn in");

        if (gameManager.hasRegion()) {
            sender.sendMessage(" ");
            sender.sendMessage("§7Huidige region: §e" + gameManager.getRegion());
        }

        sender.sendMessage(" ");
    }
}