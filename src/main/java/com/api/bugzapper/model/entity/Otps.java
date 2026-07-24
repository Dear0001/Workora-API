package com.api.bugzapper.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otps {
    private Integer optId;
    private String optCode;
    private LocalDateTime issuedDate;
    private LocalDateTime expiration;
    private String verify;
    private Integer userId;
}
