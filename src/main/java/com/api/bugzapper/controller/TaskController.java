package com.api.bugzapper.controller;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.model.dto.SubmissionDTO;
import com.api.bugzapper.model.dto.TaskMemberDTO;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.entity.Task;
import com.api.bugzapper.model.request.*;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.taskService.TaskService;
import com.api.bugzapper.service.permissionService.PermissionService;
import com.api.bugzapper.service.projectService.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class TaskController {
    private final TaskService taskService;
    private final PermissionService permissionService;
    private final GetCurrentUser getCurrentUser;
    private final ProjectService projectService;

    public TaskController(
            TaskService taskService,
            PermissionService permissionService,
            GetCurrentUser getCurrentUser,
            ProjectService projectService) {
        this.taskService = taskService;
        this.permissionService = permissionService;
        this.getCurrentUser = getCurrentUser;
        this.projectService = projectService;
    }

    @Operation(summary = "create task - owner/pm only")
    @PostMapping("/createTask")
    public ResponseEntity<?> create(@Valid @RequestBody TaskRequest taskRequest) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        Integer projectId = projectService.getProjectByPhaseId(taskRequest.getPhaseId());
        permissionService.verifyProjectManagerOrOwner(currentUser.getUserId(), projectId);
        permissionService.verifyPhaseAccess(currentUser.getUserId(), taskRequest.getPhaseId());
        
        Task task = taskService.createTask(taskRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Task has created successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(task)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "submit task - assignee only")
    @PostMapping("/submitTask")
    public ResponseEntity<?> submitTask(@Valid @RequestBody TaskSubmitRequest taskSubmitRequest) throws MessagingException {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyTaskSubmit(currentUser.getUserId(), taskSubmitRequest.getTaskId());
        
        taskService.submitTask(taskSubmitRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Task has been submitted successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all submit data in a task - with access control")
    @GetMapping("/getAllSubmitDataByTaskId/{taskId}")
    public ResponseEntity<?> getAllSubmitDataByTaskId(@PathVariable Integer taskId) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyTaskAccess(currentUser.getUserId(), taskId);
        
        List<SubmissionDTO> task = taskService.getAllSubmitDataByTaskId(taskId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Task with id " + taskId + " was founded with all submit data successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(task)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "count user in a task submission")
    @GetMapping("/countUserInATaskSubmission/{taskId}")
    public ResponseEntity<?> countUserInATaskSubmission(@PathVariable Integer taskId) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyTaskAccess(currentUser.getUserId(), taskId);

        Integer count = taskService.countUserInATaskSubmission(taskId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Task with id " + taskId + " was founded with count user in submission successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(count)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "view task details - with access control")
    @GetMapping("/findTaskById/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyTaskAccess(currentUser.getUserId(), id);
        
        Task task = taskService.getTaskById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Task with id " + id + " was founded.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(task)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "update task - owner/pm only")
    @PutMapping("updateTaskById/{id}")
    public ResponseEntity<?> updateTaskById(@Positive @PathVariable Integer id,@Valid @RequestBody UpdateTaskRequest taskRequest) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        Integer projectId = projectService.getProjectByPhaseId(taskRequest.getPhaseId());
        permissionService.verifyProjectManagerOrOwner(currentUser.getUserId(), projectId);
        
        Task task = taskService.updateTaskById(id, taskRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Task with id " + id + "has updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(task)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "delete task - owner/pm only")
    @DeleteMapping("deleteTaskById/{id}")
    public ResponseEntity<?> deleteTaskById(@PathVariable Integer id) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        Integer projectId = taskService.getProjectIdForTask(id);
        permissionService.verifyProjectManagerOrOwner(currentUser.getUserId(), projectId);
        
        taskService.deleteTaskById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Task with id " + id + " has deleted successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "assign task - pm/owner only")
    @PutMapping("assignTaskToMemberInCompany/{companyId}/{phaseId}/{taskId}")
    public ResponseEntity<?> assignTaskToMemberInCompany(@Valid @RequestBody AssignTaskRequest assignTaskRequest) throws MessagingException {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyProjectManagerOrOwner(currentUser.getUserId(), assignTaskRequest.getProjectId());
        
        taskService.assignTaskToMemberInCompany(assignTaskRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Task with id " + assignTaskRequest.getTaskId() + " has been assigned to user of a company with id " + assignTaskRequest.getCompanyId() + " successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "view task member")
    @GetMapping("taskMember/{taskId}")
    public ResponseEntity<?> getAllTaskMember(@PathVariable Integer taskId) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyTaskAccess(currentUser.getUserId(), taskId);

        List<TaskMemberDTO> taskMembers = taskService.getAllTaskMember(taskId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all task members successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(taskMembers)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "update task status - owner, pm, or assignee")
    @PutMapping("updateTaskStatus/{taskId}/{phaseId}/{companyId}")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Integer taskId, @PathVariable Integer phaseId, @PathVariable Integer companyId, @RequestBody TaskForStatus taskForStatus) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyTaskStatusEdit(currentUser.getUserId(), taskId);
        
        taskService.updateTaskStatus(taskId, phaseId, companyId, taskForStatus);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Task with id " + taskId + " has been updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get all task of a phase - with access control")
    @GetMapping("getAllTaskByPhaseId/{phaseId}")
    public ResponseEntity<?> getAllTaskByPhaseId(@PathVariable Integer phaseId) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyPhaseAccess(currentUser.getUserId(), phaseId);
        
        List<Task> tasks = taskService.getAllTaskByPhaseId(phaseId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all task in phase id " + phaseId + "successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(tasks)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
