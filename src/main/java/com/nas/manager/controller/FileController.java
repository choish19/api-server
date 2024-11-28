package com.nas.manager.controller;

import com.nas.manager.dto.FileResponse;
import com.nas.manager.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {
    private final FileService fileService;

    @PostMapping
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) List<String> tags,
            Authentication authentication) {
        return ResponseEntity.ok(fileService.storeFile(file, description, tags, authentication));
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> getAllFiles(Authentication authentication) {
        return ResponseEntity.ok(fileService.getAllFiles(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable Long id, Authentication authentication) {
        return fileService.getFile(id, authentication);
    }

    @PutMapping("/{id}/bookmark")
    public ResponseEntity<?> toggleBookmark(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(fileService.toggleBookmark(id, authentication));
    }

    @PutMapping("/{id}/recommend")
    public ResponseEntity<?> incrementRecommendations(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(fileService.incrementRecommendations(id, authentication));
    }
}