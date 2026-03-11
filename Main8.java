package com.stresser.controller;

import com.stresser.model.*;
import com.stresser.service.AttackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AttackController {
    
    @Autowired
    private AttackService attackService;
    
    @PostMapping("/attack/start")
    public ResponseEntity<String> startAttack(@RequestBody AttackRequest request) {
        String attackId = attackService.startAttack(request);
        return ResponseEntity.ok(attackId);
    }
    
    @PostMapping("/attack/stop/{attackId}")
    public ResponseEntity<Void> stopAttack(@PathVariable String attackId) {
        attackService.stopAttack(attackId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/attack/stats/{attackId}")
    public ResponseEntity<AttackStats> getStats(@PathVariable String attackId) {
        AttackStats stats = attackService.getStats(attackId);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }
}
