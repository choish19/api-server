package com.nas.manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import com.nas.manager.model.WatchHistory;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    List<WatchHistory> findByUser(User user);
    Optional<WatchHistory> findByUserAndFile(User user, FileInfo file);
}