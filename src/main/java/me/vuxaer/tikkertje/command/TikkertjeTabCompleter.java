package me.vuxaer.tikkertje.command;

import me.vuxaer.tikkertje.manager.GameManager;
import me.vuxaer.tikkertje.util.WorldGuardHook;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TikkertjeTabCompleter implements TabCompleter {

    private final GameManager gameManager;

    public TikkertjeTabCompleter(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = List.of("start", "stop", "help", "reload", "setregion", "clearregion", "setspawn");
            for (String sub : subcommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        }

        else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "setspawn" -> {
                    List<String> types = List.of("lobby", "game");
                    for (String type : types) {
                        if (type.startsWith(args[1].toLowerCase())) {
                            completions.add(type);
                        }
                    }
                }

                case "setregion" -> {
                    if (!gameManager.hasWorldGuard()) return completions;
                    List<String> regions = WorldGuardHook.getRegions(player.getWorld());
                    for (String region : regions) {
                        if (region.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(region);
                        }
                    }
                }
            }
        }

        return completions;
    }
}