package com.api.bugzapper.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NotificationDTO {
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
    private Integer userId;
    private Integer userRoleId;
    private String avatar;
    private Boolean isRead;
}
