package com.api.bugzapper.model.entity;

import com.api.bugzapper.model.dto.AppUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RateFeedbackFromUser {
    private Integer id;
    private String feedback;
    private Integer rateValue;
    private Boolean type;
    private AppUserDTO user;
    private LocalDateTime createdAt = LocalDateTime.now();
}
