package com.api.bugzapper.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Notification {
    private int notificationId;
    private String title;
    private String description;
    private String status;
    private Integer companyId;
    private Integer projectId;
    private Integer phaseId;
    private Integer taskId;
    private Integer reportId;
    private Integer applyId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private Boolean isRead;
    private LocalDateTime deletedAt;
    private Integer userId;
    private Integer userRoleId;
}
