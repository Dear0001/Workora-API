package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostRecruitmentRequest {
    @NotBlank(message = "Description must not be blank.")
    private String description;
    @NotBlank(message = "Title must not be blank.")
    private String title;
    @NotBlank(message = "Fee must not be blank.")
    private String fee;
    @NotBlank(message = "Image must not be blank.")
    private String image;
    @NotNull(message = "CompanyId must not be null.")
    private Integer companyId;
    @NotBlank(message = "applicationTitle must not be blank.")
    private String applicationTitle;
    private List<Map<String, Object>> postData;
    /** Role an accepted applicant receives (PROJECT_MANAGER/PHASE_LEAD/DEVELOPER/RECRUITER). Defaults to DEVELOPER if omitted. */
    private Integer roleId;
}
