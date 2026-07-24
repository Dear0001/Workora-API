package com.api.bugzapper.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserRole {
    private Integer userRoleId;
    private Integer roleId;
    private Integer userId;
    private Integer companyId;
    private Integer taskId;
    private Integer phasesId;

}
