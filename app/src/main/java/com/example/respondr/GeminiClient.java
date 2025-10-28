package com.example.respondr;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Minimal Gemini client for MVP.
 *
 * IMPORTANT: This class uses a placeholder endpoint and expects an API key to be set in
 * the `GEMINI_API_KEY` constant below or set by modifying the code. This is an MVP helper:
 * - Set GEMINI_ENDPOINT to the REST endpoint you use.
 * - Set GEMINI_API_KEY to your API key (or better: load from secure storage / env for production).
 */
public class GeminiClient {

    // Google AI Studio (Gemini) endpoint - using gemini-2.0-flash-exp (latest free model)
    private static final String MODEL_NAME = "gemini-2.0-flash-exp";
    private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent";

    // API key is now loaded from BuildConfig (stored in local.properties, not committed to Git)
    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Context ctx;

    public GeminiClient(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        client = new OkHttpClient.Builder().build();
    }

    public interface ResultCallback {
        void onSuccess(String reply);
        void onFailure(String error);
    }

    public void sendMessage(String message, ResultCallback cb) {
        // Build request body for Gemini API: { "contents": [{ "parts": [{ "text": "..." }] }] }
        JsonObject root = new JsonObject();
        JsonObject part = new JsonObject();
        part.addProperty("text", message);
        
        JsonObject content = new JsonObject();
        com.google.gson.JsonArray parts = new com.google.gson.JsonArray();
        parts.add(part);
        content.add("parts", parts);
        
        com.google.gson.JsonArray contents = new com.google.gson.JsonArray();
        contents.add(content);
        root.add("contents", contents);

        RequestBody body = RequestBody.create(root.toString(), JSON);

        Request.Builder reqBuilder = new Request.Builder()
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json");

        // If an API key is provided, use it as a query parameter. If it looks like a Bearer token
        // (starts with "Bearer "), use it as an Authorization header.
        if (GEMINI_API_KEY != null && !GEMINI_API_KEY.isEmpty()) {
            if (GEMINI_API_KEY.trim().toLowerCase().startsWith("bearer ")) {
                reqBuilder.addHeader("Authorization", GEMINI_API_KEY.trim());
                reqBuilder.url(GEMINI_ENDPOINT);
            } else {
                reqBuilder.url(GEMINI_ENDPOINT + "?key=" + GEMINI_API_KEY.trim());
            }
        } else {
            // No API key provided in code — still set the URL (user may use other auth at runtime)
            reqBuilder.url(GEMINI_ENDPOINT);
        }

        Request request = reqBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                cb.onFailure("Network error: Unable to connect. Please check your internet connection.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String respBody = response.body() != null ? response.body().string() : "";
                    // Try to extract error message from response
                    try {
                        JsonObject errorObj = JsonParser.parseString(respBody).getAsJsonObject();
                        if (errorObj.has("error")) {
                            JsonObject error = errorObj.getAsJsonObject("error");
                            String errorMsg = error.has("message") ? error.get("message").getAsString() : "Unknown error";
                            cb.onFailure("API Error: " + errorMsg);
                            return;
                        }
                    } catch (Exception ignored) {}
                    cb.onFailure("API Error: " + response.message());
                    return;
                }
                String body = response.body() != null ? response.body().string() : "";
                // Parse Gemini API response: { "candidates": [{ "content": { "parts": [{ "text": "..." }] } }] }
                try {
                    JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
                    if (obj.has("candidates")) {
                        StringBuilder sb = new StringBuilder();
                        for (var el : obj.getAsJsonArray("candidates")) {
                            JsonObject cand = el.getAsJsonObject();
                            if (cand.has("content")) {
                                JsonObject content = cand.getAsJsonObject("content");
                                if (content.has("parts")) {
                                    for (var partEl : content.getAsJsonArray("parts")) {
                                        JsonObject partObj = partEl.getAsJsonObject();
                                        if (partObj.has("text")) {
                                            sb.append(partObj.get("text").getAsString());
                                        }
                                    }
                                }
                            }
                        }
                        String reply = sb.length() > 0 ? sb.toString() : body;
                        cb.onSuccess(reply);
                        return;
                    }
                } catch (Exception ignored) {
                }
                // fallback: return the raw response
                cb.onSuccess(body);
            }
        });
    }
}
