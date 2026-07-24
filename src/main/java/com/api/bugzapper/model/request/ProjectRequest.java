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
public class ProjectRequest {
    @NotBlank(message = "Name must not be blank.")
    private String projectName;
    @NotBlank(message = "Description must not be blank.")
    private String description;
    @NotNull(message = "Company must not be null.")
    private Integer companyId;
    private Integer projectMangerId;
}
