package com.example.SpringBoot_Bakend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "SmritiSetu Spring Boot Backend",
                "version", "1.0.0",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @RequestMapping(value = "/", method = RequestMethod.HEAD)
    public ResponseEntity<Void> rootHead() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP"
        ));
    }
}
