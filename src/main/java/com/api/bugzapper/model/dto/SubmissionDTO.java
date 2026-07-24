package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubmissionDTO {
    private Integer taskId;
    private String taskName;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String attachment;
    private String avatar;
}
