package com.api.bugzapper.model.entity;


import com.api.bugzapper.model.dto.AppUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Report {
    private Integer reportId;
    private String description;
    private String location;
    private String problem;
    private String image;
    private LocalDateTime createdAt = LocalDateTime.now();
    private AppUserDTO userId;
    private Task taskId;
    private Phase phaseId;
}
