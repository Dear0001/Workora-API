package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhaseAttachmentRequest {
    @NotBlank(message = "Phase id must not be blank.")
    private Integer phaseId;
    @NotBlank(message = "Attachment must not be blank.")
    private String attachment;
}
