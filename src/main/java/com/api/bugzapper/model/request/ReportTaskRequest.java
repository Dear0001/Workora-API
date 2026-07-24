package com.api.bugzapper.model.request;

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
public class ReportTaskRequest {
    @NotBlank(message = "Description must not be blank.")
    private String description;
    @NotBlank(message = "Location must not be blank.")
    private String location;
    @NotBlank(message = "Problem must not be blank.")
    private String problem;
    @NotNull(message = "User id must not be null.")
    private Integer userId;
    @NotNull(message = "Task id must not null.")
    private Integer taskId;
}
