package com.api.bugzapper.model.entity;

import com.api.bugzapper.model.dto.AppUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RateFeedbackToCompany {
    private Integer totalCompanyRated;
    private Double totalRateValue;
}
