package com.nas.manager.model;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "files")
public class FileInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String path;
    private String thumbnail;
    private String description;
    private LocalDateTime lastAccessed;
    private Integer accessCount = 0;
    private Integer recommendations = 0;
    private Boolean bookmarked = false;
    private Integer bookmarkCount = 0;

    @ElementCollection
    private Set<String> tags;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}