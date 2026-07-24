package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TopCompany {
    private Integer companyId;
    private String companyName;
    private String companyProfile;
    private Double totalRateValue;
    private String description;
}
