package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CountTaskStatusDTO {
    private Integer phaseId;
    private String phaseName;
    private String notYet;
    private String onProgress;
    private String completed;
}
