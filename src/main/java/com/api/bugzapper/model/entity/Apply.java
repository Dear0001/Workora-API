package com.api.bugzapper.model.entity;

import com.api.bugzapper.model.dto.AppUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Apply {
    private Integer applyId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private List<Map<String, Object>> applyData;
    private AppUserDTO userId;
    private Integer postRecruitmentId;
    private String status;
    private String role;
}
