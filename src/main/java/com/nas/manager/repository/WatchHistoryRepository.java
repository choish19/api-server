package com.nas.manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import com.nas.manager.model.WatchHistory;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    List<WatchHistory> findByUser(User user);
    Optional<WatchHistory> findByUserAndFile(User user, FileInfo file);
    
    @Modifying
    @Query("DELETE FROM WatchHistory wh WHERE wh.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM WatchHistory wh WHERE wh.user.id = :userId AND wh.file.id = :fileId")
    void deleteByUserIdAndFileId(@Param("userId") Long userId, @Param("fileId") Long fileId);
}