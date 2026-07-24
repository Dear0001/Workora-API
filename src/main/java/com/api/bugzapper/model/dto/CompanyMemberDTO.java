package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyMemberDTO {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String roleName;
    private String avatar;
}
