package com.nas.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nas.manager.model.Bookmark;
import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUser(User user);
    void deleteByUserIdAndFileId(Long userId, Long fileId);
    boolean existsByUserAndFile(User user, FileInfo file);
    boolean existsByUserIdAndFileId(Long userId, Long fileId);
}