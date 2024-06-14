package org.lushplugins.followers.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mysql.cj.util.LRUCache;
import org.lushplugins.lushlib.utils.LushLogger;
import org.mineskin.MineskinClient;
import org.mineskin.data.Skin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class SkinUtils {
    private static final MineskinClient MINESKIN_CLIENT = new MineskinClient("LushFollowersPlugin");
    private static final LRUCache<String, Skin> SKIN_CACHE = new LRUCache<>(50);

    public static CompletableFuture<Skin> generateSkin(String base64) {
        String url = getRawUrlFromBase64(base64);
        if (SKIN_CACHE.containsKey(url)) {
            return CompletableFuture.completedFuture(SKIN_CACHE.get(url));
        }

        return MINESKIN_CLIENT.generateUrl(url).thenApply(skin -> {
            SKIN_CACHE.put(url, skin);
            return skin;
        });
    }

    public static URL getUrlFromBase64(String base64) {
        try {
            String rawUrl = getRawUrlFromBase64(base64);
            if (rawUrl == null) {
                return null;
            }

            return new URL(rawUrl);
        } catch (MalformedURLException e) {
            LushLogger.getLogger().severe(base64 + " does not appear to be a valid texture.");
            return null;
        }
    }

    public static String getRawUrlFromBase64(String base64) {
        String dataRaw = new String(Base64.getDecoder().decode(base64)).toLowerCase();
        JsonObject data = JsonParser.parseString(dataRaw).getAsJsonObject();

        String rawUrl;
        try {
            rawUrl = data.get("textures").getAsJsonObject().get("skin").getAsJsonObject().get("url").getAsString();
        } catch (NullPointerException e) {
            LushLogger.getLogger().severe(base64 + " does not appear to be a valid texture.");
            return null;
        }

        return rawUrl;
    }
}
