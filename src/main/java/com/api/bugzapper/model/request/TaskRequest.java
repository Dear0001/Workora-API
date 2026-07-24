package com.api.bugzapper.model.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TaskRequest {
    @NotBlank(message = "Name must not be blank.")
    private String taskName;

    @NotBlank(message = "Title must not be blank.")
    private String title;

    @NotBlank(message = "Description must not be blank.")
    private String taskDescription;

    @NotBlank(message = "Status must not be blank.")
    private String taskStatus;

    @NotNull(message = "Due date must not be null.")
    private Date dueDate;

    private List<String> attachment;

    @NotNull(message = "Phase id must not be null.")
    private Integer phaseId;

    private List<Integer> userIds;
}
