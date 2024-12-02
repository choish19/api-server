package com.nas.manager.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nas.manager.dto.FileResponse;
import com.nas.manager.service.FileService;

import lombok.RequiredArgsConstructor;

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

    @PutMapping("/{id}/recommend")
    public ResponseEntity<?> incrementRecommendations(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(fileService.incrementRecommendations(id, authentication));
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<?> addBookmark(@PathVariable("id") Long id, Authentication authentication) {
        fileService.addBookmark(id, authentication);
        return ResponseEntity.ok("Bookmark added");
    }

    @DeleteMapping("/{id}/bookmark")
    public ResponseEntity<?> removeBookmark(@PathVariable("id") Long id, Authentication authentication) {
        fileService.removeBookmark(id, authentication);
        return ResponseEntity.ok("Bookmark removed");
    }
}
