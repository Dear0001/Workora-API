package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostRecruitmentRequestForUpdate {
    @NotBlank(message = "Description must not be blank.")
    private String description;
    @NotBlank(message = "Title must not be blank.")
    private String title;
    @NotBlank(message = "Fee must not be blank.")
    private String fee;
    @NotBlank(message = "Image must not be blank.")
    private String image;
    @NotBlank(message = "applicationTitle must not be blank.")
    private String applicationTitle;
    private List<Map<String, Object>> postData;
    private Integer roleId;
    /** OPEN or CLOSED. Ignored/left unchanged if omitted. */
    private String status;
    /** Optional — scopes the post to a specific project. Ignored/left unchanged if omitted. */
    private Integer projectId;
}
