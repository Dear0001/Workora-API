package com.api.bugzapper.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetReportRequest {
    private Integer companyId;
    private Integer projectId;

}
