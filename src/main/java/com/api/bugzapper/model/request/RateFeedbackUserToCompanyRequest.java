package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateFeedbackUserToCompanyRequest {
    @NotBlank(message = "Feedback message must not be blank.")
    private String feedback;
    private Integer rateValue;
    @NotNull(message = "Company id must not be null.")
    private Integer companyId;

}
