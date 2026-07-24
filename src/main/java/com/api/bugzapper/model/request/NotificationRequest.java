package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationRequest {
    @NotBlank(message = "Title must not be blank.")
    private String title;
    @NotBlank(message = "Description must not be blank.")
    private String description;
    @NotBlank(message = "Status must not be blank.")
    private String status;
    @NotNull(message = "Redirect id must not be null.")
    private Integer redirectId;
    @NotNull(message = "User id must not be null.")
    private Integer userId;
    @NotNull(message = "User role id must not be null.")
    private Integer userRoleId;
}
