package com.api.bugzapper.service.companyService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.configuration.PushNotificationOptions;
import com.api.bugzapper.constant.RoleId;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.dto.CompanyMemberDTO;
import com.api.bugzapper.model.dto.CompanyRatingDTO;
import com.api.bugzapper.model.dto.TopCompany;
import com.api.bugzapper.model.entity.*;
import com.api.bugzapper.model.request.CompanyRequest;
import com.api.bugzapper.model.request.InviteMemberRequest;
import com.api.bugzapper.repository.CompanyRepository;

import com.api.bugzapper.repository.RoleRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.service.notificationService.NotificationService;
import com.api.bugzapper.service.permissionService.PermissionService;
import com.api.bugzapper.service.projectService.ProjectService;
import com.api.bugzapper.service.userService.AppUserService;
import com.api.bugzapper.util.EmailUtil;
import jakarta.mail.MessagingException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRoleRepository userRolesRepository;
    private final RoleRepository rolesRepository;
    private final GetCurrentUser getCurrentUser;
    private final AppUserService appUserService;
    private final EmailUtil emailUtil;
    private final NotificationService notificationService;
    private final ProjectService projectService;
    private final PermissionService permissionService;

    public CompanyService(CompanyRepository companyRepository, UserRoleRepository userRolesRepository, RoleRepository rolesRepository, GetCurrentUser getCurrentUser, AppUserService appUserService, EmailUtil emailUtil, NotificationService notificationService, @Lazy ProjectService projectService, PermissionService permissionService) {
        this.companyRepository = companyRepository;
        this.userRolesRepository = userRolesRepository;
        this.rolesRepository = rolesRepository;
        this.getCurrentUser = getCurrentUser;
        this.appUserService = appUserService;
        this.emailUtil = emailUtil;
        this.notificationService = notificationService;
        this.projectService = projectService;
        this.permissionService = permissionService;
    }
    public Company createCompany(CompanyRequest companyRequest) {

        String currentUsername = getUsernameOfCurrentUser();

        String code = RandomStringUtils.randomAlphanumeric(15);

        Company company = companyRepository.createCompany(companyRequest, code);

        // Assign current user as COMPANY_OWNER (role_id 1)
        AppUser currentUser = getCurrentUser.getCurrentUser();
        Integer placeholderUserRoleId = userRolesRepository.checkCompanyIdIsNull(currentUser.getUserId());
        if (placeholderUserRoleId != null) {
            userRolesRepository.updateCompanyIdToUserRole(placeholderUserRoleId, company.getCompanyId());
            return company;
        }
        userRolesRepository.assignRoleToUserInCompany(currentUser.getUserId(), company.getCompanyId(), RoleId.COMPANY_OWNER);

        return company;
    }
    public Company getCompanyById(Integer id) {
        Company response = companyRepository.getCompanyById(id);
        if (response == null) {
            throw new CustomNotFoundException("Company with id " + id + " not found.");
        }
        return response;
    }
    public Company updateCompany(Integer id, CompanyRequest companyRequest) {
        // Retrieve current company's email
        Company currentCompany = getCompanyById(id);

        // Check if the company belongs to the current user and the user has admin role
        if (!isCompanyAdmin(getCurrentUser.getCurrentUser().getUserId(), currentCompany.getCompanyId())) {
            throw new CustomNotFoundException("You do not have permission to update this company.");
        }

        Company updatedCompany = companyRepository.updateCompany(id, companyRequest);

        return updatedCompany;
    }

    private boolean isCompanyAdmin(Integer userId, Integer companyId) {
        return permissionService.isCompanyOwner(userId, companyId);
    }
    public void deleteCompany(Integer id) {
        Company company = getCompanyById(id);

        if (!isCurrentUserAdminOfCompany(company.getCompanyId())) {
            throw new CustomNotFoundException("You do not have permission to delete this company.");
        }

        companyRepository.deleteCompany(id);
    }


    private String getUsernameOfCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private boolean isCurrentUserAdminOfCompany(Integer companyId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        return permissionService.isCompanyOwner(currentUser.getUserId(), companyId);
    }

    public boolean addUserToCompanyByCode(String code) {
        Company company = companyRepository.findByCode(code);
        System.out.println("company : " + company);
        if (company == null) {
            throw new CustomNotFoundException("Invalid company code provided: " + code);
        }

        AppUser currentUser = getCurrentUser.getCurrentUser();
        System.out.println("current user : " + currentUser);
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found in the repository.");
        }

        if (userRolesRepository.isUserExistsInCompany(currentUser.getUserId(), company.getCompanyId())) {
            throw new CustomNotFoundException("User already exists in the company.");
        }

        String applicantName = currentUser.getFirstName() + " " + currentUser.getLastName() + " has joined your company";
        Integer userRoleId = userRolesRepository.findUserRoleIdByCompanyIdLimit1(company.getCompanyId());
        System.out.println("userRoleId : " + userRoleId);

        Integer userReceiverId = userRolesRepository.findUserIdByCompanyId(company.getCompanyId());
        System.out.println("user id " + userReceiverId);
        AppUserDTO appUserDTO = userReceiverId != null ? appUserService.getUserDtoById(userReceiverId) : null;

        // Open join-by-code always grants DEVELOPER (role_id 4); a company owner can
        // promote/reassign the member's role afterward via switch-user-role or invite-member.
        Integer userCompanyNull = userRolesRepository.checkCompanyIdIsNull(currentUser.getUserId());
        if (userCompanyNull != null) {
            userRolesRepository.updateUserToCompany(currentUser.getUserId(), company.getCompanyId(), RoleId.DEVELOPER, userCompanyNull);
        } else {
            userRolesRepository.assignRoleToUserInCompany(currentUser.getUserId(), company.getCompanyId(), RoleId.DEVELOPER);
        }

        Notification notification = Notification.builder()
                .title("Join company notification")
                .description(applicantName)
                .status("company")
                .companyId(company.getCompanyId())
                .userId(currentUser.getUserId())
                .userRoleId(userRoleId)
                .build();

        if (userReceiverId != null && userRoleId != null && appUserDTO != null) {
            try {
                PushNotificationOptions.sendMessageToUser(
                        notification.getDescription(), userReceiverId, notification.getTitle());
                try {
                    emailUtil.sendNotificationToEmail(appUserDTO.getEmail(), applicantName, "join_company");
                } catch (MessagingException e) {
                    log.warn("Join company: email to admin failed: {}", e.getMessage());
                }
                notificationService.createNotification(notification);
            } catch (Exception e) {
                log.warn(
                        "Join company: push/notification failed (member was added). Run SQL migration "
                                + "src/main/resources/notification-add-app-columns.sql if you see missing-column errors: {}",
                        e.getMessage());
            }
        } else {
            log.warn(
                    "Join company: skipped admin notify (missing receiver or role). companyId={}",
                    company.getCompanyId());
        }

        return true;
    }
    public List<CompanyMemberDTO> companyMembers(Integer companyId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found in the repository.");
        }
        // Check if the current user is a member of the specified company
        int isMember = userRolesRepository.isUserInCompany(currentUser.getUserId(), companyId);
        if (isMember <= 0) {
            throw new CustomNotFoundException("Current user is not a member of the specified company.");
        }
        return userRolesRepository.companyMembers(companyId);
    }

    public String getCurrentUserRoleNameInCompany(Integer companyId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found in the repository.");
        }
        int isMember = userRolesRepository.isUserInCompany(currentUser.getUserId(), companyId);
        if (isMember <= 0) {
            throw new CustomNotFoundException("Current user is not a member of the specified company.");
        }
        String roleName = userRolesRepository.getRoleNameForUserInCompany(currentUser.getUserId(), companyId);
        if (roleName == null) {
            throw new CustomNotFoundException("Role not found for the current user in this company.");
        }
        return roleName;
    }
    public UserRole updateUserRole(Integer userId, Integer roleId, Integer companyId) {
        if (getCurrentUser.getCurrentUser().getUserId().equals(userId)) {
            throw new CustomNotFoundException("You can not demote yourself");
        }

        AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);

        if (appUserDTO == null) {
            throw new CustomNotFoundException("User is not found.");
        }

        if (!isCurrentUserOwnerOfCompany(companyId)) {
            throw new CustomNotFoundException("Current user is not the owner of the company.");
        }

        if (!rolesRepository.roleExists(roleId)) {
            throw new CustomNotFoundException("Role ID " + roleId + " does not exist.");
        }

        if (roleId.equals(RoleId.COMPANY_OWNER) || roleId.equals(RoleId.BUG_HUNTER)) {
            throw new CustomNotFoundException("Cannot assign this role through switch-user-role.");
        }

        Integer userRoleId = userRolesRepository.findPrimaryUserRoleId(userId, companyId);
        if (userRoleId == null) {
            throw new CustomNotFoundException("User is not a member of this company.");
        }

        int updatedUserRole = userRolesRepository.updateUserRole(userRoleId, roleId);
        log.info("User role updated: {}", updatedUserRole);

        UserRole userRole = userRolesRepository.findUserRole(userId, companyId).get(0);
        if (userRole == null) {
            throw new CustomNotFoundException("Updated user role not found.");
        }

        return userRole;
    }
    /**
     * Company-owner-only: grant an existing user one of the assignable company roles
     * (PROJECT_MANAGER/PHASE_LEAD/DEVELOPER/RECRUITER), optionally scoped to a project
     * and/or phase. Unlike switch-user-role, this creates a NEW user_roles row, so it's
     * how a user ends up with several different roles across projects in one company.
     */
    public UserRole inviteMember(InviteMemberRequest request) {
        if (!isCurrentUserOwnerOfCompany(request.getCompanyId())) {
            throw new CustomNotFoundException("Only the company owner can invite members.");
        }

        Integer roleId = request.getRoleId();
        if (roleId.equals(RoleId.COMPANY_OWNER) || roleId.equals(RoleId.BUG_HUNTER)) {
            throw new CustomNotFoundException("roleId must be one of PROJECT_MANAGER, PHASE_LEAD, DEVELOPER, RECRUITER.");
        }
        if (!rolesRepository.roleExists(roleId)) {
            throw new CustomNotFoundException("Role ID " + roleId + " does not exist.");
        }

        AppUserDTO invitee = appUserService.findUserByEmail(request.getEmail().toLowerCase());
        Integer inviteeId = invitee.getUserId();
        Integer companyId = request.getCompanyId();
        Integer projectId = request.getProjectId();
        Integer phaseId = request.getPhaseId();

        if (projectId != null && phaseId != null) {
            if (userRolesRepository.isUserExistsInPhase(inviteeId, phaseId, companyId, projectId)) {
                throw new CustomNotFoundException("User already has a role in this phase.");
            }
            userRolesRepository.insertPhaseIdAndProjectIdAndCompanyIdIntoUserRole(inviteeId, roleId, companyId, projectId, phaseId);
        } else if (projectId != null) {
            userRolesRepository.insertProjectIdAndCompanyIdIntoUserRole(inviteeId, roleId, companyId, projectId);
        } else {
            if (userRolesRepository.isUserExistsInCompany(inviteeId, companyId)) {
                throw new CustomNotFoundException("User already exists in the company.");
            }
            Integer placeholderUserRoleId = userRolesRepository.checkCompanyIdIsNull(inviteeId);
            if (placeholderUserRoleId != null) {
                userRolesRepository.updateUserToCompany(inviteeId, companyId, roleId, placeholderUserRoleId);
            } else {
                userRolesRepository.assignRoleToUserInCompany(inviteeId, companyId, roleId);
            }
        }

        List<UserRole> rows = userRolesRepository.findUserRole(inviteeId, companyId);
        if (rows.isEmpty()) {
            throw new CustomNotFoundException("Invite succeeded but the new role could not be read back.");
        }
        return rows.get(rows.size() - 1);
    }

    public void removeUserFromCompany(Integer userId, Integer companyId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (!isCurrentUserOwnerOfCompany(companyId)) {
            throw new CustomNotFoundException("Current user is not the owner of the company.");
        }
        if (currentUser.getUserId().equals(userId)) {
            throw new CustomNotFoundException("Admin cannot remove themselves from the company.");
        }
        AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);

        if (appUserDTO == null) {
            throw new CustomNotFoundException("User not found in the company.");
        }
        userRolesRepository.removeUserFromCompany(userId, companyId);
    }
    public Integer getCompanyIdByPhaseId(Integer phaseId) {
        Integer companyId = companyRepository.getCompanyIdByPhaseId(phaseId);
        if (companyId == null) {
            throw new CustomNotFoundException("Phase with id " + phaseId + " is not in the company");
        }
        return companyId;
    }
    public List<TopCompany> getTopCompany() {
        List<TopCompany> topCompanies = companyRepository.getTopCompany();
        if (topCompanies == null) {
            throw new CustomNotFoundException("There are no company");
        }
        return topCompanies;
    }
    public CompanyRatingDTO getCompanyByIdWithRating(Integer companyId) {
        getCompanyById(companyId);
        return companyRepository.getCompanyByIdWithRating(companyId);
    }

    private boolean isCurrentUserOwnerOfCompany(Integer companyId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (currentUser == null) {
            throw new CustomNotFoundException("Current user not found in the repository.");
        }

        int ownershipCount = companyRepository.isOwnerOfCompany(currentUser.getUserId(), companyId);
        return ownershipCount > 0;
    }
    public Company getCompanyByProjectId(Integer projectId) {
        Project project = projectService.getProjectById(projectId);
        if (project == null) {
            throw new CustomNotFoundException("Project with id " + projectId + " is not found.");
        }
        Company company = companyRepository.getCompanyByProjectId(projectId);
        if (company == null) {
            throw new CustomNotFoundException("Company with id " + projectId + " is not found.");
        }
        return company;
    }
    public int isOwnerOfCompany(Integer userId, Integer companyId) {
        return companyRepository.isOwnerOfCompany(userId, companyId);
    }
    public Integer isTrue(Integer userId, Integer companyId, Integer projectId, Integer phaseId) {
        return companyRepository.isTrue(userId, companyId, projectId, phaseId);
    }
}
