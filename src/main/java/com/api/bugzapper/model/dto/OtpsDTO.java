package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpsDTO {
    private String optCode;
    private LocalDateTime issuedDate;
    private LocalDateTime expiration;
    private boolean verify;
    private Integer userId;
}
