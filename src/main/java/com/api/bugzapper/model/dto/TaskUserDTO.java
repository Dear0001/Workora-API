package com.api.bugzapper.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskUserDTO {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String gender;
    private String avatar;
    private Integer roleId;
    private String roleName;
}
