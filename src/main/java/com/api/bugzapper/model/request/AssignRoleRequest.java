package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AssignRoleRequest {
    @NotNull(message = "User Id cannot be null")
    private List<Integer> userIds;

    @NotNull(message = "Role Id cannot be null")
    private List<Integer> roleId;
}
