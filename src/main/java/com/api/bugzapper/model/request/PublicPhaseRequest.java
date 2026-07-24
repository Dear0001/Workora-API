package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicPhaseRequest {
    @NotBlank(message = "Name must not be blank.")
    private String name;
    @NotBlank(message = "Description must not be blank.")
    private String description;
    private Double price;
    @NotBlank(message = "Image must not be blank.")
    private String image;
    @NotNull(message = "Project id must not be null.")
    private Integer projectId;
}
