package me.vuxaer.tikkertje.net;

import com.google.gson.Gson;
import me.vuxaer.tikkertje.util.GameResult;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpService {

    private final JavaPlugin plugin;
    private final Gson gson = new Gson();

    public HttpService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendGameResult(String endpoint, GameResult result) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");

                conn.setDoOutput(true);

                String json = gson.toJson(result);
                byte[] input = json.getBytes(StandardCharsets.UTF_8);

                conn.setRequestProperty("Content-Length", String.valueOf(input.length));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(input);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    plugin.getLogger().info("Game result sent successfully! (" + responseCode + ")");
                    plugin.getLogger().warning("Response code: " + responseCode);
                    plugin.getLogger().warning("Location: " + conn.getHeaderField("Location"));
                } else {
                    plugin.getLogger().warning("API responded with code: " + responseCode);
                }
            } catch (ConnectException e) {
                plugin.getLogger().warning("Could not send game result: API is offline or unreachable.");

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send game result: " + e.getMessage());
            }
        });
    }
}