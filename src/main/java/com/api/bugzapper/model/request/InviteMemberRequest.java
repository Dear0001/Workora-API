package com.api.bugzapper.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Company-owner-only "invite an existing user with a chosen role" request.
 * roleId must be one of PROJECT_MANAGER(2)/PHASE_LEAD(3)/DEVELOPER(4)/RECRUITER(5)
 * — COMPANY_OWNER and BUG_HUNTER can never be granted through this endpoint.
 * projectId/phaseId are optional: omit both for a company-level role, set
 * projectId for a project-scoped role, set both for a phase-scoped role.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InviteMemberRequest {
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotNull(message = "Company Id cannot be null")
    private Integer companyId;

    @NotNull(message = "Role Id cannot be null")
    private Integer roleId;

    private Integer projectId;

    private Integer phaseId;
}
