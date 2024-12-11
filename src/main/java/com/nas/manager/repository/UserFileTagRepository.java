package com.nas.manager.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import com.nas.manager.model.UserFileTag;

public interface UserFileTagRepository extends JpaRepository<UserFileTag, Long> {
    List<UserFileTag> findByUserAndFile(User user, FileInfo file);
    
    @Query("SELECT DISTINCT uft.tag FROM UserFileTag uft WHERE uft.user = :user")
    Set<String> findAllTagsByUser(@Param("user") User user);
    
    void deleteByUserAndFileAndTag(User user, FileInfo file, String tag);
    
    List<UserFileTag> findByUserAndTag(User user, String tag);
}