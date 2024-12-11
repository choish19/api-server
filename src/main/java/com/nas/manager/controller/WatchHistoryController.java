package com.nas.manager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nas.manager.service.WatchHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/watch-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class WatchHistoryController {
    private final WatchHistoryService watchHistoryService;

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteWatchHistory(
            @PathVariable Long fileId,
            Authentication authentication) {
        watchHistoryService.deleteWatchHistory(fileId, authentication);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllWatchHistory(Authentication authentication) {
        watchHistoryService.deleteAllWatchHistory(authentication);
        return ResponseEntity.ok().build();
    }
}