package com.api.bugzapper.controller;

import com.api.bugzapper.model.entity.Apply;
import com.api.bugzapper.model.entity.ApplyCompany;
import com.api.bugzapper.model.request.ApplyRequest;
import com.api.bugzapper.model.response.ApiResponse;
import com.api.bugzapper.service.applyService.ApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/apply")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000","https://www.bugzapper.dev"})
public class ApplyController {
    private final ApplyService applyService;

    public ApplyController(ApplyService applyService) {
        this.applyService = applyService;
    }

    @Operation(summary = "apply to post recruitment")
    @PostMapping("/createApply")
    public ResponseEntity<?> create(@RequestBody ApplyRequest applyRequest) {
        applyService.createApply(applyRequest);
        ApiResponse<?> apiResponse = ApiResponse.<Apply>builder()
                .message("Apply has been inserted successfully")
                .code(201)
                .status(HttpStatus.CREATED).build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "view apply details")
    @GetMapping("/getApplyById/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Apply apply = applyService.getApplyById(id);
        ApiResponse<?> apiResponse = ApiResponse.<Apply>builder()
                .payload(apply)
                .message("Get apply by id " + id + " successfully")
                .code(200)
                .status(HttpStatus.OK).build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "get apply by company id")
    @GetMapping("/getApplyByCompanyId/{companyId}")
    public ResponseEntity<?> getApplyByCompanyId(@PathVariable Integer companyId) {
        List<ApplyCompany> applies = applyService.getApplyByCompanyId(companyId);
        ApiResponse<?> apiResponse = ApiResponse.<List<ApplyCompany>>builder()
                .message("Get all applies by company id : " + companyId + " successfully")
                .code(200)
                .payload(applies)
                .status(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "accept an application (owner/recruiter only) — grants the applicant the post's role")
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Integer id) {
        Apply apply = applyService.acceptApply(id);
        ApiResponse<?> apiResponse = ApiResponse.<Apply>builder()
                .payload(apply)
                .message("Application accepted successfully")
                .code(200)
                .status(HttpStatus.OK).build();
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "reject an application (owner/recruiter only)")
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Integer id) {
        Apply apply = applyService.rejectApply(id);
        ApiResponse<?> apiResponse = ApiResponse.<Apply>builder()
                .payload(apply)
                .message("Application rejected successfully")
                .code(200)
                .status(HttpStatus.OK).build();
        return ResponseEntity.ok(apiResponse);
    }

}
