package com.api.bugzapper.model.dto;

import com.api.bugzapper.model.entity.Phase;
import com.api.bugzapper.model.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportTaskDTO {
    private Integer reportId;
    private String description;
    private String location;
    private String problem;
    private LocalDateTime createdAt = LocalDateTime.now();
    private AppUserDTO userId;
    private Task taskId;
}
