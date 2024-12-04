package com.nas.manager.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user_interactions")
public class UserInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private FileInfo file;

    @Enumerated(EnumType.STRING)
    private InteractionType type;

    private LocalDateTime timestamp;
    private Integer engagementLevel;

    public enum InteractionType {
        VIEW(1),
        DOWNLOAD(2),
        LIKE(5);

        private final int weight;

        InteractionType(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }
}