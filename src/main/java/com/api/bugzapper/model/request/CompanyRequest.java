package com.api.bugzapper.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyRequest {
    @NotBlank(message = "Name must not be blank.")
    private String name;
    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Invalid Email")
    private String email;
    @NotBlank(message = "Phone must not be blank.")
    private String phone;
    @NotBlank(message = "Address must not be blank.")
    private String address;
    @NotBlank(message = "Description must not be blank.")
    private String description;
    @NotBlank(message = "Profile Image must not be blank.")
    private String profileImage;
    @NotBlank(message = "Cover Image must not be blank.")
    private String coverImage;
}
