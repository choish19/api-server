package com.nas.manager.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nas.manager.dto.FileResponse;
import com.nas.manager.dto.PageRequestDto;
import com.nas.manager.dto.PageResponse;
import com.nas.manager.model.Bookmark;
import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import com.nas.manager.model.UserFileTag;
import com.nas.manager.model.WatchHistory;
import com.nas.manager.repository.BookmarkRepository;
import com.nas.manager.repository.FileRepository;
import com.nas.manager.repository.UserFileTagRepository;
import com.nas.manager.repository.UserRepository;
import com.nas.manager.repository.WatchHistoryRepository;
import com.nas.manager.util.LogUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
    private static final Logger logger = LogUtil.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final Path fileStorageLocation;
    private final WatchHistoryRepository watchHistoryRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserFileTagRepository userFileTagRepository;

    public FileResponse storeFile(MultipartFile file, String description, List<String> tags, Authentication authentication) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            User user = getUser(authentication);

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

    public PageResponse<FileResponse> getAllFiles(Authentication authentication, PageRequestDto request) {
        try {
            User user = getUser(authentication);
            List<Bookmark> userBookmarks = bookmarkRepository.findByUser(user);
            List<WatchHistory> watchHistories = watchHistoryRepository.findByUser(user);

            Sort sort = Sort.by(
                Sort.Direction.fromString(request.getDirection()),
                request.getSortBy()
            );

            Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
            );

            Page<FileInfo> filePage = fileRepository.findAll(pageable);

            List<FileResponse> content = filePage.getContent().stream()
                    .map(fileInfo -> {
                        boolean isBookmarked = isBookmarked(userBookmarks, fileInfo);
                        LocalDateTime watchedAt = getWatchedAt(watchHistories, fileInfo);
                        Set<String> userTags = getFileTags(user, fileInfo);
                        return mapToFileResponse(fileInfo, isBookmarked, userTags, watchedAt);
                    })
                    .collect(Collectors.toList());

            return PageResponse.<FileResponse>builder()
                    .content(content)
                    .pageNumber(filePage.getNumber())
                    .pageSize(filePage.getSize())
                    .totalElements(filePage.getTotalElements())
                    .totalPages(filePage.getTotalPages())
                    .last(filePage.isLast())
                    .build();
        } catch (Exception e) {
            logger.error("모든 파일 가져오기 중 오류 발생", e);
            throw e;
        }
    }

    public List<FileResponse> getFilesWithHistory(Authentication authentication) {
        User user = getUser(authentication);
        List<WatchHistory> watchHistories = watchHistoryRepository.findByUser(user);
        List<Bookmark> userBookmarks = bookmarkRepository.findByUser(user);

        return watchHistories.stream()
                .map(watchHistory -> {
                    FileInfo fileInfo = watchHistory.getFile();
                    boolean isBookmarked = isBookmarked(userBookmarks, fileInfo);
                    Set<String> userTags = getFileTags(user, fileInfo);
                    return mapToFileResponse(fileInfo, isBookmarked, userTags, watchHistory.getWatchedAt());
                })
                .collect(Collectors.toList());
    }

    public List<FileResponse> getFilesWithBookmarks(Authentication authentication) {
        User user = getUser(authentication);
        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
        List<WatchHistory> watchHistories = watchHistoryRepository.findByUser(user);

        return bookmarks.stream()
                .map(bookmark -> {
                    FileInfo fileInfo = bookmark.getFile();
                    LocalDateTime watchedAt = getWatchedAt(watchHistories, fileInfo);
                    Set<String> userTags = getFileTags(user, fileInfo);
                    return mapToFileResponse(fileInfo, true, userTags, watchedAt);
                })
                .collect(Collectors.toList());
    }

    // public ResponseEntity<Resource> getFile(Long id, Authentication authentication) {
    //     try {
    //         FileInfo fileInfo = getFileInfo(id);
    //         Path filePath = fileStorageLocation.resolve(fileInfo.getPath());
    //         Resource resource = new UrlResource(filePath.toUri());

    //         if (resource.exists()) {
    //             updateFileAccess(fileInfo);
    //             return ResponseEntity.ok()
    //                     .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"")
    //                     .body(resource);
    //         } else {
    //             throw new RuntimeException("File not found");
    //         }
    //     } catch (Exception e) {
    //         logger.error("파일 다운로드 중 오류 발생", e);
    //         throw new RuntimeException("File could not be downloaded", e);
    //     }
    // }

    public FileResponse incrementRecommendations(Long id, Authentication authentication) {
        try {
            FileInfo fileInfo = getFileInfo(id);
            fileInfo.setRecommendations(fileInfo.getRecommendations() + 1);

            return mapToFileResponse(fileRepository.save(fileInfo), fileInfo.getBookmarked());
        } catch (Exception e) {
            logger.error("추천 수 증가 중 오류 발생", e);
            throw e;
        }
    }

    public void recordWatchHistory(Long fileId, Authentication authentication) {
        User user = getUser(authentication);
        FileInfo file = getFileInfo(fileId);

        updateFileAccess(file);

        WatchHistory watchHistory = watchHistoryRepository.findByUserAndFile(user, file)
                .orElseGet(() -> {
                    WatchHistory newHistory = new WatchHistory();
                    newHistory.setUser(user);
                    newHistory.setFile(file);
                    return newHistory;
                });

        watchHistory.setWatchedAt(LocalDateTime.now());
        watchHistoryRepository.save(watchHistory);
    }

    public void addBookmark(Long fileId, Authentication authentication) {
        FileInfo fileInfo = getFileInfo(fileId);
        initializeBookmarkCount(fileInfo);

        User user = getUser(authentication);

        boolean exists = bookmarkRepository.existsByUserAndFile(user, fileInfo);
        if (!exists) {
            Bookmark bookmark = new Bookmark();
            bookmark.setUser(user);
            bookmark.setFile(fileInfo);
            fileInfo.setBookmarkCount(fileInfo.getBookmarkCount() + 1);

            fileRepository.save(fileInfo);
            bookmarkRepository.save(bookmark);
        } else {
            logger.info("이미 북마크가 존재합니다: userId={}, fileId={}", user.getId(), fileId);
        }
    }

    @Transactional
    public void removeBookmark(Long fileId, Authentication authentication) {
        User user = getUser(authentication);
        bookmarkRepository.deleteByUserIdAndFileId(user.getId(), fileId);
        FileInfo file = getFileInfo(fileId);
        file.setBookmarkCount(file.getBookmarkCount() - 1);
        fileRepository.save(file);
    }

    public FileResponse mapToFileResponse(FileInfo fileInfo, boolean isBookmarked) {
        return mapToFileResponse(fileInfo, isBookmarked, null, null);
    }

    private FileResponse mapToFileResponse(FileInfo fileInfo, boolean isBookmarked, Set<String> tags, LocalDateTime watchedAt) {
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
                .tags(tags != null ? tags : fileInfo.getTags())
                .watchedAt(watchedAt)
                .build();
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private FileInfo getFileInfo(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    private boolean isBookmarked(List<Bookmark> bookmarks, FileInfo fileInfo) {
        return bookmarks.stream()
                .anyMatch(bookmark -> bookmark.getFile().getId().equals(fileInfo.getId()));
    }

    private void updateFileAccess(FileInfo fileInfo) {
        fileInfo.setAccessCount(fileInfo.getAccessCount() + 1);
        fileInfo.setLastAccessed(LocalDateTime.now());
        fileRepository.save(fileInfo);
    }

    private void initializeBookmarkCount(FileInfo fileInfo) {
        if (fileInfo.getBookmarkCount() == null) {
            fileInfo.setBookmarkCount(0);
        }
    }

    public Set<String> getFileTags(User user, FileInfo file) {
        return userFileTagRepository.findByUserAndFile(user, file)
                .stream()
                .map(UserFileTag::getTag)
                .collect(Collectors.toSet());
    }

    private LocalDateTime getWatchedAt(List<WatchHistory> watchHistories, FileInfo fileInfo) {
        return watchHistories.stream()
                .filter(watchHistory -> watchHistory.getFile().getId().equals(fileInfo.getId()))
                .map(WatchHistory::getWatchedAt)
                .findFirst()
                .orElse(null);
    }
}