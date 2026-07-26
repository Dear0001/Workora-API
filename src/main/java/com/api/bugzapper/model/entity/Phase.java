package com.api.bugzapper.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Phase {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private String image;
    private String link;
    private Boolean isPrivate;
    private Project project;
    /** OPEN (default) or CLOSED — a CLOSED public phase no longer accepts new bug reports. */
    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
