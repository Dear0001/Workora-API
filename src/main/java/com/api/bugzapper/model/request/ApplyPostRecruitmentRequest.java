package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ApplyPostRecruitmentRequest {
    @NotNull(message = "Post recruitment id must not be null.")
    @NumberFormat
    private Integer postRecruitmentId;
    @NotNull(message = "Apply id must not be null.")
    @NumberFormat
    private Integer applyId;
}
