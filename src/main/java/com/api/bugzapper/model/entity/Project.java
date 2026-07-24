package com.api.bugzapper.model.entity;

import com.api.bugzapper.model.dto.AppUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Project {
    private Integer projectId;
    private String projectName;
    private String description;
    private Company company;
    private AppUserDTO user;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
