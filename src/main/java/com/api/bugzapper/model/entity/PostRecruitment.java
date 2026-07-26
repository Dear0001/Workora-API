package com.api.bugzapper.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostRecruitment {
    private Integer postRecruitmentId;
    private String description;
    private String title;
    private String fee;
    private String image;
    private String applicationTitle;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<Map<String, Object>> postData;
    private List<Integer> applies;
    private Integer companyId;
    private String companyName;
    private String companyProfile;
    private String companyDescription;
    /** Optional — scopes the post to a specific project. Null means company-wide. */
    private Integer projectId;
    private String projectName;
    /** Role granted to an applicant when their application is accepted. Defaults to DEVELOPER if null. */
    private Integer roleId;
    /** OPEN (default) or CLOSED — a CLOSED post no longer accepts new applications. */
    private String status;
    /** Not persisted — computed per-request: can the current user edit/close this post? */
    private Boolean canManage;
}
