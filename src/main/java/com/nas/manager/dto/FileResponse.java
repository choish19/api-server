package com.nas.manager.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class FileResponse {
    private Long id;
    private String name;
    private String type;
    private String url;
    private String thumbnail;
    private String description;
    private LocalDateTime lastAccessed;
    private Integer accessCount;
    private Integer recommendations;
    private Boolean bookmarked;
    private Set<String> tags;
}