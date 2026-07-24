package com.api.bugzapper.model.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Email must be not blank")
    @NotNull(message = "Email must be not null")
    @NotEmpty(message = "Email must be not empty")
    @Email(message = "Invalid Email")
    private String email;

//    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$")
    private String password;
}
