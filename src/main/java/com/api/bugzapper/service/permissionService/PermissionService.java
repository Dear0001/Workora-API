package com.api.bugzapper.service.permissionService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.constant.RoleId;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.repository.ProjectRepository;
import com.api.bugzapper.repository.TaskRepository;
import com.api.bugzapper.repository.PhaseRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PermissionService handles all role-based access control checks for BugZapper
 * 
 * Role Hierarchy:
 * 1. OWNER/ADMIN - Can create projects, manage all recruitment
 * 2. PROJECT_MANAGER - Can see only their assigned projects and all tasks in them
 * 3. PHASE_LEAD - Can see phases they manage
 * 4. DEVELOPER - Can only see and edit tasks assigned to them
 * 5. BUG_HUNTER/TESTER - Limited view and submission rights
 */
@Service
@AllArgsConstructor
public class PermissionService {
    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);
    
    private final UserRoleRepository userRoleRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PhaseRepository phaseRepository;
    private final GetCurrentUser getCurrentUser;

    // ========================================
    // COMPANY-LEVEL PERMISSIONS
    // ========================================

    /**
     * Check if user is company owner/admin
     * Role ID: 1 = ADMIN/OWNER
     */
    public boolean isCompanyOwner(Integer userId, Integer companyId) {
        Integer roleId = userRoleRepository.findUserRoleByUserIdAndCompanyId(userId, companyId);
        return roleId != null && roleId.equals(RoleId.COMPANY_OWNER);
    }

    /**
     * Check if user is company owner (throws exception if not)
     */
    public void verifyCompanyOwner(Integer userId, Integer companyId) {
        if (!isCompanyOwner(userId, companyId)) {
            throw new CustomNotFoundException(
                "Access denied. Only company owner can perform this action."
            );
        }
    }

    /**
     * Get all companies where user is owner
     */
    public List<Integer> getCompaniesWhereUserIsOwner(Integer userId) {
        return userRoleRepository.findCompanyIdAsAdminByUserId(userId);
    }

    // ========================================
    // PROJECT-LEVEL PERMISSIONS
    // ========================================

    /**
     * Check if user is project manager for a specific project
     */
    public boolean isProjectManager(Integer userId, Integer projectId) {
        Integer pmId = userRoleRepository.getProjectManagerIdFromProject(projectId);
        return pmId != null && pmId.equals(userId);
    }

    /**
     * Check if user is project manager or owner for a specific project
     */
    public boolean isProjectManagerOrOwner(Integer userId, Integer projectId) {
        Integer companyId = projectRepository.getCompanyIdByProjectId(projectId);
        if (companyId == null) {
            return false;
        }

        if (isCompanyOwner(userId, companyId)) {
            return true;
        }

        return isProjectManager(userId, projectId);
    }

    /**
     * Verify user is project manager or owner (throws exception if not)
     */
    public void verifyProjectManagerOrOwner(Integer userId, Integer projectId) {
        if (!isProjectManagerOrOwner(userId, projectId)) {
            throw new CustomNotFoundException(
                "Access denied. Only company owner or project manager can perform this action."
            );
        }
    }

    /**
     * Check if user can access project
     * Owner can see all projects in their company
     * PM can see only their assigned projects
     */
    public boolean canAccessProject(Integer userId, Integer projectId) {
        Integer companyId = projectRepository.getCompanyIdByProjectId(projectId);
        if (companyId == null) {
            return false;
        }

        // Owner can access all projects in their company
        if (isCompanyOwner(userId, companyId)) {
            return true;
        }

        // PM can only access their assigned projects
        if (isProjectManager(userId, projectId)) {
            return true;
        }

        // Developers can access projects where they are invited to a phase
        Integer phaseAssignments = projectRepository.countPhaseAssignmentsInProject(userId, projectId);
        return phaseAssignments != null && phaseAssignments > 0;
    }

    /**
     * Verify user can access project (throws exception if not)
     */
    public void verifyProjectAccess(Integer userId, Integer projectId) {
        if (!canAccessProject(userId, projectId)) {
            throw new CustomNotFoundException(
                "Access denied. You do not have permission to access this project."
            );
        }
    }

    /**
     * Get all projects user can see
     */
    public List<Integer> getAccessibleProjects(Integer userId, Integer companyId) {
        if (isCompanyOwner(userId, companyId)) {
            return projectRepository.getProjectIdsByCompanyId(companyId);
        }

        java.util.Set<Integer> projectIds = new java.util.LinkedHashSet<>();
        List<Integer> pmProjects = projectRepository.getProjectIdsByProjectManagerId(userId, companyId);
        if (pmProjects != null) {
            projectIds.addAll(pmProjects);
        }
        List<Integer> memberProjects = projectRepository.getProjectIdsByPhaseMember(userId, companyId);
        if (memberProjects != null) {
            projectIds.addAll(memberProjects);
        }
        return new java.util.ArrayList<>(projectIds);
    }

    // ========================================
    // PHASE-LEVEL PERMISSIONS
    // ========================================

    /**
     * Check if user can access phase
     * Owner can see all phases
     * PM can see all phases in their projects
     * Developers can see only phases they're assigned to
     */
    public boolean canAccessPhase(Integer userId, Integer phaseId) {
        Integer projectId = phaseRepository.getProjectIdByPhaseId(phaseId);
        if (projectId == null) {
            return false;
        }

        Integer companyId = projectRepository.getCompanyIdByProjectId(projectId);
        if (companyId == null) {
            return false;
        }

        // Owner can access all phases
        if (isCompanyOwner(userId, companyId)) {
            return true;
        }

        // PM can access all phases in their project
        if (isProjectManager(userId, projectId)) {
            return true;
        }

        // Check if user is assigned to this phase
        Integer isInPhase = userRoleRepository.isUserInCompanyProjectPhase(userId, companyId, projectId, phaseId);
        return isInPhase > 0;
    }

    /**
     * Verify user can access phase (throws exception if not)
     */
    public void verifyPhaseAccess(Integer userId, Integer phaseId) {
        if (!canAccessPhase(userId, phaseId)) {
            throw new CustomNotFoundException(
                "Access denied. You do not have permission to access this phase."
            );
        }
    }

    /**
     * Get all phases user can see
     */
    public List<Integer> getAccessiblePhases(Integer userId, Integer projectId) {
        Integer companyId = projectRepository.getCompanyIdByProjectId(projectId);
        if (companyId == null) {
            return List.of();
        }

        if (isCompanyOwner(userId, companyId)) {
            // Owner can see all phases
            return phaseRepository.getPhaseIdsByProjectId(projectId);
        } else if (isProjectManager(userId, projectId)) {
            // PM can see all phases in their project
            return phaseRepository.getPhaseIdsByProjectId(projectId);
        } else {
            // Developers can see only their assigned phases
            return phaseRepository.getPhaseIdsByUserIdAndProjectId(userId, projectId);
        }
    }

    // ========================================
    // TASK-LEVEL PERMISSIONS
    // ========================================

    /**
     * Check if user can access task
     * Owner can see all tasks
     * PM can see all tasks in their projects
     * Developers can see only tasks assigned to them
     */
    public boolean canAccessTask(Integer userId, Integer taskId) {
        Integer projectId = taskRepository.getProjectIdByTaskId(taskId);
        if (projectId == null) {
            return false;
        }

        Integer phaseId = taskRepository.getPhaseIdByTaskId(taskId);
        if (phaseId == null) {
            return false;
        }

        Integer companyId = projectRepository.getCompanyIdByProjectId(projectId);
        if (companyId == null) {
            return false;
        }

        // Owner can see all tasks
        if (isCompanyOwner(userId, companyId)) {
            return true;
        }

        // PM can see all tasks in their project
        if (isProjectManager(userId, projectId)) {
            return true;
        }

        // Check if user is assigned to this task
        Integer isInTask = userRoleRepository.isUserExistInTask(userId, companyId, projectId, phaseId, taskId);
        return isInTask > 0;
    }

    /**
     * Verify user can access task (throws exception if not)
     */
    public void verifyTaskAccess(Integer userId, Integer taskId) {
        if (!canAccessTask(userId, taskId)) {
            throw new CustomNotFoundException(
                "Access denied. You do not have permission to access this task."
            );
        }
    }

    /**
     * Check if user can edit task status (only assignee or PM/Owner)
     */
    public boolean canEditTaskStatus(Integer userId, Integer taskId) {
        Integer projectId = taskRepository.getProjectIdByTaskId(taskId);
        if (projectId == null) {
            return false;
        }

        Integer companyId = projectRepository.getCompanyIdByProjectId(projectId);
        if (companyId == null) {
            return false;
        }

        // Owner and PM can edit task status
        if (isCompanyOwner(userId, companyId) || isProjectManager(userId, projectId)) {
            return true;
        }

        // Developer can only edit their own assigned tasks
        return canAccessTask(userId, taskId);
    }

    /**
     * Verify user can edit task status (throws exception if not)
     */
    public void verifyTaskStatusEdit(Integer userId, Integer taskId) {
        if (!canEditTaskStatus(userId, taskId)) {
            throw new CustomNotFoundException(
                "Access denied. You can only edit status for tasks assigned to you."
            );
        }
    }

    /**
     * Check if user can submit task (only assignee)
     */
    public boolean canSubmitTask(Integer userId, Integer taskId) {
        Integer projectId = taskRepository.getProjectIdByTaskId(taskId);
        if (projectId == null) {
            return false;
        }

        Integer phaseId = taskRepository.getPhaseIdByTaskId(taskId);
        if (phaseId == null) {
            return false;
        }

        Integer companyId = projectRepository.getCompanyIdByProjectId(projectId);
        if (companyId == null) {
            return false;
        }

        // Check if user is assigned to this task
        Integer isInTask = userRoleRepository.isUserExistInTask(userId, companyId, projectId, phaseId, taskId);
        return isInTask > 0;
    }

    /**
     * Verify user can submit task (throws exception if not)
     */
    public void verifyTaskSubmit(Integer userId, Integer taskId) {
        if (!canSubmitTask(userId, taskId)) {
            throw new CustomNotFoundException(
                "Access denied. You can only submit tasks assigned to you."
            );
        }
    }

    /**
     * Get all tasks user can see
     */
    public List<Integer> getAccessibleTasks(Integer userId, Integer projectId) {
        Integer companyId = projectRepository.getCompanyIdByProjectId(projectId);
        if (companyId == null) {
            return List.of();
        }

        if (isCompanyOwner(userId, companyId)) {
            // Owner can see all tasks
            return taskRepository.getTaskIdsByProjectId(projectId);
        } else if (isProjectManager(userId, projectId)) {
            // PM can see all tasks in their project
            return taskRepository.getTaskIdsByProjectId(projectId);
        } else {
            // Developers can see only their assigned tasks
            return taskRepository.getTaskIdsByUserIdAndProjectId(userId, projectId);
        }
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Get user's role name at company level
     */
    public String getUserRoleAtCompany(Integer userId, Integer companyId) {
        return userRoleRepository.getRoleNameForUserInCompany(userId, companyId);
    }

    /**
     * Get current authenticated user
     */
    public AppUser getCurrentAuthenticatedUser() {
        AppUser user = getCurrentUser.getCurrentUser();
        if (user == null) {
            throw new CustomNotFoundException("Current user not authenticated.");
        }
        return user;
    }

    /**
     * Check if user has any of the specified role IDs at company level
     */
    public boolean hasAnyRole(Integer userId, Integer companyId, Integer... roleIds) {
        Integer userRole = userRoleRepository.findUserRoleByUserIdAndCompanyId(userId, companyId);
        if (userRole == null) {
            return false;
        }
        for (Integer roleId : roleIds) {
            if (userRole.equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is owner or project manager
     */
    public boolean isOwnerOrProjectManager(Integer userId, Integer companyId) {
        return hasAnyRole(userId, companyId, 1, 2);
    }

    /**
     * Check if user is a RECRUITER (role_id 5) in the company — manages recruitment posts/applications.
     */
    public boolean isRecruiter(Integer userId, Integer companyId) {
        return hasAnyRole(userId, companyId, RoleId.RECRUITER);
    }

    /**
     * Check if user is a PHASE_LEAD (role_id 3) assigned to this specific phase.
     */
    public boolean isPhaseLead(Integer userId, Integer phaseId) {
        Integer roleId = userRoleRepository.getRoleIdByUserIdInPhaseId(userId, phaseId);
        return roleId != null && roleId.equals(RoleId.PHASE_LEAD);
    }
}
