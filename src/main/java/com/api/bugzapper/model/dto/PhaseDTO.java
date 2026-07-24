package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Timestamp;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PhaseDTO {
    private Integer phaseId;
    private String phaseName;
    private String description;
    private Double price;
    private String image;
    private String link;
    private Boolean isPrivate;
    private Integer projectId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private Integer companyId;
}
