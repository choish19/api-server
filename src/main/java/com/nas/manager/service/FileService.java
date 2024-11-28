package com.nas.manager.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nas.manager.dto.FileResponse;
import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import com.nas.manager.repository.FileRepository;
import com.nas.manager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final Path fileStorageLocation;

    public FileResponse storeFile(MultipartFile file, String description, List<String> tags, Authentication authentication) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(file.getOriginalFilename());
            fileInfo.setType(file.getContentType());
            fileInfo.setPath(fileName);
            fileInfo.setDescription(description);
            fileInfo.setTags(Set.copyOf(tags));
            fileInfo.setLastAccessed(LocalDateTime.now());
            fileInfo.setUser(user);

            FileInfo savedFile = fileRepository.save(fileInfo);
            return mapToFileResponse(savedFile);
        } catch (Exception e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    public List<FileResponse> getAllFiles(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return fileRepository.findAll().stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    public ResponseEntity<Resource> getFile(Long id, Authentication authentication) {
        try {
            FileInfo fileInfo = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            Path filePath = fileStorageLocation.resolve(fileInfo.getPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                fileInfo.setAccessCount(fileInfo.getAccessCount() + 1);
                fileInfo.setLastAccessed(LocalDateTime.now());
                fileRepository.save(fileInfo);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("File not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("File could not be downloaded", e);
        }
    }

    public FileResponse toggleBookmark(Long id, Authentication authentication) {
        FileInfo fileInfo = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        fileInfo.setBookmarked(!fileInfo.getBookmarked());
        return mapToFileResponse(fileRepository.save(fileInfo));
    }

    public FileResponse incrementRecommendations(Long id, Authentication authentication) {
        FileInfo fileInfo = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        fileInfo.setRecommendations(fileInfo.getRecommendations() + 1);
        return mapToFileResponse(fileRepository.save(fileInfo));
    }

    private FileResponse mapToFileResponse(FileInfo fileInfo) {
        return FileResponse.builder()
                .id(fileInfo.getId())
                .name(fileInfo.getName())
                .type(fileInfo.getType())
                .url("/api/files/" + fileInfo.getId())
                .thumbnail(fileInfo.getThumbnail())
                .description(fileInfo.getDescription())
                .lastAccessed(fileInfo.getLastAccessed())
                .accessCount(fileInfo.getAccessCount())
                .recommendations(fileInfo.getRecommendations())
                .bookmarked(fileInfo.getBookmarked())
                .tags(fileInfo.getTags())
                .build();
    }
}