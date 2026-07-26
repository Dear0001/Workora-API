package com.api.bugzapper.service.phaseService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.configuration.PushNotificationOptions;
import com.api.bugzapper.constant.RoleId;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.*;
import com.api.bugzapper.model.entity.*;
import com.api.bugzapper.model.request.AssignRoleRequest;
import com.api.bugzapper.model.request.PhaseRequest;
import com.api.bugzapper.model.request.PublicPhaseRequest;
import com.api.bugzapper.model.request.UserIdsRequest;
import com.api.bugzapper.repository.PhaseRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.service.companyService.CompanyService;
import com.api.bugzapper.service.notificationService.NotificationService;
import com.api.bugzapper.service.projectService.ProjectService;
import com.api.bugzapper.service.userService.AppUserService;
import com.api.bugzapper.util.EmailUtil;
import com.api.bugzapper.util.EncryptionUtil;
import jakarta.mail.MessagingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhaseService {
    private static final Logger log = LoggerFactory.getLogger(PhaseService.class);
    private final PhaseRepository phaseRepository;
    private final ProjectService projectService;
    private final UserRoleRepository userRoleRepository;
    private final GetCurrentUser getCurrentUser;
    private final CompanyService companyService;
    private final NotificationService notificationService;
    private final EmailUtil emailUtil;
    private final AppUserService appUserService;
    @Value("${base_url}")
    String base_url;

    public PhaseService(PhaseRepository phaseRepository, ProjectService projectService, UserRoleRepository userRoleRepository, GetCurrentUser getCurrentUser, CompanyService companyService, NotificationService notificationService, EmailUtil emailUtil, AppUserService appUserService) throws Exception {
        this.phaseRepository = phaseRepository;
        this.projectService = projectService;
        this.userRoleRepository = userRoleRepository;
        this.getCurrentUser = getCurrentUser;
        this.companyService = companyService;
        this.notificationService = notificationService;
        this.emailUtil = emailUtil;
        this.appUserService = appUserService;
    }

    /** Public wrapper so other services (e.g. phase attachments) can require owner/PM rights on a phase's project. */
    public void verifyCanManagePhase(Integer phaseId) {
        Integer projectId = projectService.getProjectByPhaseId(phaseId);
        Company company = companyService.getCompanyByProjectId(projectId);
        validateAdminOrProjectManager(getCurrentUser.getCurrentUser().getUserId(), company.getCompanyId());
    }

    private void validateAdminOrProjectManager(Integer userId, Integer companyId) {
        Integer isAdminOrProjectManager = userRoleRepository.isAdminOrProjectManager(userId, companyId);
        boolean isCompanyOwner = companyService.isOwnerOfCompany(userId, companyId) > 0;
        if (isAdminOrProjectManager < 1 && !isCompanyOwner) {
            throw new CustomNotFoundException("Current user is not the owner of the company or a project manager.");
        }
    }
    @Transactional
    public Phase createPrivatePhase(PhaseRequest phaseRequest) {
        AppUser currentUser = getCurrentUser.getCurrentUser();

        Company company = companyService.getCompanyByProjectId(phaseRequest.getProjectId());
        validateAdminOrProjectManager(currentUser.getUserId(), company.getCompanyId());
        Project projects = projectService.getProjectById(phaseRequest.getProjectId());
        Integer roleId = userRoleRepository.getRoleIdByUserIdAndProjectId(currentUser.getUserId(), projects.getProjectId());

        // If roleId is null, use company-level role (admin or project manager already validated)
        if (roleId == null) {
            roleId = userRoleRepository.findUserRoleByUserIdAndCompanyId(currentUser.getUserId(), company.getCompanyId());
        }

        if (roleId != null && (roleId == RoleId.COMPANY_OWNER || roleId == RoleId.PROJECT_MANAGER)) {
            String code = RandomStringUtils.randomAlphanumeric(5);
            String link = code;
            Integer phaseId = phaseRepository.createPrivatePhase(phaseRequest, link);

            String encryptedPhaseId = encryptPhaseId(phaseId);

            String linkEncryption = encryptedPhaseId;
            Phase phase1 = phaseRepository.updateLinkToEncrypt(linkEncryption, phaseId);
            phase1.setLink(base_url + phase1.getLink());

            Integer phaseIdInUserRoles = userRoleRepository.getPhaseIdByUserIdAndCompanyIdAndProjectId(currentUser.getUserId(), company.getCompanyId(), projects.getProjectId());

            for (Integer userId : phaseRequest.getUserId()){
                System.out.println("userId: " + userId);

                AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);
                Integer isInCompany = userRoleRepository.isUserInCompany(userId, company.getCompanyId());
                if (isInCompany == 0) {
                    throw new CustomNotFoundException("Current user is not the member of the company.");
                }
                Integer userRoleId = userRoleRepository.findUserRoleIdByUserId(userId);
                String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has added you to a phase";

                Notification notification = Notification.builder()
                        .title("Add to phase notification")
                        .description(applicantName)
                        .status("phase")
                        .companyId(company.getCompanyId())
                        .projectId(projects.getProjectId())
                        .phaseId(phaseId)
                        .userId(currentUser.getUserId()) // This id is the phase owner
                        .userRoleId(userRoleId) // This is the receiver
                        .build();

                // Push notification to post owner
                PushNotificationOptions.sendMessageToUser(notification.getDescription(), userId, notification.getTitle());
                try {
                    emailUtil.sendNotificationToEmail(appUserDTO.getEmail(), applicantName, "add_to_phase");
                } catch (MessagingException e) {
                    log.warn("Phase creation: notification email failed for user {}: {}", userId, e.getMessage());
                }

                // Insert notification to its table
                notificationService.createNotification(notification);

                Integer managerRoleId = userRoleRepository.getRoleIdByUserIdAndProjectId(userId, projects.getProjectId());
                Integer isInProjectAndPhase = userRoleRepository.isInProjectAndPhase(userId,company.getCompanyId(), projects.getProjectId());
                if (managerRoleId != null && managerRoleId == RoleId.PROJECT_MANAGER) {
                    if (isInProjectAndPhase < 1 ) {
                        userRoleRepository.updatePhaseIdForUser(userId, company.getCompanyId(), projects.getProjectId(), phaseId, RoleId.PROJECT_MANAGER);
                    }else{
                        userRoleRepository.insertPhaseIdForUserInUserRole(userId, company.getCompanyId(), projects.getProjectId(), phaseId, RoleId.PROJECT_MANAGER);
                    }
                }else{
                    if (isInProjectAndPhase < 1 ) {
                        Integer isInProject = userRoleRepository.isInProjectInCompany(userId, company.getCompanyId());
                        if (isInProject > 0) {
                            userRoleRepository.updateProjectIdForUser(userId, company.getCompanyId(), projects.getProjectId(), phaseId, RoleId.DEVELOPER);
                        }else{
                            userRoleRepository.updatePhaseIdForUser(userId, company.getCompanyId(), projects.getProjectId(), phaseId, RoleId.DEVELOPER);
                        }
                    }else{
                        userRoleRepository.insertPhaseIdForUserInUserRole(userId, company.getCompanyId(), projects.getProjectId(), phaseId, RoleId.DEVELOPER);
                    }
                }
            }

            if (phaseIdInUserRoles == null) {
                userRoleRepository.updateInsertPhaseIdAndProjectIdAndCompanyIdIntoUserRole(currentUser.getUserId(), roleId, company.getCompanyId(), projects.getProjectId(), phaseId);
                return phase1;
            }
            userRoleRepository.insertPhaseIdAndProjectIdAndCompanyIdIntoUserRole(currentUser.getUserId(), roleId, company.getCompanyId(), projects.getProjectId(), phaseId);
            return phase1;
        }

        throw new CustomNotFoundException("Current user is not the owner of the company or a project manager in this phase");
    }

    private String encryptPhaseId(Integer phaseId) {
        try {
            return EncryptionUtil.encrypt(String.valueOf(phaseId));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting the phase ID", e);
        }
    }

    private String decryptPhaseId(String phaseId) {
        try {
            return EncryptionUtil.decrypt(String.valueOf(phaseId));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting the phase ID", e);
        }
    }
    @Transactional
    public Phase updatePrivatePhase(Integer id, PhaseRequest phaseRequest) {
        findById(id);
        Project project = projectService.getProjectById(phaseRequest.getProjectId());

        AppUser currentUser = getCurrentUser.getCurrentUser();
        Company company = companyService.getCompanyByProjectId(phaseRequest.getProjectId());

        Integer roleId = userRoleRepository.getRoleIdByUserIdAndProjectId(currentUser.getUserId(), project.getProjectId());

        validateAdminOrProjectManager(currentUser.getUserId(), company.getCompanyId());

        // If roleId is null, use company-level role (admin or project manager already validated)
        if (roleId == null) {
            roleId = userRoleRepository.findUserRoleByUserIdAndCompanyId(currentUser.getUserId(), company.getCompanyId());
        }

        if (roleId != null && (roleId == RoleId.COMPANY_OWNER || roleId == RoleId.PROJECT_MANAGER)) {
            for (Integer userId : phaseRequest.getUserId()){
                AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);
                Integer isInCompany = userRoleRepository.isUserInCompany(userId, company.getCompanyId());
                if (isInCompany == 0) {
                    throw new CustomNotFoundException("Current user is not the member of the company.");
                }

                Integer userRoleId = userRoleRepository.findUserRoleIdByUserId(userId);
                String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has added you to a phase";

                Notification notification = Notification.builder()
                        .title("Add to phase notification")
                        .description(applicantName)
                        .status("phase")
                        .companyId(company.getCompanyId())
                        .projectId(project.getProjectId())
                        .phaseId(id)
                        .userId(currentUser.getUserId()) // This id is the phase owner
                        .userRoleId(userRoleId) // This is the receiver
                        .build();

                // Push notification to post owner
                PushNotificationOptions.sendMessageToUser(notification.getDescription(), userId, notification.getTitle());
                try {
                    emailUtil.sendNotificationToEmail(appUserDTO.getEmail(), applicantName, "add_to_phase");
                } catch (MessagingException e) {
                    log.warn("Phase update: notification email failed for user {}: {}", userId, e.getMessage());
                }

                // Insert notification to its table
                notificationService.createNotification(notification);


                Integer managerRoleId = userRoleRepository.getRoleIdByUserIdAndProjectId(userId, project.getProjectId());
                Integer isInProjectAndPhase = userRoleRepository.isInProjectAndPhase(userId,company.getCompanyId(), project.getProjectId());

                if (managerRoleId != null && managerRoleId == RoleId.PROJECT_MANAGER) {
                    if (isInProjectAndPhase < 1 ) {
                        userRoleRepository.updatePhaseIdForUser(userId, company.getCompanyId(), project.getProjectId(), id, RoleId.PROJECT_MANAGER);
                    }else{
                        userRoleRepository.insertPhaseIdForUserInUserRole(userId, company.getCompanyId(), project.getProjectId(), id, RoleId.PROJECT_MANAGER);
                    }
                }else {
                    if (isInProjectAndPhase < 1) {
                        Integer isInProject = userRoleRepository.isInProjectInCompany(userId, company.getCompanyId());
                        if (isInProject > 0) {
                            userRoleRepository.updateProjectIdForUser(userId, company.getCompanyId(), project.getProjectId(), id, RoleId.DEVELOPER);
                        }else{
                            userRoleRepository.updatePhaseIdForUser(userId, company.getCompanyId(), project.getProjectId(), id, RoleId.DEVELOPER);
                        }
                    }else{
                        userRoleRepository.insertPhaseIdForUserInUserRole(userId, company.getCompanyId(), project.getProjectId(), id, RoleId.DEVELOPER);
                    }
                }
            }

            return phaseRepository.updatePrivatePhase(id, phaseRequest);
        }
        throw new CustomNotFoundException("Current user is not the owner of the company or a project manager");
    }
    public void deletePhase(Integer id) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        findById(id);

        Integer projectId = projectService.getProjectByPhaseId(id);

        Company company = companyService.getCompanyByProjectId(projectId);
        validateAdminOrProjectManager(currentUser.getUserId(), company.getCompanyId());
        Integer roleId = userRoleRepository.getRoleIdByUserIdAndProjectId(currentUser.getUserId(), projectId);
        System.out.println("role id " + roleId);
        if (roleId != null && (roleId == RoleId.COMPANY_OWNER || roleId == RoleId.PROJECT_MANAGER)) {
            userRoleRepository.setPhaseIdToNull(id);
            notificationService.clearPhaseReference(id);
            phaseRepository.deletePhase(id);
            return;
        }
        System.out.println("hi you are here");
        throw new CustomNotFoundException("Current user is not the owner of the company or a project manager");
    }
    public List<PhaseMemberDTO> getPhaseMembers(Integer phaseId) {
        findById(phaseId);
        List<PhaseMemberDTO> phaseMembers = userRoleRepository.getPhaseMembersForDisplay(phaseId);

        return phaseMembers;
    }
    public Phase changePhaseType(Integer phaseId, Boolean isPrivate) {
        // Ensure the phase exists
        Phase phase = phaseRepository.findById(phaseId);
        if (phase == null) {
            throw new CustomNotFoundException("Phase with id " + phaseId + " not found.");
        }
        Integer currentUserId = getCurrentUser.getCurrentUser().getUserId();
        Integer companyId = phase.getProject().getCompany().getCompanyId();
        boolean isAdminOrManager = userRoleRepository.isAdminOrProjectManager(currentUserId, companyId) > 0;
        boolean isCompanyOwner = companyService.isOwnerOfCompany(currentUserId, companyId) > 0;
        if (!isAdminOrManager && !isCompanyOwner) {
            throw new CustomNotFoundException("Current user is not the owner of the company or a project manager.");
        }

        // Update the phase type
        phase.setIsPrivate(isPrivate);
        return phaseRepository.updatePhaseType(phaseId, isPrivate);
    }
    public List<PhaseDTO> getAllByUserId(Integer offset, Integer limit) {
        AppUser currentUser = getCurrentUser.getCurrentUser();

        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found.");
        }

        return phaseRepository.getAllPhaseByUserId(currentUser.getUserId(), offset, limit);
    }
    public Phase findById(Integer id) {
        Phase phase = phaseRepository.findById(id);
        if (phase == null) {
            throw new CustomNotFoundException("Phase with id " + id + " not found.");
        }
        AppUser currentUser = getCurrentUser.getCurrentUser();
        boolean isPhaseMember = isPhaseMember(id, currentUser.getUserId());

        // Check if the current user is an admin or a project manager
        Integer companyId = companyService.getCompanyIdByPhaseId(phase.getId());
        boolean isAdminOrProjectManager = userRoleRepository.isAdminOrProjectManager(currentUser.getUserId(), companyId) > 0;
        boolean isCompanyOwner = companyService.isOwnerOfCompany(currentUser.getUserId(), companyId) > 0;

        if (phase.getIsPrivate() && !isPhaseMember && !isAdminOrProjectManager && !isCompanyOwner) {
            throw new CustomNotFoundException("Phase with id " + id + " is private and you are not authorized to view it.");
        }

        return phase;
    }

    private boolean isPhaseMember(Integer phaseId, Integer userId) {
        List<PhaseMemberDTO> phaseMembers = userRoleRepository.getPhaseMembers(phaseId);
        return phaseMembers.stream().anyMatch(member -> member.getUserId().equals(userId));
    }
    public void removeMemberFromPhase(Integer phaseId, UserIdsRequest userIds) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        Integer companyId = companyService.getCompanyIdByPhaseId(phaseId);
        validateAdminOrProjectManager(currentUser.getUserId(), companyId);
        for (Integer userId : userIds.getUserIds()){
            boolean isPhaseMember = isPhaseMember(phaseId, userId);
            if (!isPhaseMember) {
                throw new CustomNotFoundException("User is not a member of the phase.");
            }

            userRoleRepository.removeMemberFromPhase(phaseId, userId);
        }
    }
    public Integer addUserToPhaseByCode(String code) {
        String phaseCode = decryptPhaseId(code);

        Integer decryptCode = Integer.valueOf(phaseCode);
        System.out.println("this is code " + decryptCode);
        Phase phase = phaseRepository.findByCode(decryptCode);
        if (phase == null) {
            throw new CustomNotFoundException("Invalid phase code provided: " + decryptCode);
        }
        Integer companyId = companyService.getCompanyIdByPhaseId(phase.getId());

        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found in the repository.");
        }

        Integer projectId = projectService.getProjectByPhaseId(phase.getId());

//        if (userRoleRepository.isUserInCompany(currentUser.getUserId(), companyId) < 1) {
//            throw new CustomNotFoundException("User is not a member in this company.");
//        }

        if (userRoleRepository.isUserExistsInPhase(currentUser.getUserId(), phase.getId(), companyId, projectId)) {
            throw new CustomNotFoundException("User already exists in the phase.");
        }

        List<Integer> userIds = userRoleRepository.findUserRoleIdByCompanyIdAndProjectId(companyId, projectId);

        String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has joined to a phase";
        System.out.println("company id : " + companyId);
        for (Integer userId : userIds) {
            Integer userRoleId = userRoleRepository.findUserRoleIdByUserIdAndCompanyId(userId, companyId);
            AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);
            Notification notification = Notification.builder()
                    .title("Join to phase notification")
                    .description(applicantName)
                    .status("phase")
                    .companyId(companyId)
                    .projectId(projectId)
                    .phaseId(phase.getId())
                    .userId(currentUser.getUserId()) // This id is the assign task owner
                    .userRoleId(userRoleId) // This is the receiver
                    .build();

            // Push notification to post owner
            PushNotificationOptions.sendMessageToUser(notification.getDescription(), userId, notification.getTitle());
            try {
                emailUtil.sendNotificationToEmail(appUserDTO.getEmail(), applicantName, "joined_phase");
            } catch (MessagingException e) {
                log.warn("Phase join: notification email failed for user {}: {}", userId, e.getMessage());
            }

            // Insert notification to its table
            notificationService.createNotification(notification);
        }


        Integer userId = currentUser.getUserId();
        Integer managerRoleId = userRoleRepository.getRoleIdByUserIdAndProjectId(userId, projectId);
        Integer isInProjectAndPhase = userRoleRepository.isInProjectAndPhase(userId,companyId, projectId);
        if (managerRoleId == null) {
            userRoleRepository.insertPhaseIdForUserInUserRole(userId, companyId, projectId, phase.getId(), RoleId.DEVELOPER);
        } else if (managerRoleId == RoleId.PROJECT_MANAGER) {
            if (isInProjectAndPhase < 1) {
                userRoleRepository.updatePhaseIdForUser(userId, companyId, projectId, phase.getId(), RoleId.PROJECT_MANAGER);
            } else {
                userRoleRepository.insertPhaseIdForUserInUserRole(userId, companyId, projectId, phase.getId(), RoleId.PROJECT_MANAGER);
            }
        } else {
            Integer isInCompany = userRoleRepository.isUserInCompany(userId, companyId);
            if(isInCompany == 1 ){
                userRoleRepository.insertToCompany(userId, companyId, projectId, phase.getId(), RoleId.DEVELOPER);
            }
            else if (isInCompany < 1) {
                Integer userCompanyNull = userRoleRepository.checkCompanyIdIsNull(userId);
                if (userCompanyNull != null) {
                    userRoleRepository.updateToCompany(userId, companyId, projectId, phase.getId(), RoleId.DEVELOPER, userCompanyNull);
                } else {
                    userRoleRepository.insertToCompany(userId, companyId, projectId, phase.getId(), RoleId.DEVELOPER);
                }
            } else {
                if(isInCompany > 1){
                    userRoleRepository.insertToCompany(userId, companyId, projectId, phase.getId(), RoleId.DEVELOPER);
                }
                else if (isInProjectAndPhase < 1) {
                    userRoleRepository.updatePhaseIdForUser(userId, companyId, projectId, phase.getId(), RoleId.DEVELOPER);
                } else{
                    userRoleRepository.insertPhaseIdForUserInUserRole(userId, companyId, projectId, phase.getId(), RoleId.DEVELOPER);
                }
            }
        }
        return phase.getProject().getProjectId();
    }
    public List<Phase> getAllPhaseByProjectId(Integer projectId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        projectService.getProjectById(projectId);
        Company company = companyService.getCompanyByProjectId(projectId);

        boolean isCompanyOwner = companyService.isOwnerOfCompany(currentUser.getUserId(), company.getCompanyId()) > 0;
        Integer pmId = userRoleRepository.getProjectManagerIdFromProject(projectId);
        boolean isProjectManager = pmId != null && pmId.equals(currentUser.getUserId());

        if (isCompanyOwner || isProjectManager) {
            return phaseRepository.getAllPhaseByProjectId(projectId);
        }

        List<Integer> phaseIds = phaseRepository.getPhaseIdsByUserIdAndProjectId(
                currentUser.getUserId(), projectId);
        List<Phase> phases = new java.util.ArrayList<>();
        for (Integer phaseId : phaseIds) {
            Phase phase = phaseRepository.findById(phaseId);
            if (phase != null) {
                phases.add(phase);
            }
        }
        return phases;
    }
    public List<PhaseNewsfeedDTO> getAllPublicPhase(Integer offset, Integer limit) {
        List<PhaseNewsfeedDTO> phases = phaseRepository.getAllPublicPhase(offset, limit);
        applyCanManage(phases);
        return phases;
    }

    private void applyCanManage(List<PhaseNewsfeedDTO> phases) {
        Integer currentUserId = getCurrentUser.getCurrentUser().getUserId();
        for (PhaseNewsfeedDTO phase : phases) {
            Integer companyId = phase.getCompanyId();
            boolean canManage = companyId != null
                    && (userRoleRepository.isAdminOrProjectManager(currentUserId, companyId) > 0
                        || companyService.isOwnerOfCompany(currentUserId, companyId) > 0);
            phase.setCanManage(canManage);
        }
    }

    /** Owner/project-manager-only: close or reopen a public phase to new bug reports. */
    public Phase updatePhaseStatus(Integer phaseId, String status) {
        Phase phase = findById(phaseId);
        Integer currentUserId = getCurrentUser.getCurrentUser().getUserId();
        Integer companyId = companyService.getCompanyIdByPhaseId(phaseId);
        validateAdminOrProjectManager(currentUserId, companyId);

        String normalized;
        if ("CLOSED".equalsIgnoreCase(status)) {
            normalized = "CLOSED";
        } else if ("OPEN".equalsIgnoreCase(status)) {
            normalized = "OPEN";
        } else {
            throw new CustomNotFoundException("Status must be either OPEN or CLOSED");
        }
        phaseRepository.updatePhaseStatus(phaseId, normalized);
        return phaseRepository.findById(phaseId);
    }
    public Phase createPublicPhase(PublicPhaseRequest publicPhaseRequest) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        Company company = companyService.getCompanyByProjectId(publicPhaseRequest.getProjectId());
        validateAdminOrProjectManager(currentUser.getUserId(), company.getCompanyId());
        Project projects = projectService.getProjectById(publicPhaseRequest.getProjectId());
        Integer roleId = userRoleRepository.getRoleIdByUserIdAndProjectId(currentUser.getUserId(), projects.getProjectId());

        // If roleId is null, use company-level role (admin or project manager already validated)
        if (roleId == null) {
            roleId = userRoleRepository.findUserRoleByUserIdAndCompanyId(currentUser.getUserId(), company.getCompanyId());
        }

        if (roleId != null && (roleId == RoleId.COMPANY_OWNER || roleId == RoleId.PROJECT_MANAGER)) {
            String code = RandomStringUtils.randomAlphanumeric(5);
            String link = code;

            Integer phaseId = phaseRepository.createPublicPhase(publicPhaseRequest, link);

            String encryptedPhaseId = encryptPhaseId(phaseId);

            String linkEncryption = encryptedPhaseId;
            Phase phase1 = phaseRepository.updateLinkToEncrypt(linkEncryption, phaseId);
            phase1.setLink(base_url + phase1.getLink());

            Integer phaseIdInUserRoles = userRoleRepository.getPhaseIdByUserIdAndCompanyIdAndProjectId(currentUser.getUserId(), company.getCompanyId(), projects.getProjectId());
            if (phaseIdInUserRoles == null) {
                userRoleRepository.updateInsertPhaseIdAndProjectIdAndCompanyIdIntoUserRole(currentUser.getUserId(), roleId, company.getCompanyId(), projects.getProjectId(), phaseId);
                return phase1;
            }
            userRoleRepository.insertPhaseIdAndProjectIdAndCompanyIdIntoUserRole(currentUser.getUserId(), roleId, company.getCompanyId(), projects.getProjectId(), phaseId);
            return phase1;
        }

        throw new CustomNotFoundException("Current user is not the owner of the company or a project manager in this phase");

    }
    @Transactional
    public void assignRoleToUserByPhaseId(AssignRoleRequest assignRoleRequest, Integer phaseId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        findById(phaseId);
        Integer projectId = projectService.getProjectByPhaseId(phaseId);
        Company company = companyService.getCompanyByProjectId(projectId);
        // Only the company owner or the project's manager may re-assign phase roles —
        // without this, any authenticated user could promote anyone to PROJECT_MANAGER.
        validateAdminOrProjectManager(currentUser.getUserId(), company.getCompanyId());
        // Nobody may change their own role through this endpoint (blocks a project manager
        // self-assigning PHASE_LEAD, or re-affirming themselves as PROJECT_MANAGER).
        if (assignRoleRequest.getUserIds().contains(currentUser.getUserId())) {
            throw new CustomNotFoundException("You cannot change your own role through this endpoint.");
        }
        for (Integer userId : assignRoleRequest.getUserIds()){
            AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);
            for (Integer roleId : assignRoleRequest.getRoleId()){
                if (roleId < RoleId.PROJECT_MANAGER || roleId > RoleId.DEVELOPER) {
                    throw new CustomNotFoundException("Role id must be PROJECT_MANAGER(2), PHASE_LEAD(3), or DEVELOPER(4).");
                }
                userRoleRepository.updateUserRoleByPhaseId(userId, roleId, phaseId);
            }

            String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has assigned a new role to you in a phase";

            Integer userRoleId = userRoleRepository.findUserRoleIdByUserIdInPhaseId(userId, phaseId);
            Notification notification = Notification.builder()
                    .title("Assign role notification")
                    .description(applicantName)
                    .status("phase")
                    .companyId(company.getCompanyId())
                    .projectId(projectId)
                    .phaseId(phaseId)
                    .userId(currentUser.getUserId()) // This id is the assign task owner
                    .userRoleId(userRoleId) // This is the receiver
                    .build();

            // Push notification to post owner
            PushNotificationOptions.sendMessageToUser(notification.getDescription(), userId, notification.getTitle());
            try {
                emailUtil.sendNotificationToEmail(appUserDTO.getEmail(), applicantName, "assign_role");
            } catch (MessagingException e) {
                log.warn("Phase role assignment: notification email failed for user {}: {}", userId, e.getMessage());
            }

            // Insert notification to its table
            notificationService.createNotification(notification);
        }

    }
    public Phase updatePublicPhase(Integer id, PublicPhaseRequest publicPhaseRequest) {
        findById(id);
        Company company = companyService.getCompanyByProjectId(publicPhaseRequest.getProjectId());
        validateAdminOrProjectManager(getCurrentUser.getCurrentUser().getUserId(), company.getCompanyId());
        projectService.getProjectById(publicPhaseRequest.getProjectId());
        return phaseRepository.updatePublicPhase(id, publicPhaseRequest);
    }
    public Integer getPhaseByTaskId(Integer taskId) {
        return phaseRepository.getPhaseByTaskId(taskId);
    }
    public List<PhaseNewsfeedDTO> getAllPublicPhasesByCompanyId(Integer companyId, Integer offset, Integer limit) {
        companyService.getCompanyById(companyId);
        List<PhaseNewsfeedDTO> phases = phaseRepository.getAllPublicPhasesByCompanyId(companyId, offset, limit);
        applyCanManage(phases);
        return phases;
    }
}
