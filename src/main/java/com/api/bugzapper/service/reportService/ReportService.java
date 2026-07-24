package com.api.bugzapper.service.reportService;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.configuration.PushNotificationOptions;
import com.api.bugzapper.exception.CustomNotFoundException;
import com.api.bugzapper.model.dto.AppUserDTO;
import com.api.bugzapper.model.entity.*;
import com.api.bugzapper.model.request.ReportPhaseRequest;
import com.api.bugzapper.repository.ProjectRepository;
import com.api.bugzapper.repository.ReportRepository;
import com.api.bugzapper.repository.UserRoleRepository;
import com.api.bugzapper.service.companyService.CompanyService;
import com.api.bugzapper.service.notificationService.NotificationService;
import com.api.bugzapper.service.phaseService.PhaseService;
import com.api.bugzapper.service.projectService.ProjectService;
import com.api.bugzapper.service.userService.AppUserService;
import com.api.bugzapper.util.EmailUtil;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final PhaseService phaseService;
    private final ProjectService projectService;
    private final NotificationService notificationService;
    private final PushNotificationOptions pushNotificationOptions;
    private final AppUserService appUserService;
    private final UserRoleRepository userRoleRepository;
    private final EmailUtil emailUtil;
    private final GetCurrentUser getCurrentUser;
    private final CompanyService companyService;
    private final ProjectRepository projectRepository;
    public Report getReportById(Integer id) {
        Report report = reportRepository.getReportById(id);
        if (report == null) {
            throw new CustomNotFoundException("Report with id " + id + " not found");
        }

        AppUser currentUser = getCurrentUser.getCurrentUser();
        Integer phaseId = report.getPhaseId() != null ? report.getPhaseId().getId() : null;
        Integer reporterId = report.getUserId() != null ? report.getUserId().getUserId() : null;

        // The reporter can always see their own report; otherwise the same phase-membership
        // check as getReportByPhaseId applies (matches the private-phase visibility rule).
        if (reporterId != null && reporterId.equals(currentUser.getUserId())) {
            return report;
        }
        if (phaseId == null) {
            throw new CustomNotFoundException("You do not have permission to view this report.");
        }
        phaseService.findById(phaseId);
        Integer projectId = projectRepository.getProjectByPhaseId(phaseId);
        Company company = companyService.getCompanyByProjectId(projectId);
        Integer count = userRoleRepository.isUserInCompanyProjectPhase(currentUser.getUserId(), company.getCompanyId(), projectId, phaseId);
        if (count < 1) {
            throw new CustomNotFoundException("You do not have permission to view this report.");
        }
        return report;
    }
    public void createReportPhase(ReportPhaseRequest reportPhaseRequest) {
        Phase phase = phaseService.findById(reportPhaseRequest.getPhaseId());
        AppUser currentUser = getCurrentUser.getCurrentUser();
        Integer userId = currentUser.getUserId();
        AppUserDTO appUserDTO = appUserService.getUserDtoById(userId);
        System.out.println(userId);
        String applicantName = appUserDTO.getFirstName() + " " + appUserDTO.getLastName() + " has report on your public post";
        Integer projectId = projectService.getProjectByPhaseId(reportPhaseRequest.getPhaseId());
        List<Integer> userReceiverIds =  userRoleRepository.findUserIdByProjectId(projectId);
        Integer companyId = companyService.getCompanyIdByPhaseId(phase.getId());

        // Insert notification to its table
        Integer reportId = reportRepository.createReportPhase(reportPhaseRequest, userId);

        for (Integer userReceiverId : userReceiverIds) {
            if (userReceiverId.equals(userId)) {
                continue;
            }
            Integer userRoleId = userRoleRepository.findUserRoleIdByUserIdAndCompanyId(userReceiverId, companyId);

            AppUserDTO userReceiver = appUserService.getUserDtoById(userReceiverId);

            Notification notification = Notification.builder()
                    .title("Report notification")
                    .description(applicantName)
                    .status("report")
                    .reportId(reportId)
                    .userId(userId) // This id is the applicant owner
                    .userRoleId(userRoleId) // This is the post owner
                    .build();

            // Push notification to post owner
            PushNotificationOptions.sendMessageToUser(notification.getDescription(), userReceiverId, notification.getTitle());

            try {
                emailUtil.sendNotificationToEmail(userReceiver.getEmail(), applicantName, "report");
            } catch (MessagingException e) {
                throw new RuntimeException("Unable to send notification please try again");
            }
            notificationService.createNotification(notification);
        }
    }
    public List<Report> getReportByPhaseId(Integer offset, Integer limit, Integer phaseId) {
        AppUser currentUser = getCurrentUser.getCurrentUser();
        phaseService.findById(phaseId);

        Integer projectId = projectRepository.getProjectByPhaseId(phaseId);
        Company company = companyService.getCompanyByProjectId(projectId);
        Integer count = userRoleRepository.isUserInCompanyProjectPhase(currentUser.getUserId(),company.getCompanyId(), projectId, phaseId);

        if (count < 1) {
            throw new CustomNotFoundException("This user is not a member of this phase id : " + phaseId);
        }
        return reportRepository.getReportByPhaseId(offset,limit,phaseId);
    }

}
