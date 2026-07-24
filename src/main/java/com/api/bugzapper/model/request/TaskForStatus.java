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
public class TaskForStatus {
    @NotBlank(message = "Status must not be blank.")
    @NotNull(message = "Status must not be null.")
    private String taskStatus;
}
