package com.api.bugzapper.model.dto;

import com.api.bugzapper.model.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RateFeedbackFromCompanyToUser {
    private Integer id;
    private String feedback;
    private Integer rateValue;
    private Boolean type;
    private Company company;
    private LocalDateTime createdAt = LocalDateTime.now();
}
