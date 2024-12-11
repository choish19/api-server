package com.nas.manager.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;

public interface FileRepository extends JpaRepository<FileInfo, Long> {
    List<FileInfo> findByUser(User user);
    Page<FileInfo> findByUser(User user, Pageable pageable);
    
    List<FileInfo> findTop3ByOrderByLastWriteTimeDesc();
    
    @Query(value = "SELECT * FROM files ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<FileInfo> findRandomFiles(@Param("limit") int limit);

    @Query("SELECT DISTINCT f FROM FileInfo f " +
           "LEFT JOIN f.watchHistories wh " +
           "WHERE wh.user.id = :userId " +
           "ORDER BY wh.watchedAt DESC")
    Page<FileInfo> findFilesWithHistoryByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT f FROM FileInfo f " +
           "LEFT JOIN Bookmark b ON b.file = f AND b.user.id = :userId " +
           "WHERE b.id IS NOT NULL")
    Page<FileInfo> findBookmarkedFiles(@Param("userId") Long userId, Pageable pageable);
}