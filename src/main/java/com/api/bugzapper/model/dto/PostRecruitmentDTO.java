package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostRecruitmentDTO {
    private Integer postRecruitmentId;
    private String description;
    private String title;
    private String fee;
    private String image;
    private String applicationTitle;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private List<Map<String, Object>> postData;
    private Integer userRoleId;
}
