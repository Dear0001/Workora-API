package com.api.bugzapper.model.response;

import com.api.bugzapper.model.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectResponse {
    private Integer projectId;
    private String projectName;
    private String description;
    private Company companyId;
//    private LocalDateTime createAt;
//    private LocalDateTime updatedAt;
//    private LocalDateTime deletedAt;
}
