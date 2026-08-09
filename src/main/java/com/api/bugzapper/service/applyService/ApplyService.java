package com.api.bugzapper.service.applyService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.configuration.PushNotificationOptions;
import com.api.bugzapper.constant.RoleId;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.entity.*;
import com.api.bugzapper.model.request.ApplyRequest;
import com.api.bugzapper.repository.ApplyPostRecruitmentRepository;
import com.api.bugzapper.repository.ApplyRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.service.companyService.CompanyService;
import com.api.bugzapper.service.notificationService.NotificationService;
import com.api.bugzapper.service.postRecruitmentService.PostRecruitmentService;
import com.api.bugzapper.service.userService.AppUserService;
import com.api.bugzapper.util.EmailUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class ApplyService {
    private final ApplyRepository applyRepository;
    private final AppUserService appUserService;
    private final PostRecruitmentService postRecruitmentService;
    private final ApplyPostRecruitmentRepository applyPostRecruitmentRepository;
    private final NotificationService notificationService;
    private final UserRoleRepository userRoleRepository;
    private final EmailUtil emailUtil;
    private final GetCurrentUser getCurrentUser;
    private final CompanyService companyService;
    public void createApply(ApplyRequest applyRequest) {
        AppUser currentUser = getCurrentUser.getCurrentUser();

        AppUserDTO appUserDTO = appUserService.getUserDtoById(currentUser.getUserId());
        PostRecruitment postRecruitment = postRecruitmentService.getPostRecruitmentById(applyRequest.getPostRecruitmentId());

        Integer applyPostRecruitmentId = applyRepository.getApplyPostRecruitmentId(currentUser.getUserId(), applyRequest.getPostRecruitmentId());
        Integer isMemberOfCompany = userRoleRepository.isUserInCompany(currentUser.getUserId(), postRecruitment.getCompanyId());

        if (isMemberOfCompany != null && isMemberOfCompany > 0) {
            throw new CustomNotFoundException("You are already a member of this company, so you can not apply to its own recruitment post.");
        }

        if ("CLOSED".equalsIgnoreCase(postRecruitment.getStatus())) {
            throw new CustomNotFoundException("This post is no longer accepting applications.");
        }

        if (applyPostRecruitmentId != null) {
            throw new CustomNotFoundException("You already apply on this Post Recruitment.");
        }
        String applyJson;
        try {
            applyJson = new ObjectMapper().writeValueAsString(applyRequest.getApplyData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Apply apply = applyRepository.createApply(applyJson, applyRequest, currentUser.getUserId());
        Integer userRoleId = userRoleRepository.findUserRoleIdByCompanyIdLimit1(postRecruitment.getCompanyId());

        String applicantName = appUserDTO.getFirstName() + " " + appUserDTO.getLastName() + " has applied on your post";
        Notification notification = Notification.builder()
                .title("Apply notification")
                .description(applicantName)
                .status("apply")
                .applyId(apply.getApplyId())
                .userId(currentUser.getUserId()) // This id is the applicant owner
                .userRoleId(userRoleId) // This is the post owner
                .build();
        // Get post owner id
        Integer userId = userRoleRepository.findUserIdByUserRoleId(userRoleId);
        // Push notification to post owner
        PushNotificationOptions.sendMessageToUser(notification.getDescription(), userId, notification.getTitle());

        AppUserDTO appUser = appUserService.getUserDtoById(userId);
        try {
            emailUtil.sendNotificationToEmail(appUser.getEmail(), applicantName, "apply");
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send notification please try again");
        }
        // Insert notification to its table
        notificationService.createNotification(notification);

        applyPostRecruitmentRepository.insertToApplyPostRecruitment(apply.getApplyId(), postRecruitment.getPostRecruitmentId());
    }
    public Apply getApplyById(Integer id) {
        Apply apply = applyRepository.getApplyById(id);
        if (apply == null) {
            throw new CustomNotFoundException("Apply with id : " + id + " not found");
        }

        AppUser currentUser = getCurrentUser.getCurrentUser();
        Integer applicantId = apply.getUserId() != null ? apply.getUserId().getUserId() : null;
        if (applicantId != null && applicantId.equals(currentUser.getUserId())) {
            return apply;
        }

        PostRecruitment postRecruitment = getPostRecruitmentForApply(id);
        if (postRecruitment != null
                && postRecruitmentService.canManageRecruitmentForCompany(currentUser.getUserId(), postRecruitment.getCompanyId())) {
            return apply;
        }

        throw new CustomNotFoundException("You do not have permission to view this application.");
    }
    public List<ApplyCompany> getApplyByCompanyId(Integer companyId) {
        companyService.getCompanyById(companyId);
        AppUser currentUser = getCurrentUser.getCurrentUser();
        if (!postRecruitmentService.canManageRecruitmentForCompany(currentUser.getUserId(), companyId)) {
            throw new CustomNotFoundException("You do not have permission to view applications for this company.");
        }
        return applyRepository.getApplyByCompanyId(companyId);
    }

    private PostRecruitment getPostRecruitmentForApply(Integer applyId) {
        Integer postRecruitmentId = applyPostRecruitmentRepository.findPostRecruitmentIdByApplyId(applyId);
        if (postRecruitmentId == null) {
            return null;
        }
        return postRecruitmentService.getPostRecruitmentById(postRecruitmentId);
    }

    /**
     * Owner/recruiter-only: accept an application. Sets status ACCEPTED and grants the
     * applicant the post's configured role (defaults to DEVELOPER) in that company.
     */
    public Apply acceptApply(Integer applyId) {
        Apply apply = applyRepository.getApplyById(applyId);
        if (apply == null) {
            throw new CustomNotFoundException("Apply with id : " + applyId + " not found");
        }

        AppUser currentUser = getCurrentUser.getCurrentUser();
        PostRecruitment postRecruitment = getPostRecruitmentForApply(applyId);
        if (postRecruitment == null) {
            throw new CustomNotFoundException("This application is not linked to a recruitment post.");
        }
        if (!postRecruitmentService.canManageRecruitmentForCompany(currentUser.getUserId(), postRecruitment.getCompanyId())) {
            throw new CustomNotFoundException("You do not have permission to accept this application.");
        }
        if ("ACCEPTED".equalsIgnoreCase(apply.getStatus())) {
            throw new CustomNotFoundException("This application has already been accepted.");
        }

        Integer applicantId = apply.getUserId().getUserId();
        Integer companyId = postRecruitment.getCompanyId();

        boolean isOwner = userRoleRepository.isAdminOfThePost(currentUser.getUserId(), companyId) != null
                && userRoleRepository.isAdminOfThePost(currentUser.getUserId(), companyId) > 0;
        boolean isProjectManager = userRoleRepository.isProjectManagerOfCompany(currentUser.getUserId(), companyId) != null
                && userRoleRepository.isProjectManagerOfCompany(currentUser.getUserId(), companyId) > 0;

        if (isOwner) {
            Integer roleId = postRecruitment.getRoleId() != null ? postRecruitment.getRoleId() : RoleId.DEVELOPER;
            if (!userRoleRepository.isUserExistsInCompany(applicantId, companyId)) {
                Integer placeholderUserRoleId = userRoleRepository.checkCompanyIdIsNull(applicantId);
                if (placeholderUserRoleId != null) {
                    userRoleRepository.updateUserToCompany(applicantId, companyId, roleId, placeholderUserRoleId);
                } else {
                    userRoleRepository.assignRoleToUserInCompany(applicantId, companyId, roleId);
                }
            }
            applyRepository.updateApplyStatus(applyId, "ACCEPTED");
            return applyRepository.getApplyById(applyId);
        }

        if (isProjectManager) {
            applyRepository.updateApplyStatus(applyId, "PENDING_OWNER_APPROVAL");
            return applyRepository.getApplyById(applyId);
        }

        throw new CustomNotFoundException("You do not have permission to accept this application.");
    }

    /** Owner/recruiter-only: reject an application. Sets status REJECTED, grants no role. */
    public Apply rejectApply(Integer applyId) {
        Apply apply = applyRepository.getApplyById(applyId);
        if (apply == null) {
            throw new CustomNotFoundException("Apply with id : " + applyId + " not found");
        }

        AppUser currentUser = getCurrentUser.getCurrentUser();
        PostRecruitment postRecruitment = getPostRecruitmentForApply(applyId);
        if (postRecruitment == null) {
            throw new CustomNotFoundException("This application is not linked to a recruitment post.");
        }
        if (!postRecruitmentService.canManageRecruitmentForCompany(currentUser.getUserId(), postRecruitment.getCompanyId())) {
            throw new CustomNotFoundException("You do not have permission to reject this application.");
        }

        applyRepository.updateApplyStatus(applyId, "REJECTED");
        return applyRepository.getApplyById(applyId);
    }
}