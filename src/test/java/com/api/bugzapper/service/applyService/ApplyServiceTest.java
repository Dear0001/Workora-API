package com.api.bugzapper.service.applyService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.constant.RoleId;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.dto.AppliesDTO;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.entity.Apply;
import com.api.bugzapper.model.entity.PostRecruitment;
import com.api.bugzapper.repository.ApplyPostRecruitmentRepository;
import com.api.bugzapper.repository.ApplyRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.service.companyService.CompanyService;
import com.api.bugzapper.service.notificationService.NotificationService;
import com.api.bugzapper.service.postRecruitmentService.PostRecruitmentService;
import com.api.bugzapper.service.userService.AppUserService;
import com.api.bugzapper.util.EmailUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplyServiceTest {

    @Mock
    private ApplyRepository applyRepository;
    @Mock
    private AppUserService appUserService;
    @Mock
    private PostRecruitmentService postRecruitmentService;
    @Mock
    private ApplyPostRecruitmentRepository applyPostRecruitmentRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private EmailUtil emailUtil;
    @Mock
    private GetCurrentUser getCurrentUser;
    @Mock
    private CompanyService companyService;

    @InjectMocks
    private ApplyService applyService;

    @Test
    void getApplyById_byProjectManager_rejectsOwnerApprovalStatus() {
        Apply apply = new Apply();
        apply.setApplyId(10);
        apply.setStatus("PENDING_OWNER_APPROVAL");
        AppUserDTO applicant = new AppUserDTO();
        applicant.setUserId(8);
        apply.setUserId(applicant);

        AppUser currentUser = new AppUser();
        currentUser.setUserId(2);

        PostRecruitment postRecruitment = new PostRecruitment();
        postRecruitment.setCompanyId(100);

        when(getCurrentUser.getCurrentUser()).thenReturn(currentUser);
        when(applyRepository.getApplyById(10)).thenReturn(apply);
        when(applyPostRecruitmentRepository.findPostRecruitmentIdByApplyId(10)).thenReturn(22);
        when(postRecruitmentService.getPostRecruitmentById(22)).thenReturn(postRecruitment);
        when(postRecruitmentService.canManageRecruitmentForCompany(2, 100)).thenReturn(true);
        when(userRoleRepository.isAdminOfThePost(2, 100)).thenReturn(0);
        when(userRoleRepository.isProjectManagerOfCompany(2, 100)).thenReturn(1);

        assertThrows(CustomNotFoundException.class, () -> applyService.getApplyById(10));
    }

    @Test
    void getApplyById_byOwner_rejectsPendingStatus() {
        Apply apply = new Apply();
        apply.setApplyId(11);
        apply.setStatus("PENDING");
        AppUserDTO applicant = new AppUserDTO();
        applicant.setUserId(8);
        apply.setUserId(applicant);

        AppUser currentUser = new AppUser();
        currentUser.setUserId(3);

        PostRecruitment postRecruitment = new PostRecruitment();
        postRecruitment.setCompanyId(100);

        when(getCurrentUser.getCurrentUser()).thenReturn(currentUser);
        when(applyRepository.getApplyById(11)).thenReturn(apply);
        when(applyPostRecruitmentRepository.findPostRecruitmentIdByApplyId(11)).thenReturn(23);
        when(postRecruitmentService.getPostRecruitmentById(23)).thenReturn(postRecruitment);
        when(postRecruitmentService.canManageRecruitmentForCompany(3, 100)).thenReturn(true);
        when(userRoleRepository.isAdminOfThePost(3, 100)).thenReturn(1);
        when(userRoleRepository.isProjectManagerOfCompany(3, 100)).thenReturn(0);

        assertThrows(CustomNotFoundException.class, () -> applyService.getApplyById(11));
    }

    @Test
    void acceptApply_byProjectManager_setsPendingOwnerApprovalWithoutAssigningRole() {
        Apply apply = new Apply();
        apply.setApplyId(10);
        apply.setStatus("PENDING");
        AppUserDTO applicant = new AppUserDTO();
        applicant.setUserId(8);
        apply.setUserId(applicant);

        AppUser currentUser = new AppUser();
        currentUser.setUserId(2);

        PostRecruitment postRecruitment = new PostRecruitment();
        postRecruitment.setCompanyId(100);
        postRecruitment.setRoleId(RoleId.DEVELOPER);

        when(getCurrentUser.getCurrentUser()).thenReturn(currentUser);
        when(applyRepository.getApplyById(10)).thenReturn(apply);
        when(applyPostRecruitmentRepository.findPostRecruitmentIdByApplyId(10)).thenReturn(22);
        when(postRecruitmentService.getPostRecruitmentById(22)).thenReturn(postRecruitment);
        when(postRecruitmentService.canManageRecruitmentForCompany(2, 100)).thenReturn(true);
        when(userRoleRepository.isAdminOfThePost(2, 100)).thenReturn(0);
        when(userRoleRepository.isProjectManagerOfCompany(2, 100)).thenReturn(1);

        Apply result = applyService.acceptApply(10);

        assertEquals("PENDING_OWNER_APPROVAL", result.getStatus());
        verify(applyRepository).updateApplyStatus(10, "PENDING_OWNER_APPROVAL");
        verify(userRoleRepository, never()).assignRoleToUserInCompany(anyInt(), anyInt(), anyInt());
        verify(userRoleRepository, never()).updateUserToCompany(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void acceptApply_byOwner_setsAcceptedAndAssignsRole() {
        Apply apply = new Apply();
        apply.setApplyId(11);
        apply.setStatus("PENDING");
        AppUserDTO applicant = new AppUserDTO();
        applicant.setUserId(9);
        apply.setUserId(applicant);

        AppUser currentUser = new AppUser();
        currentUser.setUserId(3);

        PostRecruitment postRecruitment = new PostRecruitment();
        postRecruitment.setCompanyId(100);
        postRecruitment.setRoleId(RoleId.DEVELOPER);

        when(getCurrentUser.getCurrentUser()).thenReturn(currentUser);
        when(applyRepository.getApplyById(11)).thenReturn(apply);
        when(applyPostRecruitmentRepository.findPostRecruitmentIdByApplyId(11)).thenReturn(23);
        when(postRecruitmentService.getPostRecruitmentById(23)).thenReturn(postRecruitment);
        when(postRecruitmentService.canManageRecruitmentForCompany(3, 100)).thenReturn(true);
        when(userRoleRepository.isAdminOfThePost(3, 100)).thenReturn(1);
        when(userRoleRepository.isProjectManagerOfCompany(3, 100)).thenReturn(0);
        when(userRoleRepository.isUserExistsInCompany(9, 100)).thenReturn(false);
        when(userRoleRepository.checkCompanyIdIsNull(9)).thenReturn(null);

        Apply result = applyService.acceptApply(11);

        assertEquals("ACCEPTED", result.getStatus());
        verify(userRoleRepository).assignRoleToUserInCompany(9, 100, RoleId.DEVELOPER);
        verify(applyRepository).updateApplyStatus(11, "ACCEPTED");
    }

    @Test
    void appliesDto_exposesStatus() {
        AppliesDTO dto = new AppliesDTO();
        dto.setStatus("PENDING");
        dto.setViewCondition("PM_PENDING_REVIEW");

        assertEquals("PENDING", dto.getStatus());
        assertEquals("PM_PENDING_REVIEW", dto.getViewCondition());
    }
}
