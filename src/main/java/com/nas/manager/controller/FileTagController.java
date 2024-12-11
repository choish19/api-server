package com.nas.manager.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nas.manager.dto.FileResponse;
import com.nas.manager.dto.UserFileTagRequest;
import com.nas.manager.service.FileTagService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class FileTagController {
    private final FileTagService fileTagService;

    @PostMapping("/{fileId}/tags")
    public ResponseEntity<?> addTag(
            @PathVariable Long fileId,
            @RequestBody UserFileTagRequest request,
            Authentication authentication) {
        fileTagService.addTag(fileId, request.getTag(), authentication);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{fileId}/tags/{tag}")
    public ResponseEntity<?> removeTag(
            @PathVariable Long fileId,
            @PathVariable String tag,
            Authentication authentication) {
        fileTagService.removeTag(fileId, tag, authentication);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tags")
    public ResponseEntity<Set<String>> getUserTags(Authentication authentication) {
        return ResponseEntity.ok(fileTagService.getUserTags(authentication));
    }

    @GetMapping("/tags/{tag}")
    public ResponseEntity<List<FileResponse>> getFilesByTag(
            @PathVariable String tag,
            Authentication authentication) {
        return ResponseEntity.ok(fileTagService.getFilesByTag(tag, authentication));
    }
}