package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostRecruitmentStatusRequest {
    /** OPEN or CLOSED. */
    @NotBlank(message = "Status must not be blank.")
    private String status;
}
