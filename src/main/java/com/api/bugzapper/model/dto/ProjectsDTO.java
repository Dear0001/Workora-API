package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectsDTO {
    private Integer projectId;
    private String projectName;
    private String description;
    private String companyName;
    private LocalDateTime createdAt = LocalDateTime.now();
}
