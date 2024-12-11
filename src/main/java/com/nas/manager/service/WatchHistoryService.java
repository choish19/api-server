package com.nas.manager.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nas.manager.model.User;
import com.nas.manager.repository.UserRepository;
import com.nas.manager.repository.WatchHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {
    private final WatchHistoryRepository watchHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void deleteWatchHistory(Long fileId, Authentication authentication) {
        User user = getUser(authentication);
        watchHistoryRepository.deleteByUserIdAndFileId(user.getId(), fileId);
    }

    @Transactional
    public void deleteAllWatchHistory(Authentication authentication) {
        User user = getUser(authentication);
        watchHistoryRepository.deleteAllByUserId(user.getId());
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}