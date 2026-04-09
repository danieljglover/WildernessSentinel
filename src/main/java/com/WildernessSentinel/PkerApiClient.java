package com.WildernessSentinel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class PkerApiClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String serverUrl;

    @Setter
    private String apiKey;

    public PkerApiClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    public String register() {
        JsonObject body = new JsonObject();
        Request request = new Request.Builder()
            .url(serverUrl + "/api/register")
            .post(RequestBody.create(JSON, body.toString()))
            .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                return json.get("apiKey").getAsString();
            }
        } catch (IOException e) {
            log.warn("Failed to register with PKer server", e);
        }
        return null;
    }

    public void reportAttacker(String attackerName, int wildernessLevel, int world) {
        if (apiKey == null) return;
        JsonObject body = new JsonObject();
        body.addProperty("attackerName", attackerName);
        body.addProperty("wildernessLevel", wildernessLevel);
        body.addProperty("world", world);
        Request request = new Request.Builder()
            .url(serverUrl + "/api/report")
            .header("X-API-Key", apiKey)
            .post(RequestBody.create(JSON, body.toString()))
            .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Failed to report attacker: {}", response.code());
            }
        } catch (IOException e) {
            log.warn("Failed to report attacker to PKer server", e);
        }
    }

    public void updateLastSeen(String attackerName, int world) {
        if (apiKey == null) return;
        JsonObject body = new JsonObject();
        body.addProperty("attackerName", attackerName);
        body.addProperty("wildernessLevel", 0);
        body.addProperty("world", world);
        Request request = new Request.Builder()
            .url(serverUrl + "/api/seen")
            .header("X-API-Key", apiKey)
            .put(RequestBody.create(JSON, body.toString()))
            .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Failed to update last seen: {}", response.code());
            }
        } catch (IOException e) {
            log.warn("Failed to update last seen on PKer server", e);
        }
    }

    public Map<String, PkerInfo> searchPkers(String name) {
        if (apiKey == null) return new HashMap<>();
        Request request = new Request.Builder()
            .url(serverUrl + "/api/pkers/search?name=" + name)
            .header("X-API-Key", apiKey)
            .get()
            .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                JsonArray arr = json.getAsJsonArray("pkers");
                Map<String, PkerInfo> result = new HashMap<>();
                arr.forEach(e -> {
                    JsonObject entry = e.getAsJsonObject();
                    String pkerName = entry.get("name").getAsString();
                    int reportCount = entry.get("reportCount").getAsInt();
                    int lastSeenWorld = entry.has("lastSeenWorld") ? entry.get("lastSeenWorld").getAsInt() : 0;
                    result.put(pkerName, new PkerInfo(reportCount, lastSeenWorld));
                });
                return result;
            }
        } catch (IOException e) {
            log.warn("Failed to search PKers", e);
        }
        return new HashMap<>();
    }

    public Map<String, PkerInfo> fetchPkers(PkerDatabaseMode mode) {
        if (apiKey == null) return new HashMap<>();
        String modeParam = mode == PkerDatabaseMode.COMMUNITY ? "community" : "personal";
        Request request = new Request.Builder()
            .url(serverUrl + "/api/pkers?mode=" + modeParam)
            .header("X-API-Key", apiKey)
            .get()
            .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                JsonArray arr = json.getAsJsonArray("pkers");
                Map<String, PkerInfo> result = new HashMap<>();
                arr.forEach(e -> {
                    JsonObject entry = e.getAsJsonObject();
                    String name = entry.get("name").getAsString();
                    int reportCount = entry.get("reportCount").getAsInt();
                    int lastSeenWorld = entry.has("lastSeenWorld") ? entry.get("lastSeenWorld").getAsInt() : 0;
                    result.put(name, new PkerInfo(reportCount, lastSeenWorld));
                });
                return result;
            }
        } catch (IOException e) {
            log.warn("Failed to fetch PKer list", e);
        }
        return new HashMap<>();
    }

    public List<HotspotEntry> fetchHotspots() {
        if (apiKey == null) return new ArrayList<>();
        Request request = new Request.Builder()
            .url(serverUrl + "/api/stats/hotspots")
            .header("X-API-Key", apiKey)
            .get()
            .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
                JsonArray arr = json.getAsJsonArray("hotspots");
                List<HotspotEntry> result = new ArrayList<>();
                arr.forEach(e -> {
                    JsonObject entry = e.getAsJsonObject();
                    result.add(new HotspotEntry(
                        entry.get("zone").getAsString(),
                        entry.get("pkerCount").getAsInt()));
                });
                return result;
            }
        } catch (IOException e) {
            log.warn("Failed to fetch hotspot data", e);
        }
        return new ArrayList<>();
    }
}
