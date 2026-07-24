package com.api.bugzapper.controller;

import com.api.bugzapper.model.dto.*;
import com.api.bugzapper.model.entity.*;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.dashboardService.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000", "https://www.bugzapper.dev"})
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "count company, project, task and bug report in dashboard")
    @GetMapping("/getAllCount")
    public ResponseEntity<?> getAllCountByUserId() {
        Dashboard dashboard = dashboardService.getAllCountByUserId();
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all counts successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(dashboard)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "count task status on type")
    @GetMapping("/countTaskStatusByPhaseId/{phaseId}")
    public ResponseEntity<?> countTaskStatusByPhaseId(@PathVariable Integer phaseId) {
        CountTaskStatusDTO countTaskStatusDTO = dashboardService.countTaskStatusByPhaseId(phaseId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all counts successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(countTaskStatusDTO)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all company that user join")
    @GetMapping("/getAllCompaniesUserJoin")
    public ResponseEntity<?> getAllCompanyByUserId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<CompaniesDTO> company = dashboardService.getAllCompanyByUserId(offset, limit);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all company successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(company)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all user's own company")
    @GetMapping("/getAllOwnCompanies")
    public ResponseEntity<?> getAllOwnCompanyByUserId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<CompaniesDTO> companies = dashboardService.getAllOwnCompanyByUserId(offset, limit);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all own company successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(companies)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all project that user join ")
    @GetMapping("/getAllProjects")
    public ResponseEntity<?> getAllProjectsByUserId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<ProjectsDTO> projects = dashboardService.getAllProjectsByUserId(offset, limit);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all own projects successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(projects)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "that user is assigned from company (use in workspace)")
    @GetMapping("/getAllTasks")
    public ResponseEntity<?> getAllTasksByUserId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<TasksDTO> tasks = dashboardService.getAllTasksByUserId(offset, limit);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all own tasks successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(tasks)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all report that user report to his company (use in workspace)")
    @GetMapping("/getAllReports")
    public ResponseEntity<?> getAllReportsByUserIdAndCompanyId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<ReportsDTO> reports = dashboardService.getAllReportsByUserId(offset, limit);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all own reports successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(reports)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all application that bug hunter apply to user's company (use in workspace)")
    @GetMapping("/getAllApplies")
    public ResponseEntity<?> getAllApplyByUserIdAndCompanyId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<AppliesDTO> applies = dashboardService.getAllApplyByUserIdAndCompanyId(offset, limit);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all own applies successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(applies)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all notification")
    @GetMapping("/getAllNotification")
    public ResponseEntity<?> getAllNotificationByUserId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<NotificationDTO> notifications = dashboardService.getAllNotificationByUserId(offset, limit);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all own notification successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(notifications)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "update notification by id")
    @PutMapping("/updateNotificationStatusById/isRead/{id}")
    public ResponseEntity<?> updateNotificationStatus(@PathVariable Integer id, @RequestParam Boolean isRead) {
        dashboardService.updateNotificationStatus(id, isRead);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Notification status updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all companies both user join and own")
    @GetMapping("/getAllCompanies")
    public ResponseEntity<?> getAllOwnCompanies(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<CompaniesDTO> companies = dashboardService.getAllCompany(offset, limit);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all own company successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(companies)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all project by company id of current user")
    @GetMapping("/getAllProjectByCompanyId/{companyId}")
    public ResponseEntity<?> getAllProjectByCompanyId(@PathVariable Integer companyId) {
        List<Project> companies = dashboardService.getAllProjectByCompanyId(companyId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all projects by company id successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(companies)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all phases by project id of current user")
    @GetMapping("/getAllPhaseByProjectId/{companyId}/{projectId}")
    public ResponseEntity<?> getAllPhaseByProjectId(@PathVariable Integer companyId, @PathVariable Integer projectId) {
        List<PhaseDTO> companies = dashboardService.getAllPhaseByProjectId(companyId, projectId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all phases by project id successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(companies)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all task by phase id of current user")
    @GetMapping("/getAllTaskByPhaseId/{companyId}/{projectId}/{phaseId}")
    public ResponseEntity<?> getAllTaskByPhaseId(@PathVariable Integer companyId, @PathVariable Integer projectId, @PathVariable Integer phaseId) {
        List<TasksDTO> companies = dashboardService.getAllTaskByPhaseId(companyId, projectId, phaseId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all tasks by phase id successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(companies)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
