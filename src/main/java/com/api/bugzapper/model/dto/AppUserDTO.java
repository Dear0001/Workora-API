package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserDTO{
    private Integer userId;
    private String firstName;
    private String lastName;
    private String gender;
    private Date dob;
    private String email;
    private String avatar;
    private String bio;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<Map<String, Object>> experience;
    private boolean type;
    private boolean isVerified;
}
