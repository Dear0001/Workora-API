package com.api.bugzapper.service.dashboardService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.*;
import com.api.bugzapper.model.entity.*;
import com.api.bugzapper.repository.*;
import com.api.bugzapper.service.companyService.CompanyService;
import com.api.bugzapper.service.notificationService.NotificationService;
import com.api.bugzapper.service.phaseService.PhaseService;
import com.api.bugzapper.service.projectService.ProjectService;
import com.api.bugzapper.service.permissionService.PermissionService;
import com.api.bugzapper.service.userService.AppUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final AppUserService appUserService;
    private final GetCurrentUser currentUser;
    private final NotificationService notificationService;
    private final UserRoleRepository userRoleRepository;
    private final PhaseService phaseService;
    private final CompanyService companyService;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final PhaseRepository phaseRepository;
    private final TaskRepository taskRepository;
    private final PermissionService permissionService;

    public DashboardService(
            DashboardRepository dashboardRepository,
            AppUserService appUserService,
            GetCurrentUser getCurrentUser,
            NotificationService notificationService,
            UserRoleRepository userRoleRepository,
            PhaseService phaseService,
            CompanyService companyService,
            ProjectRepository projectRepository,
            ProjectService projectService,
            PhaseRepository phaseRepository,
            TaskRepository taskRepository,
            PermissionService permissionService) {
        this.dashboardRepository = dashboardRepository;
        this.appUserService = appUserService;
        this.currentUser = getCurrentUser;
        this.notificationService = notificationService;
        this.userRoleRepository = userRoleRepository;
        this.phaseService = phaseService;
        this.companyService = companyService;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.phaseRepository = phaseRepository;
        this.taskRepository = taskRepository;
        this.permissionService = permissionService;
    }

    public Dashboard getAllCountByUserId() {
        Integer userId = currentUser.getCurrentUser().getUserId();
        appUserService.getUserById(userId);

        return dashboardRepository.getAllCountByUserId(userId);
    }

    public List<CompaniesDTO> getAllCompanyByUserId(Integer offset, Integer limit) {
        Integer userId = currentUser.getCurrentUser().getUserId();
        appUserService.getUserById(userId);
        return dashboardRepository.getAllCompanyByUserId(offset, limit, userId);
    }

    public List<CompaniesDTO> getAllOwnCompanyByUserId(Integer offset, Integer limit) {
        Integer userId = currentUser.getCurrentUser().getUserId();
        appUserService.getUserById(userId);
        return dashboardRepository.getAllOwnCompanyByUserId(offset, limit, userId);
    }

    public List<ProjectsDTO> getAllProjectsByUserId(Integer offset, Integer limit) {
        Integer userId = currentUser.getCurrentUser().getUserId();
        appUserService.getUserById(userId);
        return dashboardRepository.getAllProjectByUserId(offset, limit, userId);
    }

    public List<TasksDTO> getAllTasksByUserId(Integer offset, Integer limit) {
        Integer userId = currentUser.getCurrentUser().getUserId();
        appUserService.getUserById(userId);

        return dashboardRepository.getAllTasksByUserId(offset, limit, userId);
    }

    public List<ReportsDTO> getAllReportsByUserId(Integer offset, Integer limit) {
        Integer userId = currentUser.getCurrentUser().getUserId();
        appUserService.getUserById(userId);
        System.out.println("current user is " + userId);
        List<Integer> companyIds = userRoleRepository.findCompanyIdAsOwnerOrPmByUserId(userId);
        if (companyIds.isEmpty()) {
            throw new CustomNotFoundException("You have no company as admin or project manager");
        }
        List<ReportsDTO> reportList = new ArrayList<>();

        for (Integer companyId : companyIds) {
            System.out.println("this is company id " + companyId);
            List<ReportsDTO> reports = dashboardRepository.getAllReportsByUserId(offset, limit, companyId);
            reportList.addAll(reports);
        }

        return reportList;
    }

    public List<AppliesDTO> getAllApplyByUserIdAndCompanyId(Integer offset, Integer limit) {
        Integer userId = currentUser.getCurrentUser().getUserId();
        appUserService.getUserById(userId);

        List<Integer> companyIds = userRoleRepository.findCompanyIdAsOwnerOrPmByUserId(userId);
        if (companyIds == null || companyIds.isEmpty()) {
            throw new CustomNotFoundException("You are not authorized to view company applications.");
        }

        List<AppliesDTO> appliesDTOs = new ArrayList<>();
        for (Integer companyId : companyIds) {
            List<AppliesDTO> appliesDTOList = dashboardRepository.getAllApplyByCompanyId(offset, limit, companyId);
            appliesDTOs.addAll(appliesDTOList);
        }
        appliesDTOs.sort((a, b) -> b.getApplyId().compareTo(a.getApplyId()));
        return appliesDTOs;
    }

    public List<NotificationDTO> getAllNotificationByUserId(Integer offset, Integer limit) {
        AppUser currenUser = currentUser.getCurrentUser();

        List<Integer> userRoleIds = userRoleRepository.findUserRoleIdAsListByUserId(currenUser.getUserId());

        List<NotificationDTO> notifications = new ArrayList<>();

        for (Integer userRoleId : userRoleIds) {
            List<NotificationDTO> userNotifications = notificationService.getAllNotificationsByUserRoleId(userRoleId);
            notifications.addAll(userNotifications);
        }

        notifications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        Integer startIndex = (offset - 1) * limit;
        Integer endIndex = Math.min(startIndex + limit, notifications.size());
        if (startIndex >= notifications.size()) {
            return List.of();
        }
        return notifications.subList(startIndex, endIndex);
    }

    public void updateNotificationStatus(Integer id, Boolean isRead) {
        notificationService.updateNotificationStatus(id, isRead);
    }

    public CountTaskStatusDTO countTaskStatusByPhaseId(Integer phaseId) {
        AppUser user = currentUser.getCurrentUser();
        permissionService.verifyPhaseAccess(user.getUserId(), phaseId);
        return dashboardRepository.countTaskStatusByPhaseId(phaseId);
    }

    public List<CompaniesDTO> getAllCompany(Integer offset, Integer limit) {
        Integer userId = currentUser.getCurrentUser().getUserId();
        return dashboardRepository.getAllCompany(offset, limit, userId);
    }

    public List<Project> getAllProjectByCompanyId(Integer companyId) {
        AppUser currentUserEntity = currentUser.getCurrentUser();
        companyService.getCompanyById(companyId);

        List<Integer> accessibleProjectIds = permissionService.getAccessibleProjects(
                currentUserEntity.getUserId(), companyId);
        return projectService.getProjectsByIds(accessibleProjectIds);
    }

    public List<PhaseDTO> getAllPhaseByProjectId(Integer companyId, Integer projectId) {
        AppUser currentUserEntity = currentUser.getCurrentUser();
        companyService.getCompanyById(companyId);
        permissionService.verifyProjectAccess(currentUserEntity.getUserId(), projectId);

        if (permissionService.isProjectManagerOrOwner(currentUserEntity.getUserId(), projectId)) {
            return phaseRepository.getAllPhaseDTOByProjectId(projectId);
        }

        return phaseRepository.getAllPhaseByProjectIdAndUserId(
                currentUserEntity.getUserId(), companyId, projectId);
    }

    public List<TasksDTO> getAllTaskByPhaseId(Integer companyId, Integer projectId, Integer phaseId) {
        AppUser currentUserEntity = currentUser.getCurrentUser();
        companyService.getCompanyById(companyId);
        permissionService.verifyPhaseAccess(currentUserEntity.getUserId(), phaseId);

        if (permissionService.isProjectManagerOrOwner(currentUserEntity.getUserId(), projectId)) {
            return taskRepository.getAllTasksDTOByPhaseId(phaseId);
        }

        return taskRepository.getAllTaskByPhaseIdOfCurrentUser(
                currentUserEntity.getUserId(), companyId, projectId, phaseId);
    }
}
