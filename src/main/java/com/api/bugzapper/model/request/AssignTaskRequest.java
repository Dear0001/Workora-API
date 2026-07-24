package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AssignTaskRequest {
    @NotNull(message = "User Ids cannot be null")
    private List<Integer> userIds;

    @NotNull(message = "Company Id cannot be null")
    private Integer companyId;

    @NotNull(message = "Project Id cannot be null")
    private Integer projectId;

    @NotNull(message = "Project Id cannot be null")
    private Integer phaseId;

    @NotNull(message = "Task Id cannot be null")
    private Integer taskId;
}
