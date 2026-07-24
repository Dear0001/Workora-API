package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserIdsRequest {
    @NotNull(message = "User id must not be null.")
    @NumberFormat
    private List<Integer> userIds;
}
