package com.api.bugzapper.service.projectService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.configuration.PushNotificationOptions;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.entity.Company;
import com.api.bugzapper.model.entity.Notification;
import com.api.bugzapper.model.entity.Project;
import com.api.bugzapper.model.request.ProjectRequest;
import com.api.bugzapper.repository.CompanyRepository;
import com.api.bugzapper.repository.ProjectRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.service.companyService.CompanyService;
import com.api.bugzapper.service.notificationService.NotificationService;
import com.api.bugzapper.service.userService.AppUserService;
import com.api.bugzapper.util.EmailUtil;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProjectService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;
    private final CompanyService companyService;
    private final GetCurrentUser getCurrentUser;
    private final UserRoleRepository userRoleRepository;
    private final AppUserService appUserService;
    private final NotificationService notificationService;
    private final EmailUtil emailUtil;

    public ProjectService(ProjectRepository projectRepository, CompanyService companyService, GetCurrentUser getCurrentUser, UserRoleRepository userRoleRepository, AppUserService appUserService, NotificationService notificationService, EmailUtil emailUtil) {
        this.projectRepository = projectRepository;
        this.companyService = companyService;
        this.getCurrentUser = getCurrentUser;
        this.userRoleRepository = userRoleRepository;
        this.appUserService = appUserService;
        this.notificationService = notificationService;
        this.emailUtil = emailUtil;
    }
    public Project createProject(ProjectRequest projectRequest) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        System.out.println("currentUser = " + currentUser);

        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found in the repository.");
        }

        Integer isAdmin = companyService.isOwnerOfCompany(currentUser.getUserId(), projectRequest.getCompanyId());
        System.out.println("is admin: " + isAdmin);

        if (isAdmin < 1) {
            throw new CustomNotFoundException("Current user is not the owner of the company.");
        }

        if (projectRequest.getProjectMangerId() == null) {
            throw new CustomNotFoundException("Please select a project manager for this project.");
        }

        if (projectRequest.getDescription() == null || projectRequest.getDescription().isBlank()) {
            projectRequest.setDescription("No description");
        }

        AppUserDTO appUser = appUserService.getUserDtoById(projectRequest.getProjectMangerId());
        Integer isInCompany = userRoleRepository.isUserInCompany(appUser.getUserId(), projectRequest.getCompanyId());
        if (isInCompany == 0) {
            throw new CustomNotFoundException("User is not in a company id : " + projectRequest.getCompanyId());
        }

        Company company = companyService.getCompanyById(projectRequest.getCompanyId());
        Project createdProject = projectRepository.createProject(projectRequest);

        Integer isInProject = userRoleRepository.isInProjectInCompany(appUser.getUserId(), company.getCompanyId());
        if (isInProject < 1) {
            userRoleRepository.insertProjectManagerToUserRole(appUser.getUserId(), company.getCompanyId(), createdProject.getProjectId());
        }else{
            userRoleRepository.updateProjectManagerToUserRole(appUser.getUserId(), company.getCompanyId(), createdProject.getProjectId());
        }

        Integer roleId = userRoleRepository.findUserRoleByUserIdAndCompanyId(currentUser.getUserId(), projectRequest.getCompanyId());
        Integer projectId = userRoleRepository.checkProjectIdInUserRoleByUserIdAndCompanyId(currentUser.getUserId(), company.getCompanyId());
        if (projectId == null) {
            userRoleRepository.updateInsertProjectIdAndCompanyIdIntoUserRole(currentUser.getUserId(), projectRequest.getCompanyId(), createdProject.getProjectId());
            return createdProject;
        }

        String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has promote you to project manager in a project";

        Integer userRoleId = userRoleRepository.findUserRoleIdByUserIdAndCompanyId(projectRequest.getProjectMangerId(), company.getCompanyId());
        Notification notification = Notification.builder()
                .title("Assign role notification")
                .description(applicantName)
                .status("project")
                .companyId(company.getCompanyId())
                .projectId(createdProject.getProjectId())
                .userId(currentUser.getUserId()) // This id is the project owner
                .userRoleId(userRoleId) // This is the receiver
                .build();

        // Push notification to post owner
        PushNotificationOptions.sendMessageToUser(notification.getDescription(), projectRequest.getProjectMangerId(), notification.getTitle());
        try {
            emailUtil.sendNotificationToEmail(currentUser.getEmail(), applicantName, "assign_role");
        } catch (MessagingException e) {
            log.warn("Project created but assign-role email failed: {}", e.getMessage());
        }

        userRoleRepository.insertProjectIdAndCompanyIdIntoUserRole(currentUser.getUserId(), roleId, projectRequest.getCompanyId(), createdProject.getProjectId());
        createdProject.setUser(appUserService.getUserDtoById(projectRequest.getProjectMangerId()));
        return createdProject;
    }
    public Project getProjectByIdForPrivatePhase(Integer id) {
        Project response = projectRepository.getProjectById(id);
        if (response == null) {
            throw new CustomNotFoundException("Project with id " + id + " not found.");
        }
        Integer projectMangerId = userRoleRepository.getProjectManagerIdFromProject(id);
        response.setUser(appUserService.getUserDtoById(projectMangerId));
        return response;
    }
    public Project getProjectById(Integer id) {
        Project response = projectRepository.getProjectById(id);
        if (response == null) {
            throw new CustomNotFoundException("Project with id " + id + " not found.");
        }
        return response;
    }
    public Project updateProject(Integer id, ProjectRequest projectRequest) {
        System.out.println(id+ " my "+ projectRequest.getProjectMangerId());
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found in the repository.");
        }

        boolean isOwner = companyService.isOwnerOfCompany(currentUser.getUserId(), projectRequest.getCompanyId()) > 0;
        if (!isOwner) {
            throw new CustomNotFoundException("You are not the owner of the company.");
        }
        Project project = getProjectByIdForPrivatePhase(id);
        AppUserDTO appUser = appUserService.getUserDtoById(projectRequest.getProjectMangerId());
        companyService.getCompanyById(projectRequest.getCompanyId());
        Integer isInCompany = userRoleRepository.isUserInCompany(appUser.getUserId(), projectRequest.getCompanyId());
        if (isInCompany == 0) {
            throw new CustomNotFoundException("User is not in a company id : " + projectRequest.getCompanyId());
        }

        String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has promote you to project manager in a project";

        Integer userRoleId = userRoleRepository.findUserRoleIdByUserIdAndCompanyId(projectRequest.getProjectMangerId(), projectRequest.getCompanyId());
        Notification notification = Notification.builder()
                .title("Assign role notification")
                .description(applicantName)
                .status("project")
                .companyId(projectRequest.getCompanyId())
                .projectId(id)
                .userId(currentUser.getUserId()) // This id is the project owner
                .userRoleId(userRoleId) // This is the receiver
                .build();

        // Push notification to post owner
        PushNotificationOptions.sendMessageToUser(notification.getDescription(), projectRequest.getProjectMangerId(), notification.getTitle());
        try {
            emailUtil.sendNotificationToEmail(currentUser.getEmail(), applicantName, "assign_role");
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send notification please try again");
        }
        userRoleRepository.removeProjectManagerFromProject(id);
        userRoleRepository.promoteProjectManagerToProject(projectRequest,id);


        Project updatedProject = projectRepository.updateProject(id, projectRequest);
        updatedProject.setUser(appUser);

        return updatedProject;
    }
    public void deleteProject(Integer id) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found in the repository.");
        }

        Project project = projectRepository.getProjectById(id);
        if (project == null) {
            throw new CustomNotFoundException("Project with id " + id + " not found.");
        }

        boolean isAdmin = companyService.isOwnerOfCompany(currentUser.getUserId(), project.getCompany().getCompanyId()) > 0;
        if (!isAdmin) {
            throw new CustomNotFoundException("You are not the owner of the company.");
        }
        userRoleRepository.removeAllMemberFromProject(id);
        projectRepository.deleteProject(id);
    }
    public Project getProjectByTitle(String name) {
        Project response = projectRepository.getProjectByTitle(name);
        log.info("Project with title {} was founded.", name);
        if (response == null) {
            throw new CustomNotFoundException("Project with title " + name + " not found.");
        }
        return response;
    }
    public List<Project> getProjectByCompanyId(Integer companyId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found in the project.");
        }

        // Check if user is in the company
        Integer userInCompany = userRoleRepository.isUserInCompany(currentUser.getUserId(), companyId);
        if (userInCompany < 1) {
            throw new CustomNotFoundException("You don't have permission.");
        }

        // Check if the user is an owner, admin, or project manager in the company
        boolean isCompanyOwner = companyService.isOwnerOfCompany(currentUser.getUserId(), companyId) > 0;
        Integer isAdminOrProjectManager = userRoleRepository.isAdminOrProjectManager(currentUser.getUserId(), companyId);

        List<Project> allProjects = projectRepository.getProjectByCompanyId(companyId);
        List<Project> filteredProjects = new java.util.ArrayList<>();

        // If user is company owner or admin, return all projects
        if (isCompanyOwner || isAdminOrProjectManager > 0) {
            filteredProjects = allProjects;
        } else {
            // For regular members/developers, only show projects where they are assigned
            for (Project project : allProjects) {
                Integer roleIdInProject = userRoleRepository.getRoleIdByUserIdAndProjectId(currentUser.getUserId(), project.getProjectId());
                if (roleIdInProject != null && roleIdInProject > 0) {
                    filteredProjects.add(project);
                }
            }
        }

        // Populate project managers
        for (Project project : filteredProjects) {
            Integer userId = userRoleRepository.getProjectManagerIdFromProject(project.getProjectId());
            if (userId == null) {
                continue;
            }
            AppUserDTO appUser = appUserService.getUserDtoById(userId);
            project.setUser(appUser);
        }

        return filteredProjects;
    }
    public Integer getProjectByPhaseId(Integer phaseId) {
        Integer projectId = projectRepository.getProjectByPhaseId(phaseId);
        if (projectId == null) {
            throw new CustomNotFoundException("Project with id " + phaseId + " not found.");
        }
        return projectId;
    }

    /**
     * Get company ID for a project
     */
    public Integer getCompanyIdByProjectId(Integer projectId) {
        return projectRepository.getCompanyIdByProjectId(projectId);
    }

    /**
     * Get projects by list of IDs
     */
    public List<Project> getProjectsByIds(List<Integer> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        
        List<Project> projects = new java.util.ArrayList<>();
        for (Integer projectId : projectIds) {
            try {
                Project project = projectRepository.getProjectById(projectId);
                if (project != null) {
                    // Populate project manager
                    Integer pmId = userRoleRepository.getProjectManagerIdFromProject(projectId);
                    if (pmId != null) {
                        AppUserDTO pmUser = appUserService.getUserDtoById(pmId);
                        project.setUser(pmUser);
                    }
                    projects.add(project);
                }
            } catch (Exception e) {
                log.warn("Failed to load project {}: {}", projectId, e.getMessage());
            }
        }
        return projects;
    }
}