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
import com.nas.manager.dto.PageRequestDto;
import com.nas.manager.dto.PageResponse;
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
    public ResponseEntity<PageResponse<FileResponse>> getAllFiles(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastAccessed") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        PageRequestDto pageRequest = new PageRequestDto();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setSortBy(sortBy);
        pageRequest.setDirection(direction);
        
        return ResponseEntity.ok(fileService.getAllFiles(authentication, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable Long id, Authentication authentication) {
        return fileService.getFile(id, authentication);
    }

    @GetMapping("/history")
    public ResponseEntity<List<FileResponse>> getFilesWithHistory(Authentication authentication) {
        return ResponseEntity.ok(fileService.getFilesWithHistory(authentication));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<List<FileResponse>> getFilesWithBookmarks(Authentication authentication) {
        return ResponseEntity.ok(fileService.getFilesWithBookmarks(authentication));
    }

    @PutMapping("/{id}/recommend")
    public ResponseEntity<?> incrementRecommendations(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(fileService.incrementRecommendations(id, authentication));
    }

    @PostMapping("/{id}/watch")
    public ResponseEntity<?> recordWatchHistory(@PathVariable("id") Long id, Authentication authentication) {
        fileService.recordWatchHistory(id, authentication);
        return ResponseEntity.ok("Watch history recorded");
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