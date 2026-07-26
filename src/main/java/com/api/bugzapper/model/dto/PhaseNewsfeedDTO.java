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
public class PhaseNewsfeedDTO {
    private Integer phaseId;
    private String phaseName;
    private String description;
    private String image;
    private String link;
    private Boolean isPrivate;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private String type;
    private Double price;
    private Integer companyId;
    private String companyName;
    private String companyProfile;
    private String companyDescription;
    /** OPEN (default) or CLOSED — a CLOSED public phase no longer accepts new bug reports. */
    private String status;
    /** Not persisted — computed per-request: can the current user edit/close this phase? */
    private Boolean canManage;
}
