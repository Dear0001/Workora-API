package com.api.bugzapper.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportsDTO {
    private Integer reportId;
    private String companyName;
    private String location;
    private String problem;
    private LocalDateTime createdAt = LocalDateTime.now();
    private Integer userId;
    private String firstName;
    private String lastName;
    private String avatar;
    private String email;
    private Integer phaseId;
    private Double price;
    private String description;
}
