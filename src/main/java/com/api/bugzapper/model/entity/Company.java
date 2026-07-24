package com.api.bugzapper.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Company {
    private Integer companyId;
    private String companyName;
    private String email;
    private String phone;
    private String address;
    private String description;
    private String companyProfile;
    private String coverImage;
    private String inviteCode;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
