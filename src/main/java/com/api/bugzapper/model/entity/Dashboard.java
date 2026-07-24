package com.api.bugzapper.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Dashboard {
    private Integer companyCount;
    private Integer projectCount;
    private Integer taskCount;
    private Integer reportCount;
}
