package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskSubmitRequest {
    @NotNull(message = "Task id must not be null.")
    private Integer taskId;
    @NotBlank(message = "Attachment must not be blank.")
    private String attachment;
}
