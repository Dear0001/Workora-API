package com.api.bugzapper.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HistoryDTO {
    private Integer companyId;
    private String companyName;
    private String companyProfileImage;
    private Integer phaseId;
    private String phaseName;
    private String phaseDescription;
    private Integer projectId;
    private String rateFeedbackId;
    private String feedback;
    private String rateValue;
    private LocalDateTime createdAt = LocalDateTime.now();
}
