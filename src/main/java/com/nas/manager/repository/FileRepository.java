package com.nas.manager.repository;

import com.nas.manager.model.FileInfo;
import com.nas.manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileRepository extends JpaRepository<FileInfo, Long> {
    List<FileInfo> findByUser(User user);
}