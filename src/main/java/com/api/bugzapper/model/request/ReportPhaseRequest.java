package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReportPhaseRequest {
    @NotBlank(message = "Description must not be blank.")
    private String description;
    private String location;
    @NotBlank(message = "Problem must not be blank.")
    private String problem;
    @NotNull(message = "Phase id must not be null.")
    private Integer phaseId;
    private MultipartFile image;
}
