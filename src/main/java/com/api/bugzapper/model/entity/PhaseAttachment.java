package com.api.bugzapper.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PhaseAttachment {
    private Integer id;
    private Phase phase;
    private String attachment;
}
