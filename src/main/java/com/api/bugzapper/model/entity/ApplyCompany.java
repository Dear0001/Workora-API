package com.api.bugzapper.model.entity;

import com.api.bugzapper.model.dto.AppUserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplyCompany {
    private Integer applyId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private List<Map<String, Object>> applyData;
    private String title;
    private String firstName;
    private String lastName;
    private String companyName;
    private String status;
    private String role;
}
