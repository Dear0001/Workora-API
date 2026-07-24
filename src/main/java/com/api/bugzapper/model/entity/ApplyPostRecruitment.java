package com.api.bugzapper.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApplyPostRecruitment {
    private Integer applyPostRecruitmentId;
    private Integer postRecruitmentId;
    private Integer applyId;
    private LocalDateTime createdAt = LocalDateTime.now();
}
