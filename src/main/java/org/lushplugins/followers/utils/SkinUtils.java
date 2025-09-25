package org.lushplugins.followers.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mysql.cj.util.LRUCache;
import org.lushplugins.lushlib.LushLogger;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.data.Skin;
import org.mineskin.request.GenerateRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class SkinUtils {
    private static final MineSkinClient MINESKIN_CLIENT = MineSkinClient.builder()
        .requestHandler(JsoupRequestHandler::new)
        .userAgent("LushFollowersPlugin")
        .build();
    private static final LRUCache<String, Skin> SKIN_CACHE = new LRUCache<>(50);

    public static CompletableFuture<Skin> generateSkin(String base64) {
        String url = getRawUrlFromBase64(base64);
        if (SKIN_CACHE.containsKey(url)) {
            return CompletableFuture.completedFuture(SKIN_CACHE.get(url));
        }

        try {
            GenerateRequest.url(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try {
            return MINESKIN_CLIENT.generate().submitAndWait(GenerateRequest.url(url)).thenApply(response -> {
                Skin skin = response.getSkin();
                SKIN_CACHE.put(url, skin);
                return skin;
            });
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
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
