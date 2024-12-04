package com.nas.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import com.nas.manager.model.UserInteraction;

public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    List<UserInteraction> findByUser(User user);
    List<UserInteraction> findByFile(FileInfo file);
    
    @Query("SELECT ui.file.id, COUNT(ui) as count FROM UserInteraction ui " +
           "GROUP BY ui.file.id ORDER BY count DESC LIMIT :limit")
    List<Object[]> findMostPopularFiles(@Param("limit") int limit);
    
    @Query("SELECT ui.file.id FROM UserInteraction ui " +
           "WHERE ui.user.id = :userId " +
           "GROUP BY ui.file.id ORDER BY COUNT(ui) DESC LIMIT :limit")
    List<Long> findMostInteractedFilesByUser(@Param("userId") Long userId, @Param("limit") int limit);
}