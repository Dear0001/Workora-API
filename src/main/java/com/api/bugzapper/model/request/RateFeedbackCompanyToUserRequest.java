package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateFeedbackCompanyToUserRequest {
    @NotBlank(message = "Feedback message must not be blank.")
    private String feedback;
    @NotNull(message = "Rate value must not be null.")
    private Integer rateValue;
    @NotNull(message = "Company id must not be null.")
    private Integer companyId;
    @NotNull(message = "User id must not be null.")
    private Integer userId;
}
