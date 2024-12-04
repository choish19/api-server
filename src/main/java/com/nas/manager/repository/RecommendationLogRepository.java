package com.nas.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nas.manager.model.RecommendationLog;
import com.nas.manager.model.User;

import java.util.List;

public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {
    List<RecommendationLog> findByUserOrderByScoreDesc(User user);
}