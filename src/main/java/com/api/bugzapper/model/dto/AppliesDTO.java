package com.api.bugzapper.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppliesDTO {
    private Integer applyId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private Integer userId;
    private String companyName;
    private String title;
    private String firstName;
    private String lastName;
    private String status;
    private String viewCondition;
}
