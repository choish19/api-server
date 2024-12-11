package com.nas.manager.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import com.nas.manager.dto.FileResponse;
import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import com.nas.manager.model.UserFileTag;
import com.nas.manager.repository.FileRepository;
import com.nas.manager.repository.UserFileTagRepository;
import com.nas.manager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileTagService {
    private final UserFileTagRepository userFileTagRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    @Transactional
    public void addTag(Long fileId, String tag, Authentication authentication) {
        User user = getUser(authentication);
        FileInfo file = getFile(fileId);

        UserFileTag userFileTag = new UserFileTag();
        userFileTag.setUser(user);
        userFileTag.setFile(file);
        userFileTag.setTag(tag);

        userFileTagRepository.save(userFileTag);
    }

    @Transactional
    public void removeTag(Long fileId, String tag, Authentication authentication) {
        User user = getUser(authentication);
        FileInfo file = getFile(fileId);
        userFileTagRepository.deleteByUserAndFileAndTag(user, file, tag);
    }

    public Set<String> getUserTags(Authentication authentication) {
        User user = getUser(authentication);
        return userFileTagRepository.findAllTagsByUser(user);
    }

    public List<FileResponse> getFilesByTag(String tag, Authentication authentication) {
        User user = getUser(authentication);
        return userFileTagRepository.findByUserAndTag(user, tag).stream()
                .map(userFileTag -> fileService.mapToFileResponse(userFileTag.getFile(), false))
                .collect(Collectors.toList());
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private FileInfo getFile(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }
}