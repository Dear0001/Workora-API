package com.api.bugzapper.model.entity;

import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.dto.TaskUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Task {
    private int taskId;
    private String taskName;
    private String title;
    private String taskDescription;
    private String taskStatus;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Date dueDate;
    private String attachment;
    /** Denormalized from phases.project_id for reporting and indexing; kept in sync on insert/update. */
    private Integer projectId;
    private Phase phaseId;
    private List<TaskUserDTO> users;
}
