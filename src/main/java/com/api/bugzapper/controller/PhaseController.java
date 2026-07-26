package com.api.bugzapper.controller;

import com.api.bugzapper.model.dto.AddMemberToPhaseDTO;
import com.api.bugzapper.model.dto.PhaseDTO;
import com.api.bugzapper.model.dto.PhaseMemberDTO;
import com.api.bugzapper.model.dto.PhaseNewsfeedDTO;
import com.api.bugzapper.model.entity.Phase;
import com.api.bugzapper.model.request.AssignRoleRequest;
import com.api.bugzapper.model.request.PhaseRequest;
import com.api.bugzapper.model.request.PublicPhaseRequest;
import com.api.bugzapper.model.request.UserIdsRequest;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.phaseService.PhaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/phases")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class PhaseController {
    private final PhaseService phaseService;

    public PhaseController(PhaseService phaseService) {
        this.phaseService = phaseService;
    }

    @Operation(summary = "create private phase")
    @PostMapping("/createPrivatePhase")
    public ResponseEntity<?> createPrivatePhase(@Valid @RequestBody PhaseRequest phaseRequest) {
        Phase phase = phaseService.createPrivatePhase(phaseRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase has created successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(phase)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "create public phase")
    @PostMapping("/createPublicPhase")
    public ResponseEntity<?> createPublicPhase(@Valid @RequestBody PublicPhaseRequest publicPhaseRequest) {
        Phase phase = phaseService.createPublicPhase(publicPhaseRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase has created successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(phase)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "join phase by link")
    @PostMapping("/joinPhaseByLink")
    public ResponseEntity<?> joinPhaseByLink(@RequestParam String code) {
        Integer phaseId = phaseService.addUserToPhaseByCode(code);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Successfully joined the phase.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phaseId)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all user's phase")

    @GetMapping("/getAllPhaseByUserId")
    public ResponseEntity<?> getAllPhaseByUserId(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<PhaseDTO> phase = phaseService.getAllByUserId(offset, limit);
        ApiResponse<List<PhaseDTO>> apiResponse = ApiResponse.<List<PhaseDTO>>builder()
                .message("Get all phases successfully")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phase)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all public phases")
    @GetMapping("/getAllPublicPhase")
    public ResponseEntity<?> getAllPublicPhase(
            @Positive(message = "Offset cannot be negative or zero") @RequestParam(defaultValue = "1") Integer offset,
            @Positive(message = "Limit cannot be negative or zero") @RequestParam(defaultValue = "5") Integer limit
    ) {
        ApiResponse<List<PhaseNewsfeedDTO>> apiResponse = ApiResponse.<List<PhaseNewsfeedDTO>>builder()
                .message("Get all public phases successfully")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phaseService.getAllPublicPhase(offset, limit))
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "view phase details")
    @GetMapping("/getPhaseById/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        Phase phase = phaseService.findById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase with id " + id + " has founded.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phase)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "update private phase")
    @PutMapping({"/updatePrivatePhaseById/{id}", "/updatePrivatePhaseById{id}"})
    public ResponseEntity<?> updatePrivatePhase(@PathVariable Integer id, @Valid @RequestBody PhaseRequest phaseRequest) {
        Phase phase = phaseService.updatePrivatePhase(id, phaseRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase with id " + id + " has updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phase)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "update public phase")
    @PutMapping({"/updatePublicPhaseById/{id}", "/updatePublicPhaseById{id}"})
    public ResponseEntity<?> updatePublicPhase(@PathVariable Integer id,@Valid @RequestBody PublicPhaseRequest publicPhaseRequest) {
        Phase phase = phaseService.updatePublicPhase(id, publicPhaseRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase with id " + id + " has updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phase)
                .build();
        return ResponseEntity.ok(apiResponse);
    }


    @Operation(summary = "delete phase")
    @DeleteMapping("/deletePhaseById/{id}")
    public ResponseEntity<?> deletePhase(@PathVariable Integer id) {
        phaseService.deletePhase(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase with id " + id + " has deleted.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all phase members")
    @GetMapping("/getPhaseMembersByPhaseId/{phaseId}")
    public ResponseEntity<?> getPhaseMembers(@PathVariable Integer phaseId) {
        List<PhaseMemberDTO> phaseMembers = phaseService.getPhaseMembers(phaseId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all members in phase successfully")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phaseMembers)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "change phase type (false= public , true = private)")
    @PutMapping("/changePhaseType/{phaseId}/type")
    public ResponseEntity<?> changePhaseType(@PathVariable Integer phaseId, @RequestParam Boolean isPrivate) {
        Phase phase = phaseService.changePhaseType(phaseId, isPrivate);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase type updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phase)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "close or reopen a public phase to new bug reports (owner/project-manager only)")
    @PutMapping("/{phaseId}/status")
    public ResponseEntity<?> updatePhaseStatus(@PathVariable Integer phaseId, @RequestParam String status) {
        Phase phase = phaseService.updatePhaseStatus(phaseId, status);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase status updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phase)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "remove member from phase")
    @DeleteMapping("/removeMemberFromPhaseByPhaseId/{phaseId}")
    public ResponseEntity<?> removeMemberFromPhase(@PathVariable Integer phaseId, @RequestBody UserIdsRequest userIds) {
        phaseService.removeMemberFromPhase(phaseId, userIds);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Users has deleted successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get all phase in a project")
    @GetMapping("/getAllPhaseByProjectId/{projectId}")
    public ResponseEntity<?> getAllPhaseByProjectId(@PathVariable Integer projectId) {
        List<Phase> phases = phaseService.getAllPhaseByProjectId(projectId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Get all phases with id " + projectId + " successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phases)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "assign role to user in phase by phase id")
    @PutMapping("/assignRoleToUserByPhaseId/{phaseId}")
    public ResponseEntity<?> assignRoleToUserByPhaseId(@Valid @RequestBody AssignRoleRequest assignRoleRequest, @PathVariable Integer phaseId) {
        phaseService.assignRoleToUserByPhaseId(assignRoleRequest, phaseId);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Assign role to members in phases id " + phaseId + " successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
