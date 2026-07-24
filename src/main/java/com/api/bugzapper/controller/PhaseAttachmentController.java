package com.api.bugzapper.controller;

import com.api.bugzapper.model.entity.PhaseAttachment;
import com.api.bugzapper.model.request.PhaseAttachmentRequest;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.phaseAttachmentService.PhaseAttachmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/phaseAttachment")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class PhaseAttachmentController {
    private final PhaseAttachmentService phaseAttachmentService;

    public PhaseAttachmentController(PhaseAttachmentService phaseAttachmentService) {
        this.phaseAttachmentService = phaseAttachmentService;
    }

    @PostMapping("/createPhase")
    public ResponseEntity<?> create(@Valid @RequestBody PhaseAttachmentRequest phaseAttachmentRequest) {
        PhaseAttachment phaseAttachment = phaseAttachmentService.create(phaseAttachmentRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase attachment has created successfully.")
                .status(HttpStatus.CREATED)
                .code(201)
                .payload(phaseAttachment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/getPhaseAttachmentById/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        PhaseAttachment phaseAttachment = phaseAttachmentService.findById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase with id " + id + " has founded.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phaseAttachment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/updatePhaseAttachmentById/{id}")
    public ResponseEntity<?> updatePhase(@PathVariable Integer id,@Valid @RequestBody PhaseAttachmentRequest phaseAttachmentRequest) {
        PhaseAttachment phaseAttachment = phaseAttachmentService.updateAttachment(id, phaseAttachmentRequest);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase with id " + id + " has updated successfully.")
                .status(HttpStatus.OK)
                .code(200)
                .payload(phaseAttachment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/deletePhaseAttachmentById/{id}")
    public ResponseEntity<?> deletePhase(@PathVariable Integer id) {
        phaseAttachmentService.deleteAttachment(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("Phase with id " + id + " has deleted.")
                .status(HttpStatus.OK)
                .code(200)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
