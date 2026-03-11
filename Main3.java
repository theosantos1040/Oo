package com.stresser.model;

import lombok.Data;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class AttackStats {
    private AtomicLong totalRequests = new AtomicLong(0);
    private AtomicLong successfulRequests = new AtomicLong(0);
    private AtomicLong failedRequests = new AtomicLong(0);
    private AtomicLong bytesTransferred = new AtomicLong(0);
    private AtomicInteger activeThreads = new AtomicInteger(0);
    private long startTime;
    private volatile boolean running = false;
    
    public void incrementTotal() {
        totalRequests.incrementAndGet();
    }
    
    public void incrementSuccess() {
        successfulRequests.incrementAndGet();
    }
    
    public void incrementFailed() {
        failedRequests.incrementAndGet();
    }
    
    public void addBytes(long bytes) {
        bytesTransferred.addAndGet(bytes);
    }
    
    public double getRPS() {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed == 0) return 0;
        return (totalRequests.get() * 1000.0) / elapsed;
    }
    
    public double getSuccessRate() {
        long total = totalRequests.get();
        if (total == 0) return 0;
        return (successfulRequests.get() * 100.0) / total;
    }
}
