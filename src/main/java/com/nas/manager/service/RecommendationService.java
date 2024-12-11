package com.nas.manager.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nas.manager.model.FileInfo;
import com.nas.manager.model.RecommendationLog;
import com.nas.manager.model.User;
import com.nas.manager.model.UserInteraction;
import com.nas.manager.repository.FileRepository;
import com.nas.manager.repository.RecommendationLogRepository;
import com.nas.manager.repository.UserInteractionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final UserInteractionRepository interactionRepository;
    private final RecommendationLogRepository recommendationLogRepository;
    private final FileRepository fileRepository;
    
    private static final int MINIMUM_INTERACTIONS = 5;
    private static final int RECOMMENDATION_SIZE = 10;

    public List<FileInfo> getRecommendedFiles(User user) {
        List<UserInteraction> userInteractions = interactionRepository.findByUser(user);
        
        // 사용자의 상호작용이 최소 기준보다 적으면 대체 추천
        if (userInteractions.size() < MINIMUM_INTERACTIONS) {
            return getFallbackRecommendations();
        }
        
        List<FileInfo> candidates = generateCandidates(user);
        if (candidates.isEmpty()) {
            return getFallbackRecommendations();
        }
        
        return rankCandidates(user, candidates);
    }

    private List<FileInfo> getFallbackRecommendations() {
        List<FileInfo> recommendations = new ArrayList<>();
        
        // 1. 최신 파일들 (30%)
        List<FileInfo> recentFiles = fileRepository.findTop3ByOrderByLastWriteTimeDesc();
        recommendations.addAll(recentFiles);
        
        // 2. 가장 인기있는 파일들 (40%)
        List<Object[]> popularFiles = interactionRepository.findMostPopularFiles(4);
        List<Long> popularFileIds = popularFiles.stream()
                .map(obj -> (Long) obj[0])
                .collect(Collectors.toList());
        recommendations.addAll(fileRepository.findAllById(popularFileIds));
        
        // 3. 무작위 파일들 (30%)
        List<FileInfo> randomFiles = fileRepository.findRandomFiles(3);
        recommendations.addAll(randomFiles);
        
        // 중복 제거 및 최대 크기 제한
        return recommendations.stream()
                .distinct()
                .limit(RECOMMENDATION_SIZE)
                .collect(Collectors.toList());
    }

    private List<FileInfo> generateCandidates(User user) {
        List<FileInfo> candidates = new ArrayList<>();
        
        // 1. 사용자가 가장 많이 상호작용한 파일들
        List<Long> mostInteractedFileIds = interactionRepository.findMostInteractedFilesByUser(user.getId(), 10);
        candidates.addAll(fileRepository.findAllById(mostInteractedFileIds));
        
        // 2. 전체적으로 인기 있는 파일들
        List<Object[]> popularFiles = interactionRepository.findMostPopularFiles(10);
        List<Long> popularFileIds = popularFiles.stream()
                .map(obj -> (Long) obj[0])
                .collect(Collectors.toList());
        candidates.addAll(fileRepository.findAllById(popularFileIds));

        return candidates.stream().distinct().collect(Collectors.toList());
    }

    private List<FileInfo> rankCandidates(User user, List<FileInfo> candidates) {
        return candidates.stream()
                .map(file -> {
                    double score = calculateScore(user, file);
                    logRecommendation(user, file, score);
                    return Map.entry(file, score);
                })
                .sorted(Map.Entry.<FileInfo, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(RECOMMENDATION_SIZE)
                .collect(Collectors.toList());
    }

    private double calculateScore(User user, FileInfo file) {
        double score = 0.0;
        
        // 1. 파일의 전반적인 인기도
        List<UserInteraction> fileInteractions = interactionRepository.findByFile(file);
        score += fileInteractions.size() * 0.3;
        
        // 2. 사용자의 관심사와의 연관성
        List<UserInteraction> userInteractions = interactionRepository.findByUser(user);
        long similarInteractions = userInteractions.stream()
                .filter(ui -> ui.getFile().getTags().stream()
                        .anyMatch(tag -> file.getTags().contains(tag)))
                .count();
        score += similarInteractions * 0.4;
        
        // 3. 시간 가중치 (최근 파일에 더 높은 점수)
        LocalDateTime now = LocalDateTime.now();
        if (file.getLastWriteTime() != null) {
            long daysDifference = java.time.Duration.between(file.getLastWriteTime(), now).toDays();
            score += Math.max(0, 1.0 - (daysDifference / 30.0)) * 0.3;
        }
        
        return score;
    }

    private void logRecommendation(User user, FileInfo file, double score) {
        RecommendationLog log = new RecommendationLog();
        log.setUser(user);
        log.setFile(file);
        log.setWasClicked(false);
        log.setTimestamp(LocalDateTime.now());
        log.setScore(score);
        recommendationLogRepository.save(log);
    }
}