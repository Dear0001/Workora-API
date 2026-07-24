package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ApplyRequest {
    private LocalDate createdAt;
    @NotBlank(message = "Apply data must not be blank.")
    private List<Map<String, Object>> applyData;
    @NotBlank(message = "Post recruitment id must not be blank.")
    private Integer postRecruitmentId;
}
