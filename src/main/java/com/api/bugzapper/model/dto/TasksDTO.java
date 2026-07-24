package com.api.bugzapper.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TasksDTO {
    private int taskId;
    private String taskName;
    private String taskStatus;
    private String taskDescription;
    private LocalDateTime createdAt = LocalDateTime.now();
    private Date dueDate;
    private String attachment;
    private Integer projectId;
    private Integer phaseId;
    private Integer companyId;
    private String companyName;
}
