package com.nas.manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nas.manager.dto.FileResponse;
import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import com.nas.manager.service.RecommendationService;
import com.nas.manager.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<FileInfo>> getRecommendations(Authentication authentication) {
        User user = userService.getCurrentUser();
        List<FileInfo> recommendations = recommendationService.getRecommendedFiles(user);
        return ResponseEntity.ok(recommendations);
    }
}