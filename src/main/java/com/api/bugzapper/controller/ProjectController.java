package com.api.bugzapper.controller;

import com.api.bugzapper.configuration.GetCurrentUser;
import com.api.bugzapper.model.entity.AppUser;
import com.api.bugzapper.model.entity.Project;
import com.api.bugzapper.model.request.ProjectRequest;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.projectService.ProjectService;
import com.api.bugzapper.service.permissionService.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/project")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class ProjectController {
    private final ProjectService projectService;
    private final PermissionService permissionService;
    private final GetCurrentUser getCurrentUser;

    public ProjectController(ProjectService projectService, PermissionService permissionService, GetCurrentUser getCurrentUser) {
        this.projectService = projectService;
        this.permissionService = permissionService;
        this.getCurrentUser = getCurrentUser;
    }

    @Operation(summary = "create project - owner only")
    @PostMapping("/createProject")
    public ResponseEntity<?> create(@RequestBody ProjectRequest projectRequest) {
        // Only company owner can create projects
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyCompanyOwner(currentUser.getUserId(), projectRequest.getCompanyId());
        
        Project project = projectService.createProject(projectRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Project has created successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(project)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "view project details - with access control")
    @GetMapping("/getProjectById/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyProjectAccess(currentUser.getUserId(), id);
        
        Project project = projectService.getProjectByIdForPrivatePhase(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Project with id " + id + " was founded.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(project)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "update project - owner only")
    @PutMapping("/updateProjectById/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Integer id, @RequestBody ProjectRequest projectRequest) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        permissionService.verifyCompanyOwner(currentUser.getUserId(), projectRequest.getCompanyId());
        
        Project project = projectService.updateProject(id, projectRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Project with id " + id + " was updated.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(project)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "delete project - owner only")
    @DeleteMapping("/deleteProjectById/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer id) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        Integer companyId = projectService.getCompanyIdByProjectId(id);
        permissionService.verifyCompanyOwner(currentUser.getUserId(), companyId);
        
        projectService.getProjectById(id);
        projectService.deleteProject(id);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Project with id " + id + " was deleted.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/getProjectByTile/title/{name}")
    public ResponseEntity<?> getProjectByTile(@PathVariable String name) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        Project project = projectService.getProjectByTitle(name);
        permissionService.verifyProjectAccess(currentUser.getUserId(), project.getProjectId());
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Project with name " + name + " was founded.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(project)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all projects in a company - filtered by user role")
    @GetMapping("/getProjectByCompanyId/{companyId}")
    public ResponseEntity<?> getProjectByCompanyId(@PathVariable Integer companyId) {
        AppUser currentUser = permissionService.getCurrentAuthenticatedUser();
        
        // Get projects based on user's role
        List<Integer> accessibleProjectIds = permissionService.getAccessibleProjects(currentUser.getUserId(), companyId);
        List<Project> projects = projectService.getProjectsByIds(accessibleProjectIds);
        
        ApiResponse apiResponse = ApiResponse.builder()
                .message("All accessible projects with company id " + companyId + " fetched successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(projects)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

}
