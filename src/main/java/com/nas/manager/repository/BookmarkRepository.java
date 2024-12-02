package com.nas.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nas.manager.model.Bookmark;
import com.nas.manager.model.User;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUser(User user);
    void deleteByUserIdAndFileId(Long userId, Long fileId);
}