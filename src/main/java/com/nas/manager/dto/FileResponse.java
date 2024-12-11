package com.nas.manager.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileResponse {
    private Long id;
    private String name;
    private String type;
    private String url;
    private String path;
    private String thumbnail;
    private String description;
    private LocalDateTime lastAccessed;
    private Integer accessCount;
    private Integer recommendations;
    private Boolean bookmarked;
    private Integer bookmarkCount;
    private Set<String> tags;
    private LocalDateTime watchedAt;
}