package com.api.bugzapper.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class TaskMemberDTO {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String roleName;
    private String avatar;
}
