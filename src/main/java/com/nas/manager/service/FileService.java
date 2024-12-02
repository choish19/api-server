package com.nas.manager.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nas.manager.dto.FileResponse;
import com.nas.manager.model.Bookmark;
import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import com.nas.manager.repository.BookmarkRepository;
import com.nas.manager.repository.FileRepository;
import com.nas.manager.repository.UserRepository;
import com.nas.manager.util.LogUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
    private static final Logger logger = LogUtil.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final Path fileStorageLocation;
    private final BookmarkRepository bookmarkRepository;

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
            return mapToFileResponse(savedFile, false);
        } catch (Exception e) {
            logger.error("파일 저장 중 오류 발생", e);
            throw new RuntimeException("Could not store file", e);
        }
    }

    public List<FileResponse> getAllFiles(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Bookmark> userBookmarks = bookmarkRepository.findByUser(user);

            return fileRepository.findAll().stream()
                    .map(fileInfo -> {
                        boolean isBookmarked = userBookmarks.stream()
                                .anyMatch(bookmark -> bookmark.getFile().getId().equals(fileInfo.getId()));
                        return mapToFileResponse(fileInfo, isBookmarked);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("모든 파일 가져오기 중 오류 발생", e);
            throw e;
        }
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
            logger.error("파일 다운로드 중 오류 발생", e);
            throw new RuntimeException("File could not be downloaded", e);
        }
    }

    public FileResponse incrementRecommendations(Long id, Authentication authentication) {
        try {
            FileInfo fileInfo = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));
            fileInfo.setRecommendations(fileInfo.getRecommendations() + 1);

            return mapToFileResponse(fileRepository.save(fileInfo), fileInfo.getBookmarked());
        } catch (Exception e) {
            logger.error("추천 수 증가 중 오류 발생", e);
            throw e;
        }
    }

    public void addBookmark(Long fileId, Authentication authentication) {
        FileInfo fileInfo = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));
            
        if (fileInfo.getBookmarkCount() == null) {
            fileInfo.setBookmarkCount(0);
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        FileInfo file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setFile(file);
        file.setBookmarkCount(file.getBookmarkCount() + 1);

        fileRepository.save(file);
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(Long fileId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        bookmarkRepository.deleteByUserIdAndFileId(user.getId(), fileId);
        FileInfo file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        file.setBookmarkCount(file.getBookmarkCount() - 1);
        fileRepository.save(file);
    }

    private FileResponse mapToFileResponse(FileInfo fileInfo, boolean isBookmarked) {
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
                .bookmarked(isBookmarked)
                .bookmarkCount(fileInfo.getBookmarkCount())
                .tags(fileInfo.getTags())
                .build();
    }
}